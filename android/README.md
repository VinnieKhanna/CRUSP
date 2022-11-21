# Install APK on Mobile Phone

Go to the `release` folder in the `android` -> `app` and copy the `app-release.apk` on your mobile phone. 
It is important to allow `unknown sources` in order to install the app. 
If you want to run the app via `Android Studio`, you need to activate the `Developer Options`.  

# Building the Rust Library 

Build and Deploying the Rust Library on Android is based on [this](https://mozilla.github.io/firefox-browser-architecture/experiments/2017-09-21-rust-on-android.html) guide.

## Install Android Studio

## Install NDK (Native Development Kit) in Android Studio

Go to _Android Studio > Preferences > Appearance & Behaviour > Android SDK > SDK Tools_.
Check the following options for installation and click **OK**

* Android SDK Tools
* NDK
* Cmake
* LLDB

Once the NDK and associated tools have been installed, we need to set a few environment variables, first for the SDK path and the second for the NDK path. Set the following envvars:

```
export ANDROID_HOME="/home/$USER/Android/Sdk"
export NDK_HOME="$ANDROID_HOME/ndk-bundle"
```

## Install Rust

## Create Standalone NDKs

The next step is to create standalone versions of the NDK for us to compile against. We need to do this for each of the architectures we want to compile against. We will be using the make_standalone_toolchain.py script inside the main Android NDK in order to do this. First create a directory for our project.

```
mkdir measurement_client_rust
cd measurement_client_rust
```

Now let’s create our standalone NDKs. There is no need to be inside the NDK directory once you have created it to do this.

```
mkdir NDK
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch arm64 --install-dir NDK/arm64
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch arm --install-dir NDK/arm
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch x86 --install-dir NDK/x86
```

Create a new file, `cargo-config.toml`. This file will tell cargo where to look for the NDKs during cross compilation. Add the following content to the file, remembering to replace instances of `<project path>` with the path to your project directory.

```
[target.aarch64-linux-android]
ar = "workspace/abe_microservices/measurement_client_rust/NDK/arm64/bin/aarch64-linux-android-ar"
linker = "workspace/abe_microservices/measurement_client_rust/NDK/arm64/bin/aarch64-linux-android-clang"

[target.armv7-linux-androideabi]
ar = "workspace/abe_microservices/measurement_client_rust/NDK/arm/bin/arm-linux-androideabi-ar"
linker = "workspace/abe_microservices/measurement_client_rust/NDK/arm/bin/arm-linux-androideabi-clang"

[target.i686-linux-android]
ar = "../measurement_client_rust/NDK/x86/bin/i686-linux-android-ar"
linker = "workspace/abe_microservices/measurement_client_rust/NDK/x86/bin/i686-linux-android-clang"
```
## Make our SDK visible for cargo
In order for cargo to see our new SDK’s we need to copy this config file to our `.cargo` directory like this:

```
cp cargo-config.toml ~/.cargo/config
```

## Add Android architectures to rustup
Let’s go ahead and add our newly created Android architectures to **rustup** so we can use them during cross compilation:

```
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android
```

## Create Rust library

Now we’re all set up and we’re ready to start. Let’s create the `lib` directory. If you’ve already created a Rust project from following the iOS post, you don’t need to do it again.

```
cargo init measurement_client_rust --lib
mkdir android
```

`cargo init measurement_client_rust --lib` sets up a new Rust project with its default files and directories in a directory called rust. In this directory is a file called Cargo.toml, which is the package manager descriptor file, and there is be a subdirectory, src, which contains a file called lib.rs. This will contain the Rust code that we will be executing.

Our Rust project here is a super simple Hello World library. It contains a function rust_greeting that takes a string argument and return a greeting including that argument. Therefore, if the argument is “world”, the returned string is “Hello world”.

Open `measurement_client_rust/src/lib.rs` and enter the following code.
```
use std::os::raw::{c_char};
use std::ffi::{CString, CStr};

#[no_mangle]
pub extern fn rust_greeting(to: *const c_char) -> *mut c_char {
    let c_str = unsafe { CStr::from_ptr(to) };
    let recipient = match c_str.to_str() {
        Err(_) => "there",
        Ok(string) => string,
    };

    CString::new("Hello ".to_owned() + recipient).unwrap().into_raw()
}
```

As we will be calling this library from non-Rust code, we will actually be calling it through a C bridge. `#[no_mangle]` tells the compiler not to mangle the function name as it usually does by default, ensuring our function name is exported as if it had been written in C.

`extern` tells the Rust compiler that this function will be called from outside of Rust and to therefore ensure that it is compiled using C calling conventions.

The string that `rust_greeting` accepts is a pointer to a C char array. We have to then convert the string from a C string to a Rust str. First we create a CStr object from the pointer. We then convert it to a str and check the result. If an errorMessage has occurred, then no arg was provided and we substitute there, otherwise we use the value of the provided string. We then append the provided string on the end of our greeting string to create our return string. The return string is then converted into a CString and passed back into C code.

## Create a new Android Project 

Create a new Android Project with an empty activity.

Application name: `abe`

Company Domain: `at.ac.tuwien.nt`

API Version: 26

## Create a class in Android

Go to _File > New > Java Class_. Name your class `RustGreetings` and click OK.

In your new class file, add the following code. Here we are defining the native interface to our Rust library and calling it greeting, with the same signature. The sayHello method simply makes a call to that native function.

```
public class RustGreetings {

    private static native String greeting(final String pattern);

    public String sayHello(String to) {
        return greeting(to);
    }
}
```

## Expose our function through JNI

For Android we want to expose our functions through JNI. The way that JNI constructs the name of the function that it will call is `Java_<domain>_<class>_<methodname>`. In the case of the method, `greeting` that we have declared here, the function in our Rust library that JNI will attempt to call will be `Java_at_ac_tuwien_nt_abe_RustGreetings_greeting`. This is the reason why we created our Android project and Java wrapper class before adding any JNI code to the Rust library. We needed to know what the domain, class and function name were before we could construct the right JNI function name in Rust. Let’s head back over to our Rust project and create the partner code.

Open `measurement_server_client/src/lib.rs`. At the bottom of the file add the following code:

```
/// Expose the JNI interface for android below
#[cfg(target_os="android")]
#[allow(non_snake_case)]
pub mod android {
    extern crate jni;

    use super::*;
    use self::jni::JNIEnv;
    use self::jni::objects::{JClass, JString};
    use self::jni::sys::{jstring};

    #[no_mangle]
    pub unsafe extern fn Java_com_mozilla_greetings_RustGreetings_greeting(env: JNIEnv, _: JClass, java_pattern: JString) -> jstring {
        // Our Java companion code might pass-in "world" as a string, hence the name.
        let world = rust_greeting(env.get_string(java_pattern).expect("invalid pattern string").as_ptr());
        // Retake pointer so that we can use it below and allow memory to be freed when it goes out of scope.
        let world_ptr = CString::from_raw(world);
        let output = env.new_string(world_ptr.to_str().unwrap()).expect("Couldn't create java string!");

        output.into_inner()
    }
}
```

For details look into the original [guide](https://mozilla.github.io/firefox-browser-architecture/experiments/2017-09-21-rust-on-android.html).

## Include jni crate

We declared that we needed the jni crate, that means we need to include the crate in the `Cargo.toml` file. Open it up and add the following between the `[package]` and `[lib]` declarations.


```
[target.'cfg(target_os="android")'.dependencies]
jni = { version = "0.5", default-features = false }

[lib]
crate-type = ["dylib"]

```

## Build libraries

```
cargo build --target aarch64-linux-android --release
cargo build --target armv7-linux-androideabi --release
cargo build --target i686-linux-android --release
```

## Link libraries so they can be found from Android

To make it easier and avoid copying the libraries we can create links into android folders.

```
cd ../android/app/src/main
mkdir jniLibs
mkdir jniLibs/arm64
mkdir jniLibs/armeabi
mkdir jniLibs/x86

ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/aarch64-linux-android/release/libmeasurement_client_rust.so jniLibs/arm64/libmeasurement_client_rust.so
ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/armv7-linux-androideabi/release/libmeasurement_client_rust.so jniLibs/armeabi/libmeasurement_client_rust.so
ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/i686-linux-android/release/libmeasurement_client_rust.so jniLibs/x86/libmeasurement_client_rust.so
```

## Load library in Android

Now, head back to Android Studio and open `MainActivity.java`. We need to load our Rust library when the app starts, so add the following lines below the class declaration and before the onCreate method.

```
static {
    System.loadLibrary("measurement_client_rust");
}
```

## Access created Rust-function in Android

Reopen MainActivity.java and amend the onCreate method to call our greetings function.

```
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_greetings);

    RustGreetings g = new RustGreetings();
    String r = g.sayHello("world");
    println(r);
}

```

## Run application

Build and run the app. If this is your first time in Android Studio, you may need to set up a simulator. When choosing/creating your simulator pick one with API 26. When the app starts, Hello world will be printed on your console.

# Release APK

1. Android Studio -> Generate Signed Bundle SDK -> APK 
2. Keystore: 
- /home/whofer/keystore/abe_keystore.jks
- key0
3. Build Variants: debug and release
4. Signature Versions: V2 (Full APK Signature)
5. Click Finish
6. find generated files in the folders `release` and `debug`