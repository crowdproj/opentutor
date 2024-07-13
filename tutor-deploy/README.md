### flashcard-kt ::: deploy

The directory contains docker-composer files allowing to set up environment.

[docker-compose-app.yml](docker-compose-app.yml):
- flashcards-db (postgres). it is requires by [:db-pg](../db-pg)
- flashcards-keycloak. Authorization (demo:demo).
- flashcards-nats (transport)
- flashcards-tts-server
- flashcards-cards-server
- flashcards-dictionaries-server
- flashcards-app

[docker-compose-elk-stack.yml](docker-compose-elk-stack.yml)

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

Build tts-server & cards-server & dictionaries-server & app images (see `build-images` script):

```shell
docker rm tutor-deploy-flashcards-tts-server-1
docker rm tutor-deploy-flashcards-cards-server-1
docker rm tutor-deploy-flashcards-dictionaries-server-1
docker rm tutor-deploy-flashcards-app-1

docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-dictionaries-server:2.0.0-snapshot
docker rmi sszuev/open-tutor:2.0.0-snapshot

cd ..
gradle clean build -x test
cd ./app-tts
gradle dockerBuildImage
cd ../app-cards
gradle dockerBuildImage
cd ../app-dictionaries
gradle dockerBuildImage
cd ../app-ktor
gradle dockerBuildImage
cd ../tutor-deploy
```

Example commands to deploy environment:
```
docker-compose -f docker-compose-app.yml up flashcards-db flashcards-keycloak flashcards-nats 
docker-compose -f docker-compose-app.yml up flashcards-tts-server flashcards-dictionaries-serve flashcards-cards-server flashcards-app
docker-compose -f docker-compose-elk-stack.yml -p flashcards-elk-stack up
```

#### HTTPS, localhost

1) generate self-signed certificates:

```shell
mkdir tutor-deploy/data/envoy/certs
cd tutor-deploy/data/envoy/certs
openssl genrsa -out localhost.key 2048
openssl req -new -x509 -key localhost.key -out localhost.crt -days 365 -subj "/CN=localhost"
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