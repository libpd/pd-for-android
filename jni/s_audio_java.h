/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __S_AUDIO_JAVA_H__
#define __S_AUDIO_JAVA_H__

int java_open_audio(int, int, int);
int java_close_audio();
int java_send_dacs();
void java_getdevs(char *, int *, char *, int *, int *, int, int);
void java_listdevs();

#endif

