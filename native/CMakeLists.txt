# libopencvkt: the C shim (every modules/*/native/shim.cpp) + JNI glue (every modules/*/native/jni.cpp)
# on top of a static OpenCV built from the `opencv/` submodule.
#
# Don't call this directly - use scripts/build-native.sh <target>, which first builds/installs OpenCV and then:
#   - target `opencvkt_shim` (STATIC)  -> merged with OpenCV's archives into prebuilt/<konan_target>/libopencvkt.a
#                                         (embedded into the Kotlin/Native klib of modules/core)
#   - target `opencvkt`      (SHARED)  -> prebuilt/jni/<platform>/libopencvkt.{so,dylib,dll} (JVM desktop + Android)
cmake_minimum_required(VERSION 3.16)
project(opencvkt LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)
set(CMAKE_CXX_VISIBILITY_PRESET hidden)
set(CMAKE_C_VISIBILITY_PRESET hidden)
set(CMAKE_VISIBILITY_INLINES_HIDDEN ON)

option(OPENCVKT_BUILD_JNI "Build the JNI shared library (JVM desktop / Android targets)" ON)
set(OPENCVKT_JNI_INCLUDE_DIRS "" CACHE STRING "Directories containing jni.h / jni_md.h (not needed for Android)")

find_package(OpenCV REQUIRED CONFIG)

get_filename_component(OPENCVKT_REPO_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/.." ABSOLUTE)

# Discover per-module native sources: modules/<m>/{include,native/shim.cpp,native/jni.cpp}
file(GLOB _module_dirs LIST_DIRECTORIES true "${OPENCVKT_REPO_ROOT}/modules/*")
set(OPENCVKT_INCLUDE_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/include")
set(OPENCVKT_SHIM_SOURCES "")
set(OPENCVKT_JNI_SOURCES "")
foreach(dir IN LISTS _module_dirs)
    if(EXISTS "${dir}/include")
        list(APPEND OPENCVKT_INCLUDE_DIRS "${dir}/include")
    endif()
    if(EXISTS "${dir}/native/shim.cpp")
        list(APPEND OPENCVKT_SHIM_SOURCES "${dir}/native/shim.cpp")
    endif()
    if(EXISTS "${dir}/native/jni.cpp")
        list(APPEND OPENCVKT_JNI_SOURCES "${dir}/native/jni.cpp")
    endif()
endforeach()
message(STATUS "opencv-kt shim sources: ${OPENCVKT_SHIM_SOURCES}")

# ---------------- C shim (static) ----------------
add_library(opencvkt_shim STATIC ${OPENCVKT_SHIM_SOURCES})
target_include_directories(opencvkt_shim PUBLIC ${OPENCVKT_INCLUDE_DIRS})
target_link_libraries(opencvkt_shim PUBLIC ${OpenCV_LIBS})

# ---------------- JNI (shared) ----------------
if(OPENCVKT_BUILD_JNI)
    add_library(opencvkt SHARED ${OPENCVKT_JNI_SOURCES})
    target_compile_definitions(opencvkt PRIVATE OCVKT_BUILDING_SHARED)
    target_link_libraries(opencvkt PRIVATE opencvkt_shim)
    if(NOT ANDROID)
        if(NOT OPENCVKT_JNI_INCLUDE_DIRS)
            message(FATAL_ERROR "Set OPENCVKT_JNI_INCLUDE_DIRS to the JDK's include dirs (jni.h + jni_md.h)")
        endif()
        target_include_directories(opencvkt PRIVATE ${OPENCVKT_JNI_INCLUDE_DIRS})
    endif()

    if(ANDROID)
        target_link_libraries(opencvkt PRIVATE log)
        # 16 KB page-size compatibility (required for Android 15+ devices on Play).
        target_link_options(opencvkt PRIVATE "-Wl,-z,max-page-size=16384" "-Wl,--exclude-libs,ALL")
    elseif(APPLE)
        target_link_options(opencvkt PRIVATE "-Wl,-dead_strip")
    elseif(MINGW)
        set_target_properties(opencvkt PROPERTIES PREFIX "")  # System.mapLibraryName("opencvkt") == "opencvkt.dll"
        target_link_options(opencvkt PRIVATE -static -static-libgcc -static-libstdc++)
    elseif(UNIX)
        # Self-contained .so: only libc/libm/libdl/libpthread stay dynamic.
        target_link_options(opencvkt PRIVATE -static-libstdc++ -static-libgcc "-Wl,--exclude-libs,ALL" "-Wl,--gc-sections")
    endif()
endif()
