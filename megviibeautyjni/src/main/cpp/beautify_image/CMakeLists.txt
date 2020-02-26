cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_AUTOMOC ON)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_INCLUDE_CURRENT_DIR ON)

include_directories(include)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DEGL_EGLEXT_PROTOTYPES -DGL_GLEXT_PROTOTYPES")

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11 -fexceptions -frtti -flto -fuse-ld=gold -Wl,-O3")

add_library(MegviiBeautifyImage SHARED
    src/bare_format_transform.cpp
 )

find_library( log-lib log )
find_library( GLESv2-lib GLESv2 )
find_library( EGL-lib EGL )
find_library( bitmap-lib log )

target_link_libraries(MegviiBeautifyImage ${bitmap-lib} ${log-lib} ${GLESv2-lib} ${EGL-lib} android jnigraphics)