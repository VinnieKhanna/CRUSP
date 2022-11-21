#!/usr/bin/env bash

rm -fr ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs # remove previous links

mkdir ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs
mkdir ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/arm64
mkdir ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/armeabi
mkdir ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/x86

ln -s ${MEASUREMENT_PROJECT_HOME}/measurement_client_rust/target/aarch64-linux-android/release/libmeasurement_client_rust.so  ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/arm64/libmeasurement_client_rust.so
ln -s ${MEASUREMENT_PROJECT_HOME}/measurement_client_rust/target/armv7-linux-androideabi/release/libmeasurement_client_rust.so ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/armeabi/libmeasurement_client_rust.so
ln -s ${MEASUREMENT_PROJECT_HOME}/measurement_client_rust/target/i686-linux-android/release/libmeasurement_client_rust.so ${MEASUREMENT_PROJECT_HOME}/android/app/src/main/jniLibs/x86/libmeasurement_client_rust.so