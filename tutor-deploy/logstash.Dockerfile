FROM logstash:7.10.1

ADD ./logstash.conf /usr/share/logstash/pipeline/10-mp-logs.conf
ADD ./logstash.yml /usr/share/logstash/config/logstash.yml

RUN bin/logstash-plugin install logstash-filter-rest
