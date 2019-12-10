cmake_minimum_required(VERSION 3.4.1)

set (LIBPD_DIR ../../PdCore/src/main/jni/)

# Add the libpd library as a subproject. Since it is an out-of-tree source library we must also
# specify a binary directory
add_subdirectory(${LIBPD_DIR} ../libs/${ANDROID_ABI})
include_directories(${LIBPD_DIR}libpd/pure-data/src)

# Build helloworld.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(helloworld SHARED
        helloworld.c
        )
target_link_libraries(helloworld pd)