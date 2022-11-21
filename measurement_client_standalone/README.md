# Setup to run binary on PC

- Build the binary
```bash
cargo build --release
```
In order to build it successfully, make sure that the `~./cargo/config` does NOT exist.


# Setup to run binary on mobile phone:
- Go to the target-folder and select the correct processor type

- Push measurement_client_standalone executeable to phone
```bash
adb push measurement_client_standalone /data/local/tmp/measurement_client_standalone
```
- Push libraries to phone
```bash
adb push deps /data/local/tmp/deps
```

- Push standard-library (in `.rustup` Folder) to phone
```bash
adb push libstd-070325e6597044ee.so /data/local/tmp/deps/libstd-070325e6597044ee.so  
```

- Enter ADB Shell
```bash
adb shell
```
- set Environment Variable on phone
```bash
export LD_LIBRARY_PATH=/data/local/tmp/deps:/vendor/lib*:/system/lib*
```

# Problems

When executing the binary on the phone, the binary is looking for a stdlib which is not in the `deps` folder.
Therefore execution is not working.

# FAQ

## How can I solve the error when cross-compiling backtrace-sys?
Error: "Failed to find tool. Is `arm-linux-androideabi-clang` installed?"

To solve this problem, add the toolchains to the path, e.g.
```bash
export PATH="/home/whofer/workspace/abe_microservices/measurement_client_standalone/NDK/arm/bin:$PATH"
export PATH="/home/whofer/workspace/abe_microservices/measurement_client_standalone/NDK/arm64/bin:$PATH"
export PATH="/home/whofer/workspace/abe_microservices/measurement_client_standalone/NDK/x86/bin:$PATH"
```

## Where do I find the libstd-070325e6597044ee.so?

Go to following folder: `.rustup/toolchains/nightly-x86_64-unknown-linux-gnu/lib/rustlib/armv7-linux-androideabi/lib/libstd-070325e6597044ee.so
`