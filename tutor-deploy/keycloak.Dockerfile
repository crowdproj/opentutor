# https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:24.0.4 as builder

ARG KEYCLOAK_INIT_REALM

ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

ENV KC_DB=postgres
ENV KC_INIT_REALM=${KEYCLOAK_INIT_REALM}

COPY $KC_INIT_REALM /opt/keycloak/data/import/

WORKDIR /opt/keycloak
# for demonstration purposes only, please make sure to use proper certificates in production instead
RUN keytool -genkeypair -storepass password -storetype PKCS12 -keyalg RSA -keysize 2048 -dname "CN=server" -alias server -ext "SAN:c=DNS:localhost,IP:127.0.0.1" -keystore conf/server.keystore
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:24.0.4
COPY --from=builder /opt/keycloak/ /opt/keycloak/

ENTRYPOINT ["/opt/keycloak/bin/kc.sh", "start-dev", "--import-realm", "--db", "postgres", "--log-level", "INFO"]