/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __X_JAVAREC_H__
#define __X_JAVAREC_H__

#include "m_pd.h"

typedef struct _javarec {
    t_object x_obj;
    t_symbol *x_sym;
} t_javarec;

void javareceive_setup();
long javareceive_bind(const char *);
int javareceive_unbind(long);

#endif

