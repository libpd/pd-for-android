LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := pdnative

LOCAL_CFLAGS := -DPD -DHAVE_UNISTD_H -DHAVE_LIBDL -DUSEAPI_DUMMY

LOCAL_SRC_FILES := \
  z_jni.c z_libpd.c x_libpdreceive.c s_audio_libpd.c \
  g_canvas.c g_graph.c g_text.c g_rtext.c g_array.c g_template.c \
  g_io.c g_scalar.c g_traversal.c g_guiconnect.c g_readwrite.c \
  g_editor.c g_all_guis.c g_bang.c g_hdial.c g_hslider.c g_mycanvas.c \
  g_numbox.c g_toggle.c g_vdial.c g_vslider.c g_vumeter.c m_pd.c \
  m_class.c m_obj.c m_atom.c m_memory.c m_binbuf.c m_conf.c m_glob.c \
  m_sched.c s_main.c s_inter.c s_file.c s_print.c s_loader.c s_path.c \
  s_entry.c s_audio.c s_midi.c s_midi_dummy.c d_soundfile.c \
  d_ugen.c d_ctl.c d_arithmetic.c d_osc.c d_filter.c d_dac.c d_misc.c \
  d_math.c d_fft.c d_fft_mayer.c d_array.c d_global.c d_delay.c \
  d_resample.c x_arithmetic.c x_connective.c x_interface.c x_midi.c \
  x_misc.c x_time.c x_acoustics.c x_net.c x_qlist.c x_gui.c x_list.c

LOCAL_LDLIBS := -ldl -llog

include $(BUILD_SHARED_LIBRARY)

