#!/usr/bin/env bash

#rm -rf app/build
#rm -rf sdk/build


_date=`date '+%Y%m%d'`

_name='Meg_Image_'${_date}'_'$1
echo $_name


rm -rf output/*
rm -rf *.zip
rm -rf app/build
rm -rf megviibeautyjni/build

demo_make_file='pack_demo_res/CMakeLists.txt'
megvii_jni_src_main='megviibeautyjni/src/main'
megvii_jni_aar_path='megviibeautyjni/build/outputs/aar'
app_src_main='app/src/main'

output_beautify_image_sdk_dir='output/beautify_image_sdk'
output_facepp_sdk_dir='output/facepp_sdk'
output_demo_dir='output/demo'
ouput_jni_src_main="${output_demo_dir}/${megvii_jni_src_main}"
out_app_src_main="${output_demo_dir}/${app_src_main}"

output_jniaar_dir='output/jniaar'
out_beautify_image_sdk_libs=${output_beautify_image_sdk_dir}/libs
out_beautify_image_sdk_include=${output_beautify_image_sdk_dir}/include

out_facepp_sdk_libs=${output_facepp_sdk_dir}/libs
out_facepp_sdk_include=${output_facepp_sdk_dir}/include

mkdir -p ${output_beautify_image_sdk_dir}
mkdir -p ${output_beautify_image_sdk_dir}/include
mkdir -p ${output_beautify_image_sdk_dir}/libs/armeabi-v7a
mkdir -p ${output_beautify_image_sdk_dir}/libs/arm64-v8a

mkdir -p ${output_facepp_sdk_dir}
mkdir -p ${output_facepp_sdk_dir}/include
mkdir -p ${output_facepp_sdk_dir}/libs/armeabi-v7a
mkdir -p ${output_facepp_sdk_dir}/libs/arm64-v8a
mkdir -p ${output_facepp_sdk_dir}/model

# copy models to jniaar res
cp -r ${app_src_main}/res/raw/dense_model.pack \
${megvii_jni_src_main}/res/raw

cp -r ${app_src_main}/res/raw/detect_model.pack \
${megvii_jni_src_main}/res/raw

cp -r ${app_src_main}/res/raw/mgbeautify_1_2_4_model \
${megvii_jni_src_main}/res/raw

cp -r ${app_src_main}/res/raw/trained_rt_model.dat \
${megvii_jni_src_main}/res/raw

echo 'build the project'
./gradlew clean
./gradlew build


#copy image sdk
cp -r ${megvii_jni_src_main}/cpp/include/beautify_image/   \
${out_beautify_image_sdk_include}/

cp -r ${megvii_jni_src_main}/jniLibs/arm64-v8a/libMegviiBeautifyImage.so  \
${out_beautify_image_sdk_libs}/arm64-v8a/libMegviiBeautifyImage.so

cp -r ${megvii_jni_src_main}/jniLibs/armeabi-v7a/libMegviiBeautifyImage.so  \
${out_beautify_image_sdk_libs}/armeabi-v7a/libMegviiBeautifyImage.so

echo 'copy image sdk success'

#copy gradles
mkdir -p ${output_demo_dir}
cp -r build.gradle ${output_demo_dir}/
cp -r settings.gradle ${output_demo_dir}/
cp -r gradle.properties ${output_demo_dir}/
echo 'copy gradles success'

#copy BeautifyParams.xml
cp -r BeautifyParams.xml ${output_demo_dir}
cp -r readme.txt ${output_demo_dir}

#copy facepp sdk
cp -r ${megvii_jni_src_main}/cpp/include/mg_dense_landmark.h \
${out_facepp_sdk_include}/
cp -r ${megvii_jni_src_main}/jniLibs/arm64-v8a/libmegface.so \
${output_facepp_sdk_dir}/libs/arm64-v8a/
cp -r ${megvii_jni_src_main}/jniLibs/arm64-v8a/libMegviiDlmk.so \
${output_facepp_sdk_dir}/libs/arm64-v8a/
cp -r ${megvii_jni_src_main}/jniLibs/armeabi-v7a/libmegface.so \
${output_facepp_sdk_dir}/libs/armeabi-v7a/
cp -r ${megvii_jni_src_main}/jniLibs/armeabi-v7a/libMegviiDlmk.so \
${output_facepp_sdk_dir}/libs/armeabi-v7a/
cp -r ${app_src_main}/res/raw/dense_model.pack \
${output_facepp_sdk_dir}/model
cp -r ${app_src_main}/res/raw/detect_model.pack \
${output_facepp_sdk_dir}/model

#copy jni libs project
mkdir -p ${ouput_jni_src_main}
cp -r megviibeautyjni/build.gradle  output/demo/megviibeautyjni/build.gradle
cp -r ${megvii_jni_src_main}/       ${ouput_jni_src_main}/
cp -r prebuild/image_sdk/  ${ouput_jni_src_main}/jniLibs/
cp -r ${demo_make_file}  ${ouput_jni_src_main}/cpp/
rm -rf ${ouput_jni_src_main}/cpp/beautify_image
mkdir -p ${ouput_jni_src_main}/cpp/include/beautify_image/include
cp -r ${megvii_jni_src_main}/cpp/beautify_image/include/bare_format_transform.h \
${ouput_jni_src_main}/cpp/include/beautify_image/include
@echo 'copy jni libs success'

#copy app project
mkdir -p ${out_app_src_main}
cp -r app/build.gradle                     output/demo/app/build.gradle
cp -r ${app_src_main}/                      ${out_app_src_main}/
@echo 'app project success'


echo 'copy jni aar'
mkdir -p ${output_jniaar_dir}
cp -r ${megvii_jni_aar_path}/* ${output_jniaar_dir}

echo 'copy jni aar srccess'

out_apk_dir='output/apk'
mkdir -p ${out_apk_dir}
cp -r app/build/outputs/apk/*.apk ${out_apk_dir}/
echo 'copy release apk success'


