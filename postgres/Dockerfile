FROM centos:centos7
MAINTAINER Logan O'Sullivan Bruns <logan@gedanken.org>

RUN yum install -y postgresql-server

RUN mkdir -p /var/log/postgresql && chown postgres /var/log/postgresql

USER postgres

ENV PGDATA /var/lib/pgsql/data

VOLUME /var/lib/pgsql/data

RUN initdb && \
    pg_ctl -w -l /var/log/postgresql/pg.log start && \
    psql --command "CREATE USER farley WITH SUPERUSER PASSWORD 'farley';" && \
    createdb -O farley farley

RUN echo "host all  all    0.0.0.0/0  md5" >> $PGDATA/pg_hba.conf

RUN echo "listen_addresses='*'" >> $PGDATA/postgresql.conf

EXPOSE 5432

CMD pg_ctl -w -l /var/log/postgresql/pg.log start && \
    tail -f /var/log/postgresql/pg.log
