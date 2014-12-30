all:
	for f in `find . -name "*.pom"` ; do \
		mvn install:install-file -Dfile=`dirname $$f`/`basename $$f .pom`.jar -DpomFile=$$f ; \
        done

