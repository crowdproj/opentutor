#! /bin/sh

docker stop tutor-deploy-flashcards-tts-server-1
docker stop tutor-deploy-flashcards-cards-server-1
docker stop tutor-deploy-flashcards-dictionaries-server-1
docker stop tutor-deploy-flashcards-settings-server-1
docker stop tutor-deploy-flashcards-translation-server-1
docker stop tutor-deploy-flashcards-app-1

docker rm tutor-deploy-flashcards-tts-server-1
docker rm tutor-deploy-flashcards-cards-server-1
docker rm tutor-deploy-flashcards-dictionaries-server-1
docker rm tutor-deploy-flashcards-settings-server-1
docker rm tutor-deploy-flashcards-translation-server-1
docker rm tutor-deploy-flashcards-app-1

docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-dictionaries-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-settings-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-translation-server:2.0.0-snapshot
docker rmi sszuev/open-tutor:2.0.0-snapshot

cd ..
gradle clean build -x test
cd ./app-tts
gradle dockerBuildImage
cd ../app-cards
gradle dockerBuildImage
cd ../app-dictionaries
gradle dockerBuildImage
cd ../app-settings
gradle dockerBuildImage
cd ../app-translation
gradle dockerBuildImage
cd ../app-ktor
gradle dockerBuildImage
cd ../tutor-deploy
