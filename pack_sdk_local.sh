#!/usr/bin/env bash

#$1为    1,3,""    打包beautysdk
#$1为    2,3       打包densesdk

if [[ $1 == "1" || $1 == "" || $1 == "3" ]]; then
    echo "start pack beauty sdk stuff"
    pushd `pwd`
    cd ../FacePP_Beautify_SDK/android/jni
    #ndk-build clean
    #ndk-build
    #/Users/megvii/ndklib/android-ndk-r17c/ndk-build clean
    /Users/zhangjianghao/Library/Android/sdk/ndk-bundle/ndk-build -j 8
    #/Users/megvii/ndklib/android-ndk-r17c/ndk-build -j 8
    popd
    cp -rf ../FacePP_Beautify_SDK/android/libs/arm64-v8a/* megviibeautyjni/src/main/jniLibs/arm64-v8a
    cp -rf ../FacePP_Beautify_SDK/android/libs/armeabi-v7a/* megviibeautyjni/src/main/jniLibs/armeabi-v7a
    cp -rf ../FacePP_Beautify_SDK/include/* megviibeautyjni/src/main/cpp/include


    echo "beauty sdk success---------------"

    pushd `pwd`
    cd ../mg-beautify-sdk
    #bash script/creatModel.sh `pwd` MegviiBeauty 1.3.30#dev_5 com.megvii. -1 offline_auth beauty,filter,sticker,trans
    bash script/creatModel.sh `pwd` MegviiBeauty 1.2.4 com.megvii. -1 offline_auth beauty,filter,sticker,trans
    popd
    cp -rf ../mg-beautify-sdk/MODEL_OUT/mgbeautify_1_2_4_model megviibeautyjni/src/main/res/raw
    cp -rf ../mg-beautify-sdk/MODEL_OUT/mgbeautify_1_2_4_model app/src/main/res/raw

    echo "beauty model success---------------"
fi

if [[ $1 == "2"  || $1 == "3" ]]; then
    echo "start pack dnse sdk stuff"
    pushd `pwd`
    cd ../HumanEffects-Set/android_project/dense_landmark
    gradlew  assemblegeneral

    popd
    app_out_path="../HumanEffects-Set/android_project/dense_landmark/app/build/intermediates/transforms/stripDebugSymbol/arm64-v8a/release/0/lib"
    #只需要jni和sdkso
    cp -rf ../HumanEffects-Set/android_project/dense_landmark/app/build/intermediates/transforms/stripDebugSymbol/general/release/0/lib/arm64-v8a/* megviibeautyjni/src/main/jniLibs/arm64-v8a
    cp -rf ../HumanEffects-Set/android_project/dense_landmark/app/build/intermediates/transforms/stripDebugSymbol/general/release/0/lib/armeabi-v7a/* megviibeautyjni/src/main/jniLibs/armeabi-v7a
    third_party="../HumanEffects-Set/third_party/prebuilt/dlmk"
    #也是只需要一部分so
    cp -rf ../HumanEffects-Set/third_party/prebuilt/dlmk/arm64-v8a/*.so  megviibeautyjni/src/main/jniLibs/arm64-v8a
    cp -rf ../HumanEffects-Set/third_party/prebuilt/dlmk/armeabi-v7a/*.so  megviibeautyjni/src/main/jniLibs/armeabi-v7a
    dnese_include="../HumanEffects-Set/src/dense_landmark/src/out_include/general"
    cp -rf ../HumanEffects-Set/src/dense_landmark/src/out_include/general/*  megviibeautyjni/src/main/cpp/include

    echo "dense sdk success---------------"

    cp -rf ../HumanEffects-Set/android_project/dense_landmark/app/src/main/res/raw/*.pack app/src/main/res/raw

    echo "dense model success-----------------"
fi




