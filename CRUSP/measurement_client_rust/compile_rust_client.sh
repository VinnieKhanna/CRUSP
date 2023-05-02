#!/bin/sh
cp cargo-config.toml ~/.cargo/config

export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/arm/bin:$PATH"
export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/arm64/bin:$PATH"
export PATH="$MEASUREMENT_PROJECT_HOME/measurement_client_rust/NDK/x86/bin:$PATH"

RUSTFLAGS="--emit=asm" cargo build --target aarch64-linux-android --release
RUSTFLAGS="--emit=asm" cargo build --target i686-linux-android --release
RUSTFLAGS="--emit=asm" cargo build --target armv7-linux-androideabi --release

