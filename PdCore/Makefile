.PHONY: all

all:
	ndk-build
	mv libs/armeabi-v7a/libchoice.so res/raw/choice.pd_linux
	mv libs/armeabi-v7a/libbonk~.so res/raw/bonk~.pd_linux
	mv libs/armeabi-v7a/liblrshift~.so res/raw/lrshift~.pd_linux
	mv libs/armeabi-v7a/libfiddle~.so res/raw/fiddle~.pd_linux
	mv libs/armeabi-v7a/libsigmund~.so res/raw/sigmund~.pd_linux
	mv libs/armeabi-v7a/libpique.so res/raw/pique.pd_linux
	mv libs/armeabi-v7a/libloop~.so res/raw/loop~.pd_linux
	mv libs/armeabi-v7a/libexpr.so res/raw/expr.pd_linux
	cp res/raw/expr.pd_linux res/raw/expr~.pd_linux
	cp res/raw/expr.pd_linux res/raw/fexpr~.pd_linux
	cd res/raw && zip extra_ext_v7a.zip *.pd_linux && rm *.pd_linux
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
	cd res/raw && zip extra_ext.zip *.pd_linux && rm *.pd_linux

