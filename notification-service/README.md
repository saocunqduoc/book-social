# Notification Service
## MongoDb 
Install Mongo DB Com in Docker Hub

`docker pull bitnami/mongodb:latest`

Start MongoDB at port 27017 with root username and password: root/ root.

`docker run -d --name mongodb -p 27017:27017 -e MONGODB_ROOT_USER=root -e MONGODB_ROOT_PASSWORD=root bitnami/mongodb:latest
`
