COMPONENTS = fuseki xmpp postgres

all: compile js-tools images
	mkdir -p /tmp/{incoming,www,tdb}

images: $(patsubst %, build-%, $(COMPONENTS))

thirdparty:
	make -C thirdparty

js-tools:
	mkdir -p xmpp/target
	rsync -a js-tools/ xmpp/target/

compile:
	mvn compile install

clean:
	mvn clean

build-% : %/Dockerfile
	sudo docker build -t farley-`dirname $<` `dirname $<` 

publish:
	sudo docker tag farley-xmpp docker.gedanken.org:5000/farley-xmpp
	sudo docker push docker.gedanken.org:5000/farley-xmpp

coverage:
	mvn -Pscala scoverage:report

check-versions:
	mvn versions:display-dependency-updates

.PHONY: thirdparty js-tools

