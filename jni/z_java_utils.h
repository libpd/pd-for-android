/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __Z_JAVA_UTILS_H__
#define __Z_JAVA_UTILS_H__

#define MAXMSGLENGTH 32
void java_utils_init(void (*)(const char *));
int java_utils_open_audio(int, int, int, int);
int java_utils_process_short(short *, short *);
int java_utils_process_float(float *, float *);
int java_utils_process_double(double *, double *);
int java_utils_start_message();
void java_utils_add_float(float);
void java_utils_add_symbol(const char *);
int java_utils_finish_list(const char *);
int java_utils_finish_message(const char *, const char *);
void *java_utils_get_object(const char *);

#endif

