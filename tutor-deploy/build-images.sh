#! /bin/sh

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
