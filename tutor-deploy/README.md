### flashcard-kt ::: deploy

The directory contains docker-composer files allowing to set up environment.

[docker-compose-app.yml](docker-compose-app.yml):
- flashcards-db (postgres). it is requires by [:db-pg](../db-pg)
- flashcards-keycloak. Authorization (demo:demo).
- flashcards-nats (transport)
- flashcards-redis (cache)
- flashcards-envoy
- flashcards-tts-server
- flashcards-cards-server
- flashcards-dictionaries-server
- flashcards-settings-server
- flashcards-translation-server
- flashcards-linguee-api (for local run and testing only)
- flashcards-app

[docker-compose-elk-stack.yml](docker-compose-elk-stack.yml):

- [elasticsearch](elasticsearch.Dockerfile),
- [logstash](logstash.Dockerfile)
- [kibana](kibana.Dockerfile)
- `kafka` and `kafdrop`

URLs:
- kafdrop: http://localhost:9000/
- kibana: http://localhost:5601/
- elasticsearch: http://localhost:9200/
- keycloak: http://localhost:8081/
- rabbitmq: http://localhost:15672/
- application: http://localhost:8080/

For build service's images (tts, cards, dictionaries, settings, translation, app)
use [build-images.bat](build-images.bat) or [build-images.sh](build-images.sh) scripts.

Example commands to deploy environment:
```
docker-compose -f docker-compose-app.yml up flashcards-db flashcards-keycloak flashcards-nats flashcards-envoy 
docker-compose -f docker-compose-elk-stack.yml -p flashcards-elk-stack up
```

#### HTTPS, localhost

1) generate self-signed certificates:

```shell
mkdir tutor-deploy/data/envoy/certs
cd tutor-deploy/data/envoy/certs
openssl genrsa -out server.key 2048
openssl req -new -x509 -key server.key -out server.crt -days 365 -subj "/CN=localhost"
chmod 644 server.crt server.key
```

2) configure [envoy.yml](data/envoy/envoy.yaml):

- set `port_value: 443`
- `issuer: "https://localhost/realms/flashcards-realm"`
- under `filters` add following section:

```yaml
        - filters
          ...
          transport_socket:
            name: envoy.transport_sockets.tls
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
              common_tls_context:
                tls_certificates:
                  - certificate_chain:
                      filename: "/etc/envoy/certs/server.crt"
                    private_key:
                      filename: "/etc/envoy/certs/server.key"
```

3) change [docker-compose-app.yml](docker-compose-app.yml):

- for
  flashcards-keycloak: `KC_HOSTNAME_URL: "https://localhost/"` &  `KC_HOSTNAME_ADMIN_URL: "https://localhost/"` &  `KC_HOSTNAME_PORT: "443"`
- for flashcards-envoy: `ports: - "443:443"` & uncomment `- "${DATA_DIR}/envoy/certs:/etc/envoy/certs"`
- for
  flashcards-app `KEYCLOAK_AUTHORIZE_ADDRESS: "https://localhost"` & `KEYCLOAK_REDIRECT_ADDRESS: "https://localhost"`

4) `docker-compose -f docker-compose-app.yml up`
5) go to `https://localhost/admin/master/console` -> `flashcards-realm` -> `flashcards-client`
6) set `redirect url` to `https://localhost/*`
7) the application should be available via `https://localhost/`