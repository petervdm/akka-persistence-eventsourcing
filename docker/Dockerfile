# EventStore
# VERSION               0.0.1

FROM      ubuntu:14.04.2
MAINTAINER Peter van der Merwe <peter@vandermerwe.co.za>

ENV ES_VERSION 3.2.2

ADD http://download.geteventstore.com/binaries/EventStore-OSS-Ubuntu-v$ES_VERSION.tar.gz /tmp/
RUN tar xfz /tmp/EventStore-OSS-Ubuntu-v$ES_VERSION.tar.gz -C /opt

EXPOSE 2113
EXPOSE 1113

VOLUME /data/db
VOLUME /data/logs

ENV EVENTSTORE_MAX_MEM_TABLE_SIZE 100000
ENV EVENTSTORE_WORKER_THREADS 12

WORKDIR /opt/EventStore-OSS-Ubuntu-v$ES_VERSION

CMD ./run-node.sh --ext-http-prefixes=http://*:2113/ --ext-ip=0.0.0.0 \
    --db /data/db --log /data/logs --run-projections=all