pub mod crusp_client;

use measurement_shared_rust::token::Token;
use crate::crusp_client::{measure_downlink, measure_uplink};
use measurement_shared_rust::crusp_error::CruspError;
use measurement_shared_rust::results::MeasurementResult;

/// As we will be calling this library from non-Rust code,
/// we will actually be calling it through a C bridge.
/// `#[nomangle]` tells the compiler not to mangle the function name as it usually does by default,
/// ensuring our function name is exported as if it had been written in C.
/// `extern` tells the Rust compiler that this function will be called from outside of Rust
/// and to therefore ensure that it is compiled using C calling conventions.
pub extern fn rust_downlink(jni_crusp_token: Token, jni_server_ip: String, jni_server_path: String, jni_server_port: u32, ) -> Result<MeasurementResult, CruspError> {
    return measure_downlink(jni_crusp_token, jni_server_ip, jni_server_port, jni_server_path);
}

pub extern fn rust_uplink(jni_crusp_token: Token, jni_server_ip: String, jni_server_path: String, jni_server_port: u32, ) -> Result<MeasurementResult, CruspError> {
    return measure_uplink(jni_crusp_token, jni_server_ip, jni_server_port, jni_server_path);
}

/// Expose the JNI interface for android below
/// The first line here `#[cfg(target_os="android")]` is telling the compiler to target Android
/// when compiling this module. `#[cfg]` is a special attribute that
/// allows you to compile code based on a flag passed to the compiler.
/// The second line, `#[allow(non_snake_case)]`, tells the compiler not to warn if
/// we are not using snake_case for a variable or function name.
/// The Rust compiler is very strict and it enforces the use of snake_case throughout.
/// However, we defined our class name and native method in our Android project using
/// Java coding conventions which is camelCase and UpperCamelCase and we don’t want
/// to change this or our Java code will look wrong.
/// Given the way that JNI constructs native function names, we need to tell the Rust compiler
/// to go easy on us in this instance. This flag will apply to all functions and variables
/// created inside this module that we are creating, called android.
#[cfg(target_os="android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate jni;
    /// After declaring that we need the `jni` crate, and importing some useful objects from it,
    /// we can declare our function.
    /// This function needs to be marked unsafe because we will be dealing with pointers
    /// from a language that allows null pointers, but our code doesn’t check for NULL.
    /// By marking the function as not memory safe, we are alerting other Rust functions
    /// that it may not be able to deal with a null pointer.
    /// `extern` defines the function as one that will be exposed to other languages.

    use super::*;
    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JString, JObject, JValue, JMethodID};
    use self::jni::sys::{jobject, jobjectArray};
    use log::{Level, error};
    use android_logger::{Config};
    use measurement_shared_rust::crusp_error::{CruspError, ErrorType};
    use measurement_shared_rust::results::{MeasurementResult, SequenceDetails};

    /// As arguments, along with the JString that our Java function declaration said that we will
    /// be providing, we also need to take an instance of the JNIEnv and a
    /// class reference (which is unused in this example).
    /// The JNIEnv will be the object we will use to read values associated with the
    /// pointers that we are taking as argument.
    #[no_mangle]
    pub unsafe extern fn Java_at_ac_tuwien_nt_abe_measurement_RustClient_downlink(env: JNIEnv, _: JClass, jni_crusp_token: JObject, jni_server_ip: JString, jni_server_path: JString, jni_server_port: JString) -> jobject {
        // Using the JNIEnv transfers the ownership of the object to Java

        //init logger so that logging is forwarded to logcat
        android_logger::init_once(
            Config::default()
                .with_min_level(Level::Trace) // limit log level
        );

        let (token, host, path, port) = match convert_java_to_rust(&env, jni_crusp_token, jni_server_ip, jni_server_path, jni_server_port) {
            Ok(result) => result,
            Err(err) => return new_error_result(&env, err).unwrap_or(JObject::null().into_inner()),
        };

        return match rust_downlink(token, host, path, port) {
            Ok(res) => {
                match convert_rust_to_java(&env, res) {
                    Ok(jobject) => jobject.into_inner(),
                    Err(err) => {
                        error!("error when converting the result from rust to java: {}", err.msg);
                        new_error_result(&env, err).unwrap_or(JObject::null().into_inner())
                    },
                }
            },
            Err(err) => {
                error!("error when executing the measurement: {}", err.msg);
                new_error_result(&env, err).unwrap_or(JObject::null().into_inner())
            },
        };
    }

    #[no_mangle]
    pub unsafe extern fn Java_at_ac_tuwien_nt_abe_measurement_RustClient_uplink(env: JNIEnv, _: JClass, jni_crusp_token: JObject, jni_server_ip: JString, jni_server_path: JString, jni_server_port: JString) -> jobject {
        // Using the JNIEnv transfers the ownership of the object to Java

        //init logger so that logging is forwarded to logcat
        android_logger::init_once(
            Config::default()
                .with_min_level(Level::Trace) // limit log level
        );

        let (token, host, path, port) = match convert_java_to_rust(&env, jni_crusp_token, jni_server_ip, jni_server_path, jni_server_port) {
            Ok(result) => result,
            Err(err) => return new_error_result(&env, err).unwrap_or(JObject::null().into_inner()),
        };

        return match rust_uplink(token, host, path, port) {
            Ok(res) => {
                match convert_rust_to_java(&env, res) {
                    Ok(jobject) => jobject.into_inner(),
                    Err(err) => {
                        error!("error when converting the result from rust to java: {}", err.msg);
                        new_error_result(&env, err).unwrap_or(JObject::null().into_inner())
                    },
                }
            },
            Err(err) => {
                error!("error when executing the measurement: {}", err.msg);
                new_error_result(&env, err).unwrap_or(JObject::null().into_inner())
            },
        };
    }

    fn new_error_result(env: &JNIEnv, error: CruspError) -> Result<jobject, CruspError> {
        let msg = error.msg.clone();
        let jstring = env.new_string(msg)?;
        let message = JValue::Object(JObject::from(jstring));

        let field_id_str = match error.get_error_type() {
            ErrorType::Error => "CRUSP_ERROR",
            ErrorType::JNIError => "JNI_ERROR",
            ErrorType::INPUTError => "INPUT_ERROR",
            ErrorType::COMMUNICATIONError => "COMMUNICATION_ERROR",
            ErrorType::NOTFOUNDError => "SETTINGS_NOT_FOUND_ERROR",
        };

        let big_int_class = env.find_class("java/math/BigInteger")?;
        let result_class = env.find_class("at/ac/tuwien/nt/abe/model/MeasurementResult")?;
        let crusp_error_class = env.find_class("at/ac/tuwien/nt/abe/model/CruspError")?;
        let start_time = convert_u128_to_jbiginteger(&env, error.time, &big_int_class)?;
        let error_field = env.get_static_field(crusp_error_class, field_id_str, "Lat/ac/tuwien/nt/abe/model/CruspError;")?;

        return Ok(env.new_object(result_class, "(Lat/ac/tuwien/nt/abe/model/CruspError;Ljava/lang/String;Ljava/math/BigInteger;)V", &[error_field, message, start_time])?.into_inner());
    }

    fn convert_rust_to_java<'a>(env: &'a JNIEnv, result: MeasurementResult) -> Result<JObject<'a>, CruspError> {
        let result_class = env.find_class("at/ac/tuwien/nt/abe/model/MeasurementResult")?;
        let seq_details_class = env.find_class("at/ac/tuwien/nt/abe/model/SequenceDetails")?;
        let recv_packet_details_class = env.find_class("at/ac/tuwien/nt/abe/model/ReceivedPacketDetails")?;
        let big_int_class = env.find_class("java/math/BigInteger")?;

        let bandwidth = JValue::Float(result.available_bandwidth);
        let packets = JValue::Int(result.num_received_packets as i32);

        let start_time = convert_u128_to_jbiginteger(&env, result.start_time, &big_int_class)?;

        let coll_array: jobjectArray = env.new_object_array(result.sequences.len() as i32,
                                                              seq_details_class,
                                                              JObject::null())?;

        let mut i = 0;
        for seq_details in result.sequences {
            let jsequence_details = convert_sequence_details(&env, seq_details, &recv_packet_details_class, &big_int_class, &seq_details_class)?;
            env.set_object_array_element(coll_array, i, jsequence_details)?;
            i +=1;
        }

        let ret: JObject = env.new_object(result_class,
                                          "([Lat/ac/tuwien/nt/abe/model/SequenceDetails;Ljava/math/BigInteger;IF)V",
                                          &[JValue::Object(JObject::from(coll_array)), start_time, packets, bandwidth])?;
        return Ok(ret);

    }

    fn convert_sequence_details<'a>(env: &'a JNIEnv, sequence: SequenceDetails, recv_packet_details_class: &JClass, big_int_class: &JClass, seq_details_class: &JClass) -> Result<JObject<'a>, CruspError> {
        let jdetails_array: jobjectArray = env.new_object_array(sequence.packet_details_vec.len() as i32,
                                                                *recv_packet_details_class,
                                                                JObject::null())?;

        let packet_constructor: JMethodID = env.get_method_id(*recv_packet_details_class, "<init>", "(IIJI)V")?; // cache MethodId

        for (i, packet_details) in sequence.packet_details_vec.iter().enumerate() {
            let packet_nr = JValue::Int(packet_details.packet_nr as i32);
            let repeat_nr = JValue::Int(packet_details.repeat_nr as i32);
            let delta_to_start_ns = JValue::Long(packet_details.delta_to_start_ns as i64);
            let recv_bytes = JValue::Int(packet_details.recv_bytes_amount as i32);

            let jpacket_details = env.new_object_unchecked(*recv_packet_details_class,
                                                           packet_constructor,
             &[repeat_nr, packet_nr, delta_to_start_ns, recv_bytes])?;

            env.set_object_array_element(jdetails_array, i as i32, jpacket_details)?;
            error!("{}", i);
        }

        let jexpected_packets = JValue::Int(sequence.expected_packets as i32);
        let jnaive_rate = JValue::Float(sequence.naive_rate);
        let jseq_start_time = convert_u128_to_jbiginteger(&env, sequence.seq_start_time, &big_int_class)?;

        return Ok(env.new_object(*seq_details_class,
         "([Lat/ac/tuwien/nt/abe/model/ReceivedPacketDetails;IFLjava/math/BigInteger;)V",
        &[JValue::Object(JObject::from(jdetails_array)), jexpected_packets, jnaive_rate, jseq_start_time])?);
    }

    fn convert_u128_to_jbiginteger<'a>(env: &'a JNIEnv, value: u128, big_int_class: &JClass) -> Result<JValue<'a>, CruspError> {
        let string_object = JValue::Object(env.new_string(value.to_string())?.into());

        return Ok(JValue::Object(env.new_object(*big_int_class,
                                           "(Ljava/lang/String;)V",
                                           &[string_object])?));
    }

    // receive token, server-ip, server-path, server-port
    fn convert_java_to_rust(env: &JNIEnv, jni_token: JObject, jni_server_ip: JString, jni_server_path: JString, jni_server_port: JString) -> Result<(Token, String, String, u32), CruspError> {
        let repeats = env.get_field(jni_token, "repeats", "I")?.i()?;
        let volume = env.get_field(jni_token, "volume", "I")?.i()?;
        let sleep = env.get_field(jni_token, "sleep", "I")?.i()?;
        let packet_size = env.get_field(jni_token, "packetSize", "I")?.i()?;
        let rate = env.get_field(jni_token, "rate", "F")?.f()?;
        let timeout = env.get_field(jni_token, "timeout", "I")?.i()?;

        let server_ip: String = env.get_string(jni_server_ip)?.into();
        let server_path: String = env.get_string(jni_server_path)?.into();
        let server_port_string: String = env.get_string(jni_server_port)?.into();
        let server_port: u32 = server_port_string.parse::<u32>()?;

        Ok((Token::new(repeats as u16,
                      sleep as u64,
                      volume as u32,
                      rate as f32,
                      packet_size as u32,
                      timeout as u16), server_ip, server_path, server_port))
    }
}

#[cfg(test)]
mod tests {

    #[test]
    fn it_works() {
        assert_eq!(2 + 2, 4);
    }
}