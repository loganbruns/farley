FROM centos:centos7
MAINTAINER Logan O'Sullivan Bruns <logan@gedanken.org>

RUN yum install -y java-1.7.0-openjdk

WORKDIR /app/models/

RUN curl --remote-name-all \
    http://opennlp.sourceforge.net/models-1.5/en-sent.bin \
    http://opennlp.sourceforge.net/models-1.5/en-token.bin \
    http://opennlp.sourceforge.net/models-1.5/en-parser-chunking.bin

RUN yum install -y deltarpm

RUN yum install -y ImageMagick tar bzip2 wget fontconfig freetype

WORKDIR /app

RUN wget -O - https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-1.9.8-linux-x86_64.tar.bz2 | tar xjf - && mv phantomjs-1.9.8-linux-x86_64 phantomjs

RUN wget ftp://ftp.pbone.net/mirror/ftp5.gwdg.de/pub/opensuse/repositories/X11:/QtDesktop/CentOS_CentOS-6/i686/scantailor-0.9.11.1-5.17.i686.rpm && yum localinstall -y scantailor-0.9.11.1-5.17.i686.rpm && rm scantailor-0.9.11.1-5.17.i686.rpm

WORKDIR /etc/ssl/certs

ADD im.crt /etc/ssl/certs/
RUN printf "changeit\nyes\n" | keytool -importcert -file im.crt -keystore /usr/lib/jvm/java-1.7.0-openjdk-1.7.0.71-2.5.3.1.el7_0.x86_64/jre/lib/security/cacerts

WORKDIR /app

ADD target/xmpp-1.0-SNAPSHOT.jar /app/
ADD target/lib /app/lib/
ADD target/scanner /app/scanner/

CMD java -jar /app/xmpp-1.0-SNAPSHOT.jar
