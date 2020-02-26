如何使用替换SDK 的文件至Demo
1.拷贝打包sdk包中Android->sdk->beautify_image_sdk、beautify_sdk、facepp_sdk ->libs下的so 库至demo中megviibeautyjni(module)->jniLibs下面。
2.拷贝打包sdk包中Android->sdk->beautify_image_sdk、beautify_sdk、facepp_sdk ->model下的模型文件至demo中app主工程->res->raw下面进行同名替换。




如何批量处理图片
1、创建目录/sdcard/megvii_batch_proc，将所有图片放置在此文件夹中；
2、拷贝BeautifyParams.xml文件至1的文件夹中,此文件用于参数设置0.0-5.0;
3、运行app首页的批量处理按钮；
4、等所有的图片处理完，结果保存在/sdcard/DCIM/Camera/megBeautify中。