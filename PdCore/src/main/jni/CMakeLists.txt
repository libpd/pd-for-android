cmake_minimum_required(VERSION 3.4.1)

set(PD_SOURCES
        libpd/pure-data/src/d_arithmetic.c
        libpd/pure-data/src/d_array.c
        libpd/pure-data/src/d_ctl.c
        libpd/pure-data/src/d_dac.c
        libpd/pure-data/src/d_delay.c
        libpd/pure-data/src/d_fft.c
        libpd/pure-data/src/d_fft_fftsg.c
        libpd/pure-data/src/d_filter.c
        libpd/pure-data/src/d_global.c
        libpd/pure-data/src/d_math.c
        libpd/pure-data/src/d_misc.c
        libpd/pure-data/src/d_osc.c
        libpd/pure-data/src/d_resample.c
        libpd/pure-data/src/d_soundfile.c
        libpd/pure-data/src/d_ugen.c
        libpd/pure-data/src/g_all_guis.c
        libpd/pure-data/src/g_array.c
        libpd/pure-data/src/g_bang.c
        libpd/pure-data/src/g_canvas.c
        libpd/pure-data/src/g_clone.c
        libpd/pure-data/src/g_editor.c
        libpd/pure-data/src/g_graph.c
        libpd/pure-data/src/g_guiconnect.c
        libpd/pure-data/src/g_hdial.c
        libpd/pure-data/src/g_hslider.c
        libpd/pure-data/src/g_io.c
        libpd/pure-data/src/g_mycanvas.c
        libpd/pure-data/src/g_numbox.c
        libpd/pure-data/src/g_readwrite.c
        libpd/pure-data/src/g_rtext.c
        libpd/pure-data/src/g_scalar.c
        libpd/pure-data/src/g_template.c
        libpd/pure-data/src/g_text.c
        libpd/pure-data/src/g_toggle.c
        libpd/pure-data/src/g_traversal.c
        libpd/pure-data/src/g_vdial.c
        libpd/pure-data/src/g_vslider.c
        libpd/pure-data/src/g_vumeter.c
        libpd/pure-data/src/m_atom.c
        libpd/pure-data/src/m_binbuf.c
        libpd/pure-data/src/m_class.c
        libpd/pure-data/src/m_conf.c
        libpd/pure-data/src/m_glob.c
        libpd/pure-data/src/m_memory.c
        libpd/pure-data/src/m_obj.c
        libpd/pure-data/src/m_pd.c
        libpd/pure-data/src/m_sched.c
        libpd/pure-data/src/s_audio.c
        libpd/pure-data/src/s_audio_dummy.c
        libpd/pure-data/src/s_inter.c
        libpd/pure-data/src/s_loader.c
        libpd/pure-data/src/s_main.c
        libpd/pure-data/src/s_path.c
        libpd/pure-data/src/s_print.c
        libpd/pure-data/src/s_utf8.c
        libpd/pure-data/src/x_acoustics.c
        libpd/pure-data/src/x_arithmetic.c
        libpd/pure-data/src/x_connective.c
        libpd/pure-data/src/x_gui.c
        libpd/pure-data/src/x_list.c
        libpd/pure-data/src/x_midi.c
        libpd/pure-data/src/x_misc.c
        libpd/pure-data/src/x_net.c
        libpd/pure-data/src/x_array.c
        libpd/pure-data/src/x_time.c
        libpd/pure-data/src/x_interface.c
        libpd/pure-data/src/x_scalar.c
        libpd/pure-data/src/x_text.c
        libpd/pure-data/src/x_vexp.c
        libpd/pure-data/src/x_vexp_if.c
        libpd/pure-data/src/x_vexp_fun.c
        libpd/libpd_wrapper/s_libpdmidi.c
        libpd/libpd_wrapper/x_libpdreceive.c
        libpd/libpd_wrapper/z_libpd.c
        libpd/libpd_wrapper/util/ringbuffer.c
        libpd/libpd_wrapper/util/z_queued.c
        libpd/libpd_wrapper/z_hooks.c
        libpd/libpd_wrapper/util/z_print_util.c
        )

set(OPENSL_SOURCES
        libpd/jni/opensl_stream/opensl_stream.c
        libpd/jni/z_jni_opensl.c
        libpd/libpd_wrapper/z_libpd.c
        libpd/libpd_wrapper/util/z_queued.c
        libpd/libpd_wrapper/util/z_print_util.c
        )

foreach (_BUILD_TYPE RELEASE DEBUG)
    set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY_${_BUILD_TYPE} ${PROJECT_SOURCE_DIR}../../../libs/${ANDROID_ABI})
    set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_${_BUILD_TYPE} ${PROJECT_SOURCE_DIR}../../../libs/${ANDROID_ABI})
    set(CMAKE_RUNTIME_OUTPUT_DIRECTORY_${_BUILD_TYPE} ${PROJECT_SOURCE_DIR}../../../libs/${ANDROID_ABI})
endforeach()

include_directories(libpd/libpd_wrapper)
include_directories(libpd/pure-data/src)
include_directories(libpd/libpd_wrapper/util)
include_directories(libpd/jni)

# Build libpd
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD -DHAVE_UNISTD_H -DHAVE_LIBDL -DUSEAPI_DUMMY -w")
add_library(pd SHARED
            ${PD_SOURCES}
            )
target_link_libraries(pd -ldl -latomic)

# Build plain JNI binary
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-int-to-pointer-cast -Wno-pointer-to-int-cast")
add_library(pdnative SHARED
        libpd/jni/z_jni_plain.c
        )
target_link_libraries(pdnative PUBLIC pd -lOpenSLES -llog)

# Build OpenSL JNI binary
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wno-int-to-pointer-cast -Wno-pointer-to-int-cast")
add_library(pdnativeopensl SHARED
            ${OPENSL_SOURCES}
            )
target_link_libraries(pdnativeopensl PUBLIC pd -lOpenSLES -llog)

# Build libchoice.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(choice SHARED
        libpd/pure-data/extra/choice/choice.c
        )
target_link_libraries(choice PUBLIC pd)

# Build libbonk_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(bonk_tilde SHARED
        libpd/pure-data/extra/bonk~/bonk~.c
        )
target_link_libraries(bonk_tilde PUBLIC pd)

# Build liblrshift_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(liblrshift_tilde SHARED
        libpd/pure-data/extra/lrshift~/lrshift~.c
        )
target_link_libraries(liblrshift_tilde PUBLIC pd)


# Build libfiddle_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(fiddle_tilde SHARED
        libpd/pure-data/extra/fiddle~/fiddle~.c
        )
target_link_libraries(fiddle_tilde PUBLIC pd)


# Build libsigmund_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(sigmund_tilde SHARED
        libpd/pure-data/extra/sigmund~/sigmund~.c
        )
target_link_libraries(sigmund_tilde PUBLIC pd)

# Build libpique.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(pique SHARED
        libpd/pure-data/extra/pique/pique.c
        )
target_link_libraries(pique PUBLIC pd)

# Build libloop_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(loop_tilde SHARED
        libpd/pure-data/extra/loop~/loop~.c
        )
target_link_libraries(loop_tilde PUBLIC pd)

# Build bob_tilde.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(bob_tilde SHARED
        libpd/pure-data/extra/bob~/bob~.c
        )
target_link_libraries(bob_tilde PUBLIC pd)

# Build stdout.so
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DPD")
add_library(stdout SHARED
        libpd/pure-data/extra/stdout/stdout.c
        )
target_link_libraries(stdout PUBLIC pd)
