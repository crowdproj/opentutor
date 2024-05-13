@echo off

docker rm tutor-deploy-flashcards-tts-server-1
docker rm tutor-deploy-flashcards-cards-server-1
docker rm tutor-deploy-flashcards-dictionaries-server-1
docker rm tutor-deploy-flashcards-app-1

docker rmi sszuev/open-tutor-tts-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-cards-server:2.0.0-snapshot
docker rmi sszuev/open-tutor-dictionaries-server:2.0.0-snapshot
docker rmi sszuev/open-tutor:2.0.0-snapshot

cd ..

call gradle clean build -x test

cd app-tts
call gradle dockerBuildImage

cd ../app-cards
call gradle dockerBuildImage

cd ../app-dictionaries
call gradle dockerBuildImage

cd ../app-ktor
call gradle dockerBuildImage

cd ../tutor-deploy

pause