# CMake initial cache (-C) for building OpenCV from the `opencv/` submodule as static libraries
# that get merged into libopencvkt. Used by scripts/build-native.sh for every target.
#
# Philosophy: self-contained binaries. Every 3rd-party codec is built from OpenCV's bundled sources,
# nothing is picked up from the build machine, and no GUI / system video framework is required.

# --- modules (keep in sync with modules/* Kotlin modules) ---
set(BUILD_LIST "core,flann,geometry,imgproc,imgcodecs,videoio,highgui,features,calib,stereo,objdetect,dnn,photo,video,ptcloud,stitching" CACHE STRING "")

set(BUILD_SHARED_LIBS OFF CACHE BOOL "")
set(CMAKE_POSITION_INDEPENDENT_CODE ON CACHE BOOL "")
set(CMAKE_BUILD_TYPE Release CACHE STRING "")

# --- things we never need ---
foreach(opt BUILD_TESTS BUILD_PERF_TESTS BUILD_EXAMPLES BUILD_DOCS BUILD_opencv_apps BUILD_JAVA BUILD_FAT_JAVA_LIB
            BUILD_opencv_python3 BUILD_opencv_python_bindings_generator BUILD_opencv_java_bindings_generator
            BUILD_opencv_js BUILD_opencv_js_bindings_generator BUILD_opencv_objc_bindings_generator BUILD_opencv_world
            BUILD_ANDROID_PROJECTS BUILD_ANDROID_EXAMPLES INSTALL_ANDROID_EXAMPLES BUILD_KOTLIN_EXTENSIONS
            INSTALL_TESTS INSTALL_PYTHON_EXAMPLES INSTALL_C_EXAMPLES OPENCV_GENERATE_PKGCONFIG BUILD_IOS_FRAMEWORK)
    set(${opt} OFF CACHE BOOL "")
endforeach()

# --- bundled 3rd-party (built from opencv/3rdparty, statically) ---
set(OPENCV_FORCE_3RDPARTY_BUILD ON CACHE BOOL "")
foreach(opt BUILD_ZLIB BUILD_PNG BUILD_JPEG BUILD_WEBP BUILD_TIFF BUILD_OPENJPEG BUILD_PROTOBUF)
    set(${opt} ON CACHE BOOL "")
endforeach()
foreach(opt WITH_PNG WITH_JPEG WITH_WEBP WITH_TIFF WITH_OPENJPEG WITH_PROTOBUF)
    set(${opt} ON CACHE BOOL "")
endforeach()
set(WITH_SPNG OFF CACHE BOOL "")

# --- system / optional integrations that would make the binary depend on the build host ---
foreach(opt WITH_GTK WITH_QT WITH_OPENGL WITH_VTK WITH_WIN32UI WITH_COCOA
            WITH_FFMPEG WITH_GSTREAMER WITH_V4L WITH_1394 WITH_AVFOUNDATION WITH_MSMF WITH_DSHOW WITH_OBSENSOR
            WITH_CUDA WITH_OPENCL WITH_VA WITH_VA_INTEL WITH_OPENVINO WITH_TBB WITH_OPENMP WITH_HPX
            WITH_IPP WITH_ITT WITH_EIGEN WITH_LAPACK WITH_OPENEXR WITH_JASPER WITH_GDAL WITH_GDCM WITH_IMGCODEC_HDR_DUMMY
            WITH_AVIF WITH_JPEGXL WITH_ADE WITH_KLEIDICV WITH_FASTCV)
    set(${opt} OFF CACHE BOOL "")
endforeach()

# Parallel backend: plain pthreads / GCD / Win32 threads (no TBB/OpenMP runtime to ship).
set(WITH_PTHREADS_PF ON CACHE BOOL "")

# Keep the static archives free of absolute build paths.
set(OPENCV_SKIP_VISIBILITY_HIDDEN OFF CACHE BOOL "")
set(ENABLE_PRECOMPILED_HEADERS OFF CACHE BOOL "")
