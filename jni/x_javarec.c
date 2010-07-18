/*
 * Copyright (c) 2010 Peter Brinkmann (peter.brinkmann@gmail.com)
 *
 * For information on usage and redistribution, and for a DISCLAIMER OF ALL
 * WARRANTIES, see the file, "LICENSE.txt," in this distribution.
 */

#ifdef USEAPI_JAVA

#include <stdio.h>
#include "m_pd.h"
#include "z_java.h"
#include "x_javarec.h"

static t_class *javarec_class;

static void javarec_bang(t_javarec *x) {
  java_sendBang(x->x_sym->s_name);
}

static void javarec_float(t_javarec *x, t_float f) {
  java_sendFloat(x->x_sym->s_name, f);
}

static void javarec_symbol(t_javarec *x, t_symbol *s) {
  java_sendSymbol(x->x_sym->s_name, s->s_name);
}

static void javarec_pointer(t_javarec *x, t_gpointer *gp) {
  // just ignore pointers for now...
}

static void javarec_list(t_javarec *x, t_symbol *s, int argc, t_atom *argv) {
  java_sendList(x->x_sym->s_name, argc, argv);
}

static void javarec_anything(t_javarec *x, t_symbol *s,
                int argc, t_atom *argv) {
  java_sendMessage(x->x_sym->s_name, s->s_name, argc, argv);
}

static void *javareceive_new(t_symbol *s) {
  t_javarec *x = (t_javarec *)pd_new(javarec_class);
  x->x_sym = s;
  pd_bind(&x->x_obj.ob_pd, s);
  return x;
}

static void javareceive_free(t_javarec *x) {
    pd_unbind(&x->x_obj.ob_pd, x->x_sym);
}

void javareceive_setup() {
  javarec_class = class_new(gensym("javareceive_zzzzzzzzzzzzzzzzzzz"),
       (t_newmethod)javareceive_new, (t_method)javareceive_free,
       sizeof(t_javarec), CLASS_DEFAULT, A_DEFSYM, 0);
  class_addbang(javarec_class, javarec_bang);
  class_addfloat(javarec_class, javarec_float);
  class_addsymbol(javarec_class, javarec_symbol);
  class_addpointer(javarec_class, javarec_pointer);
  class_addlist(javarec_class, javarec_list);
  class_addanything(javarec_class, javarec_anything);
}

long javareceive_bind(const char *sym) {
  return (long) javareceive_new(gensym(sym));
}

int javareceive_unbind(long p) {
  pd_free((t_pd *)p);
  return 0;
}

#endif
