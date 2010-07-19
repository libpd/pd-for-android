/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __Z_LIBPD_H__
#define __Z_LIBPD_H__

#include "m_pd.h"


void libpd_init();
int libpd_blocksize();
int libpd_init_audio(int, int, int, int);
int libpd_process_short(short *, short *);
int libpd_process_float(float *, float *);
int libpd_process_double(double *, double *);
int libpd_exists(const char *);
void *libpd_bind(const char *);
int libpd_unbind(void *p);

#define MAXMSGLENGTH 32
int libpd_bang(const char *);
int libpd_float(const char *, float);
int libpd_symbol(const char *, const char *);
int libpd_start_message();
void libpd_add_float(float);
void libpd_add_symbol(const char *);
int libpd_finish_list(const char *);
int libpd_finish_message(const char *, const char *);

typedef (*t_libpd_printhook)(const char *);
typedef (*t_libpd_banghook)(const char *);
typedef (*t_libpd_floathook)(const char *, float);
typedef (*t_libpd_symbolhook)(const char *, const char *);
typedef (*t_libpd_listhook)(const char *, int, t_atom *);
typedef (*t_libpd_messagehook)(const char *, const char *, int, t_atom *);
extern t_libpd_printhook libpd_printhook;
extern t_libpd_banghook libpd_banghook;
extern t_libpd_floathook libpd_floathook;
extern t_libpd_symbolhook libpd_symbolhook;
extern t_libpd_listhook libpd_listhook;
extern t_libpd_messagehook libpd_messagehook;

#endif

