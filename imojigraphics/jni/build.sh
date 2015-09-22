#!/bin/sh

../../../../android-ndk-r10d/ndk-build NDK_DEBUG=0 && rm -vRf ../src/main/jniLibs/* && cp -vR ../libs/{armeabi*,x86} ../src/main/jniLibs/
