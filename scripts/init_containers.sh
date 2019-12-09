cd ../deploy
docker-compose up -d
sleep 10
docker-compose stop
cd ../scripts
