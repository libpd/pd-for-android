/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifdef USEAPI_LIBPD

#include <stdio.h>
#include "s_audio_libpd.h"

int libpd_open_audio(int nin, int nout, int sr) {
  return 0;
}

int libpd_close_audio() {
  return 0;
}

int libpd_send_dacs() {
  return 0;
}

void libpd_getdevs(char *indevlist, int *nindevs, char *outdevlist,
    int *noutdevs, int *canmulti, int maxndev, int devdescsize) {
  sprintf(indevlist, "NONE");
  sprintf(outdevlist, "NONE");
  *nindevs = *noutdevs = 1;
  *canmulti = 0;
}

void libpd_listdevs() {
  // do nothing
}

#endif

