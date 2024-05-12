### flashcard-kt ::: deploy

The directory contains docker-composer files allowing to set up environment.

[docker-compose-app.yml](docker-compose-app.yml):
- flashcards-rabbitmq server. it is required by [:tts-server](../tts-server) and [:tts-client](../tts-client)
- flashcards-db (postgres). it is requires by [:db-pg](../db-pg)
- flashcards-keycloak. Authorization (demo:demo).
- tts-server
- cards-server

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

Build tts-server & cards-server & app images:

```shell
docker rm tutor-deploy-flashcards-tts-server-1
docker rm tutor-deploy-flashcards-cards-server-1
docker rm tutor-deploy-flashcards-app-1

docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
docker rmi sszuev/open-tutor:2.0.0-snapshot

cd ..
gradle clean build -x test
cd ./app-tts
gradle dockerBuildImage
cd ../app-cards
gradle dockerBuildImage
cd ../app-ktor
gradle dockerBuildImage
cd ../tutor-deploy
```

Example commands to deploy environment:
```
docker-compose -f docker-compose-app.yml up flashcards-db flashcards-keycloak flashcards-rabbitmq 
docker-compose -f docker-compose-app.yml up flashcards-tts-server
docker-compose -f docker-compose-app.yml up flashcards-app
docker-compose -f docker-compose-elk-stack.yml -p flashcards-elk-stack up
```