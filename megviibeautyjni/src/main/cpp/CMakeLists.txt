
cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_AUTOMOC ON)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_INCLUDE_CURRENT_DIR ON)

include_directories(${CMAKE_SOURCE_DIR}/include)

set(jnilibs "${CMAKE_SOURCE_DIR}/../jniLibs")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${jnilibs}/${ANDROID_ABI})

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti -flto -fvisibility=hidden -fuse-ld=gold -Wl,-O3")


add_library(MegviiBeautify-jni SHARED
    megvii_beautify_jni.cpp
    beautify_handler.cpp
    gl_context_handler.cpp
    WorkerThread.cpp)


add_library(beauty SHARED IMPORTED )
set_target_properties(beauty PROPERTIES
    IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libMGBeauty.so")


add_library(MegviiDlmk SHARED IMPORTED )
set_target_properties(MegviiDlmk PROPERTIES
   IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libMegviiDlmk.so")


#add_library(MegviiBeautifyImage SHARED IMPORTED )
#set_target_properties(MegviiBeautifyImage PROPERTIES
#    IMPORTED_LOCATION "${jnilibs}/${ANDROID_ABI}/libMegviiBeautifyImage.so")



find_library( log-lib log )
find_library( GLESv2-lib GLESv2 )
find_library( EGL-lib EGL )
find_library( bitmap-lib log )


target_link_libraries(MegviiBeautify-jni ${bitmap-lib} ${log-lib} beauty  MegviiBeautifyImage MegviiDlmk ${GLESv2-lib} ${EGL-lib} android jnigraphics)


ADD_SUBDIRECTORY(beautify_image)