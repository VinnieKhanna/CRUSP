#!/bin/sh
cp cargo-config.toml ~/.cargo/config

cargo build --target aarch64-linux-android --release
cargo build --target i686-linux-android --release
cargo build --target armv7-linux-androideabi --release
