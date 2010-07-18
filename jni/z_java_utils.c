/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifdef USEAPI_JAVA

#include <stdlib.h>
#include <stdio.h>
#include <limits.h>
#include "m_pd.h"
#include "s_stuff.h"
#include "x_javarec.h"
#include "z_java_utils.h"

void pd_init();
int sys_startgui(const char *guipath);  // do we really need this?

static int ticks_per_buffer;

void java_utils_init(void (*printhook)(const char *)) {
  // are all these settings necessary?
  sys_printhook = (t_printhook) printhook;
  sys_externalschedlib = 0;
  sys_schedblocksize = DEFDACBLKSIZE;
  sys_printtostderr = 0;
  sys_usestdpath = 0;
  sys_debuglevel = 0;
  sys_verbose = 0;
  sys_noloadbang = 0;
  sys_nogui = 1;
  sys_hipriority = 0;
  sys_nmidiin = 0;
  sys_nmidiout = 0;
  sys_time = 0;
  pd_init();
  javareceive_setup();
  sys_set_audio_api(API_JAVA);
  sys_startgui(NULL);
}

int java_utils_open_audio(int inChans, int outChans, int sampleRate, int tpb) {
  ticks_per_buffer = tpb;
  int indev[MAXAUDIOINDEV], inch[MAXAUDIOINDEV],
       outdev[MAXAUDIOOUTDEV], outch[MAXAUDIOOUTDEV];
  indev[0] = outdev[0] = DEFAULTAUDIODEV;
  inch[0] = inChans;
  outch[0] = outChans;
  sys_set_audio_settings(1, indev, 1, inch,
         1, outdev, 1, outch, sampleRate, -1, 1);
  sched_set_using_audio(SCHED_AUDIO_CALLBACK);
  sys_reopen_audio();
  return 0;
}

static const t_float float_to_short = SHRT_MAX,
                   short_to_float = 1.0 / (t_float) SHRT_MAX;

#define PROCESS(_x, _y) \
  int i, j, k; \
  t_float *p0, *p1; \
  for (i = 0; i < ticks_per_buffer; i++) { \
    for (j = 0, p0 = sys_soundin; j < DEFDACBLKSIZE; j++, p0++) { \
      for (k = 0, p1 = p0; k < sys_inchannels; k++, p1 += DEFDACBLKSIZE) { \
        *p1 = *inBuffer++ _x; \
      } \
    } \
    memset(sys_soundout, 0, sys_outchannels*DEFDACBLKSIZE*sizeof(t_float)); \
    sched_tick(sys_time + sys_time_per_dsp_tick); \
    for (j = 0, p0 = sys_soundout; j < DEFDACBLKSIZE; j++, p0++) { \
      for (k = 0, p1 = p0; k < sys_outchannels; k++, p1 += DEFDACBLKSIZE) { \
        *outBuffer++ = *p1 _y; \
      } \
    } \
  } \
  return 0;

int java_utils_process_short(short *inBuffer, short *outBuffer) {
  PROCESS(* short_to_float, * float_to_short)
}

int java_utils_process_float(float *inBuffer, float *outBuffer) {
  PROCESS(,)
}

int java_utils_process_double(double *inBuffer, double *outBuffer) {
  PROCESS(,)
}

static t_atom argv[MAXMSGLENGTH], *curr;
static int argc;

int java_utils_start_message() {
  argc = 0;
  curr = argv;
  return MAXMSGLENGTH;
}

#define ADD_ARG(f) f(curr, x); curr++; argc++;

void java_utils_add_float(float x) {
  ADD_ARG(SETFLOAT);
}

void java_utils_add_symbol(const char *s) {
  t_symbol *x = gensym(s);
  ADD_ARG(SETSYMBOL);
}

int java_utils_finish_list(const char *recv) {
  t_pd *dest = gensym(recv)->s_thing;
  if (dest == NULL) return -1;
  pd_list(dest, &s_list, argc, argv);
  return 0;
}

int java_utils_finish_message(const char *recv, const char *msg) {
  t_pd *dest = gensym(recv)->s_thing;
  if (dest == NULL) return -1;
  t_symbol *sym = gensym(msg);
  pd_typedmess(dest, sym, argc, argv);
  return 0;
}

void *java_utils_get_object(const char *s) {
  t_pd *x = gensym(s)->s_thing;
  return x;
}

#endif
