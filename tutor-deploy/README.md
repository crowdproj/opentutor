### flashcard-kt ::: deploy

The directory contains [docker-compose](docker-compose-app.yml) files allowing to set up environment.

- flashcards-rabbitmq server. it is required by [:tts-server](../tts-server) and [:tts-client](../tts-client)
- flashcards-db (postgres). it is requires by [:db-pg](../db-pg)
- flashcards-keycloak. Authorization (demo:demo).
- [elk-stack](docker-compose-elk-stack.yml), which consists
  of [elasticsearch](elasticsearch.Dockerfile), [logstash](logstash.Dockerfile) and [kibana](kibana.Dockerfile), also it
  contains `kafka` and `kafdrop`

URLs:
- kafdrop: http://localhost:9000/
- kibana: http://localhost:5601/
- elasticsearch: http://localhost:9200/
- keycloak: http://localhost:8081/
- rabbitmq: http://localhost:15672/
- application: http://localhost:8080/


Example commands to deploy environment:
```
docker-compose -f docker-compose-app.yml flashcards-db up
docker-compose -f docker-compose-app.yml flashcards-keycloak up
docker-compose -f docker-compose-app.yml flashcards-rabbitmq up
docker-compose -f docker-compose-elk-stack.yml -p flashcards-elk-stack up
```