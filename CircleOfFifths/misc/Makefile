FILES = ab.svg a.svg bb.svg b.svg c.svg db.svg d.svg eb.svg e.svg fs.svg \
			f.svg g.svg

.PHONY: all clean

.SUFFIXES:

.SUFFIXES: .svg .png

.svg.png:
	rsvg-convert -a -h 72 -o $@ $<

all: ${FILES:.svg=.png}

clean:
	-rm *.png

