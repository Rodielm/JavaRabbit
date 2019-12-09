# java-RabbitMQ-docker-image

This is the same example with rabbitmq but using docker-compose. 

## Consisting of the following services:

* Web server for uploading images
* Rabbitmq services 
* A program as a worker for image processing using opencv
* A program as another worker to receive the results of the images

This infrastructure has been used to start containers that perform image processing and has been used along with RabbitMQ and a Web Server to deploy a full containerized working application in the Master in Web Technologies, Cloud Computing, and Mobile Applications by Juan Gutierrez.
