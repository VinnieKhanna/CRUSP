package at.ac.tuwien.nt.abe.measurement;

import at.ac.tuwien.nt.abe.model.CruspToken;
import at.ac.tuwien.nt.abe.model.MeasurementResult;

public class RustClient {

    /**
     * @param jni_crusp_token
     * @return the result of the measurement.
     * If there was an error in the measurement, the result contains a field called error with furthter information
     * If there was another error when translating from Java->Rust or Rust->Java, the result can be null
     * <p>
     * the 'native' qualifier declares that a call will be made to a native library written in Rust
     */

    private static native MeasurementResult downlink(final CruspToken jni_crusp_token, final String jni_server_ip, final String jni_server_path, final String jni_server_port);

    private static native MeasurementResult uplink(final CruspToken jni_crusp_token, final String jni_server_ip, final String jni_server_path, final String jni_server_port);

    public MeasurementResult executeDownlinkMeasurement(CruspToken token, String ip, String path, String port) {
        return downlink(token, ip, path, port);
    }

    public MeasurementResult executeUplinkMeasurement(CruspToken token, String ip, String path, String port) {
        return uplink(token, ip, path, port);
    }
}
