COMPONENTS = fuseki xmpp

all: compile images

images: $(patsubst %, build-%, $(COMPONENTS))

compile:
	mvn compile install

build-% : %/Dockerfile
	sudo docker build -t farley-`dirname $<` `dirname $<` 
