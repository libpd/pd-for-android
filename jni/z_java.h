/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifndef __Z_JAVA_H__
#define __Z_JAVA_H__

#include <jni.h>
#include <pthread.h>
#include "m_pd.h"

pthread_key_t __envkey; // thread local storage, for safely
                        // caching env pointer

#define GET_ENV   JNIEnv *env = (JNIEnv *) pthread_getspecific(__envkey);
// gets valid env pointer if possible, NULL otherwise

void java_sendBang(const char *);
void java_sendFloat(const char *, float);
void java_sendSymbol(const char *, const char *);
void java_sendList(const char *, int, t_atom *);
void java_sendMessage(const char *, const char *, int, t_atom *);

#endif

