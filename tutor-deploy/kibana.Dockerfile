FROM kibana:7.10.1
WORKDIR /usr/share/kibana
COPY ./kibana.yml config/