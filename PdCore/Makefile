.PHONY: all

all:
	cd bin && javah -o ../jni/src/z_jni.h org.puredata.core.PdBase
	ndk-build
	mv libs/armeabi/libchoice.so res/raw/choice.pd_linux
	mv libs/armeabi/libbonk~.so res/raw/bonk~.pd_linux
	mv libs/armeabi/liblrshift~.so res/raw/lrshift~.pd_linux
	mv libs/armeabi/libfiddle~.so res/raw/fiddle~.pd_linux
	mv libs/armeabi/libsigmund~.so res/raw/sigmund~.pd_linux
	mv libs/armeabi/libpique.so res/raw/pique.pd_linux
	mv libs/armeabi/libloop~.so res/raw/loop~.pd_linux
	mv libs/armeabi/libexpr.so res/raw/expr.pd_linux
	cp res/raw/expr.pd_linux res/raw/expr~.pd_linux
	cp res/raw/expr.pd_linux res/raw/fexpr~.pd_linux
	cd res/raw && zip extra.zip *.pd_linux && rm *.pd_linux

