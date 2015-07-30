FROM centos:centos7
MAINTAINER Logan O'Sullivan Bruns <logan@gedanken.org>

RUN yum install -y java-1.7.0-openjdk
RUN yum install -y tar ruby

VOLUME /var/jena-juseki

WORKDIR /app

RUN curl http://archive.apache.org/dist/jena/binaries/jena-fuseki1-1.1.2-distribution.tar.gz | tar xzf - && mv jena-fuseki1-1.1.2 jena-fuseki

ENV FUSEKI_HOME /app/jena-fuseki
ENV PATH $FUSEKI_HOME:$PATH

EXPOSE 3030

CMD /app/jena-fuseki/fuseki-server --update --loc=/var/jena-fuseki /ds
