
all:
	ndk-build
	mkdir ../tmp
	cp ../libs/armeabi/librj_accum.so ../tmp/rj_accum.pd_linux
	cp ../libs/armeabi/librj_barkflux_accum~.so ../tmp/rj_barkflux_accum~.pd_linux
	cp ../libs/armeabi/librj_centroid~.so ../tmp/rj_centroid~.pd_linux
	cp ../libs/armeabi/librj_senergy~.so ../tmp/rj_senergy~.pd_linux
	cp ../libs/armeabi/librj_zcr~.so ../tmp/rj_zcr~.pd_linux
	cd ../tmp && zip externals.zip *.pd_linux && mv externals.zip ../res/raw
	rm -rf ../tmp

