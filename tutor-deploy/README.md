### flashcard-kt ::: deploy

The directory contains docker-compose files allowing to set up environment.
- [rabbit-mq server](docker-compose-rabbitmq.yml). it is required by [:tts-server](../tts-server) and [:tts-client](../tts-client)
- [postgres](docker-compose-postgres.yml). it is requires by [:db-pg](../db-pg)
- [keycloak](docker-compose-keycloak.yml). Authorization.
- [elk-stack](docker-compose-elk-stack.yml), which consists from [elasticsearch](elasticsearch.Dockerfile), [logstash](logstash.Dockerfile) and [kibana](kibana.Dockerfile), also it contains kafka and kafdrop

URLs
- kafdrop: http://localhost:9000/
- kibana: http://localhost:5601/
- elasticsearch: http://localhost:9200/
- keycloak: http://localhost:8081/
- application: http://localhost:8080/


Example commands to deploy environment:
```
docker-compose -f docker-compose-keycloak.yml -p flashcards-deploy up
docker-compose -f docker-compose-rabbitmq.yml -p flashcards-deploy up
docker-compose -f docker-compose-postgres.yml -p flashcards-deploy up
docker-compose -f docker-compose-elk-stack.yml -p flashcards-deploy up
docker cp ./pg-data-sample.sql flashcards-db:/tmp
docker exec -it flashcards-db /bin/bash
psql -U dev -d flashcards -a -f /tmp/pg-data-sample.sql
```
Note that after starting container `flashcards-db` and before executing `pg-data-sample.sql`, 
need to run the `:app-ktor` application to init database using the liquibase migration.
Also note, there could be a problem with permissions, see https://stackoverflow.com/questions/44878062/initdb-could-not-change-permissions-of-directory-on-postgresql-container,
do not use ntfs-3g