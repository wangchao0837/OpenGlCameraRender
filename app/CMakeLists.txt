
cmake_minimum_required(VERSION 3.4.1)

add_subdirectory(src/main/cpp/alignment)
add_library(
        native-lib
        SHARED
        src/main/cpp/native-lib.cpp
        src/main/cpp/FaceTracker.cpp
)

include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)


#add_library(libopencv_java4 SHARED IMPORTED)


#set_target_properties(libopencv_java4 PROPERTIES IMPORTED_LOCATION src/main/cpp/jniLibs/armeabi-v7a/libopencv_java4.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_GLAGS} -L/${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}")

target_link_libraries(
        native-lib
        seeta_fa_lib
        opencv_java4
        android
        log)


##该变量为真时会创建完整版本的Makefile
#set(CMAKE_VERBOSE_MAKEFILE on)
#
##定义变量ocvlibs使后面的命令可以使用定位具体的库文件
#set(opencvlibs "/Users/7invensun/Desktop/opencv/OpenCV-android-sdk/sdk/native/libs")
#
##调用头文件的具体路径
#include_directories(/Users/7invensun/Desktop/opencv/OpenCV-android-sdk/sdk/native/jni/include)
#
##增加我们的动态库
#add_library(libopencv_java4 SHARED IMPORTED)
#
##建立链接
#set_target_properties(libopencv_java4 PROPERTIES IMPORTED_LOCATION
#        "${opencvlibs}/${ANDROID_ABI}/libopencv_java4.so")
#
#
## Creates and names a library, sets it as either STATIC
## or SHARED, and provides the relative paths to its source code.
## You can define multiple libraries, and CMake builds them for you.
## Gradle automatically packages shared libraries with your APK.
#
#add_library( # Sets the name of the library.
#        native-lib
#
#        # Sets the library as a shared library.
#        SHARED
#
#        # Provides a relative path to your source file(s).
#        native-lib.cpp)
#
## Searches for a specified prebuilt library and stores the path as a
## variable. Because CMake includes system libraries in the search path by
## default, you only need to specify the name of the public NDK library
## you want to add. CMake verifies that the library exists before
## completing its build.
#
#find_library( # Sets the name of the path variable.
#        log-lib
#
#        # Specifies the name of the NDK library that
#        # you want CMake to locate.
#        log)
#
## Specifies libraries CMake should link to your target library. You
## can link multiple libraries, such as libraries you define in this
## build script, prebuilt third-party libraries, or system libraries.
#
#target_link_libraries( # Specifies the target library.
#        native-lib
#        libopencv_java4
#        android
#        # Links the target library to the log library
#        # included in the NDK.
#        ${log-lib})