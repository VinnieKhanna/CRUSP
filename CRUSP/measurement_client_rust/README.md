# Measurement Client

## Documentation   

## Build

This cross-compiling toolchain is based on https://mozilla.github.io/firefox-browser-architecture/experiments/2017-09-21-rust-on-android.html

### Preparations (only first time)

1. Install Android SDK Tools
    1. Install NDK (via SDK-Tools in Android Studio)
    2. Install CMake (via SDK-Tools in Android Studio)
    3. Install LLDB (via SDK-Tools in Android Studio)
2. Define Paths in `.bashrc`.
    Look up all the paths by yourself.
    If you install the SDK and NDK via Android Studio, the first to paths should be valid.
    It is important that the `MEASUREMENT_PROJECT_HOME` path variable points to the `abe` project.
    ```bash
   export ANDROID_HOME=/home/$USER/Android/Sdk
   export NDK_HOME=$ANDROID_HOME/ndk-bundle 
   export MEASUREMENT_PROJECT_HOME=/home/whofer/workspace/abe
   ``` 
3. Reload `.bashrc`
    ```bash
   source ~/.bashrc 
   ```
4. Install Rust
5. Add the created Android architectures to rustup so we can use them during cross compilation
    ```bash
   rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android 
   ```
5. Go into the `measurement_client_rust` folder in your project and create the standalone NDKs. 
    This script also creates the symbolic links for Android.
    ```bash
    ./create_ndk_and_ln.sh
   ```   
7. Replace the path to the linker in `cargo-config.toml` with your path.
8. Build the client (see next chapter)
9. Create symbolic links for Android (just needed the first time)
    ```bash
   ./create_symbolic_link.sh
   ```
   
### Building for Android

Execute the `compile_rust_client.sh` script in `measurement_client_rust`
```bash
./compile_rust_client.sh
```

### FAQ
#### How can I solve the error when cross-compiling backtrace-sys?
Error: "Failed to find tool. Is `arm-linux-androideabi-clang` installed?"

To solve this problem, add the toolchains to the path, e.g.
```bash
export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/arm/bin:$PATH"
export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/arm64/bin:$PATH"
export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/x86/bin:$PATH"
```

If still not working, execute the make-toolchain commands again:
```bash
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch arm64 --install-dir NDK/arm64
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch arm --install-dir NDK/arm
${NDK_HOME}/build/tools/make_standalone_toolchain.py --api 26 --arch x86 --install-dir NDK/x86
```

## Overview

Link to Overview: [README-OVERVIEW](../README.md)