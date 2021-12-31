
all: build-targets
all: build-analysis
all: run-analysis

build-and-run-analysis:
	./build-analysis.sh && ./run-analysis.sh

help:
	@echo "Usage:"
	@echo "\t" make build-targets
	@echo "\t" make build-analysis
	@echo "\t" make run-analysis


build-targets:
	./build-targets.sh

build-analysis:
	./build-analysis.sh

run-analysis:
	./run-analysis.sh



.PHONY: submission
submission:
	mkdir -p submission/
	cp \
		Analysis.java \
		PAVBase.java \
		Lattice.java \
		submission/
	cp README.md     submission/
	cp *.sh submission/
	cp -r target1-pub/  submission/
	-rm submission/target1-pub/*.class
	cp -r target2-mine/ submission/
	-rm submission/target2-mine/*.class
	cp Makefile         submission/
	tar -cvzf submission.tar.gz  submission/


