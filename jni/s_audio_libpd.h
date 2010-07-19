/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __S_AUDIO_LIBPD_H__
#define __S_AUDIO_LIBPD_H__

int libpd_open_audio(int, int, int);
int libpd_close_audio();
int libpd_send_dacs();
void libpd_getdevs(char *, int *, char *, int *, int *, int, int);
void libpd_listdevs();

#endif

