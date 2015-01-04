COMPONENTS = fuseki xmpp postgres

all: compile images
	mkdir -p /tmp/{incoming,www,tdb}

images: $(patsubst %, build-%, $(COMPONENTS))

thirdparty:
	make -C thirdparty

compile:
	mvn compile install

build-% : %/Dockerfile
	sudo docker build -t farley-`dirname $<` `dirname $<` 

publish:
	sudo docker tag farley-xmpp docker.gedanken.org:5000/farley-xmpp
	sudo docker push docker.gedanken.org:5000/farley-xmpp

.PHONY: thirdparty

