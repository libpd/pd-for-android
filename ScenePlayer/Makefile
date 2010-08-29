.PHONY: all

all:
	ndk-build
	mv libs/armeabi/librj_zcr~.so res/raw/rj_zcr~.pd_linux
	mv libs/armeabi/librj_accum.so res/raw/rj_accum.pd_linux
	mv libs/armeabi/librj_barkflux_accum~.so res/raw/rj_barkflux_accum~.pd_linux
	mv libs/armeabi/librj_senergy~.so res/raw/rj_senergy~.pd_linux
	mv libs/armeabi/librj_centroid~.so res/raw/rj_centroid~.pd_linux
	cd res/raw && zip externals.zip *.pd_linux && rm *.pd_linux
	mv libs/armeabi-v7a/librj_zcr~.so res/raw/rj_zcr~.pd_linux
	mv libs/armeabi-v7a/librj_accum.so res/raw/rj_accum.pd_linux
	mv libs/armeabi-v7a/librj_barkflux_accum~.so \
						res/raw/rj_barkflux_accum~.pd_linux
	mv libs/armeabi-v7a/librj_senergy~.so res/raw/rj_senergy~.pd_linux
	mv libs/armeabi-v7a/librj_centroid~.so res/raw/rj_centroid~.pd_linux
	cd res/raw && zip externals_v7a.zip *.pd_linux && rm *.pd_linux

