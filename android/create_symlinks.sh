#!/usr/bin/env bash

ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/aarch64-linux-android/release/libmeasurement_client_rust.so app/src/main/jniLibs/arm64/libmeasurement_client_rust.so
ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/armv7-linux-androideabi/release/libmeasurement_client_rust.so app/src/main/jniLibs/armeabi/libmeasurement_client_rust.so
ln -s /home/whofer/workspace/abe_microservices/measurement_client_rust/target/i686-linux-android/release/libmeasurement_client_rust.so app/src/main/jniLibs/x86/libmeasurement_client_rust.so
