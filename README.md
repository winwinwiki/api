# api

### Circle Build Status

Dev  [![CircleCI](https://circleci.com/gh/winwinwiki/api/tree/dev.svg?style=svg)](https://circleci.com/gh/winwinwiki/api/tree/dev)

Prod  [![CircleCI](https://circleci.com/gh/winwinwiki/api/tree/master.svg?style=svg)](https://circleci.com/gh/winwinwiki/api/tree/master)

## Development

### Setting up dependencies:

* install Java Development Kit 10:
* install maven: [setup instructions](https://maven.apache.org/install.html)
* install lombok plugin for your IDE: [setup documentation](https://projectlombok.org/setup/overview)

### Building project

* Starting application server in local

```
$ export WINWIN_DB_HOST=localhost
$ export WINWIN_DB_NAME=winwindb
$ export WINWIN_DB_USER=winwindbuser
$ export WINWIN_DB_PASSWORD=winwindbpassword
$ ./mvnw spring-boot:run
```

The service should be up on port 80. all apis are listed in http://localhost:80/swagger-ui.html

## Deployment

### Setup dependencies

* Install Java

``` $ sudo yum install java-1.8.0-openjdk.x86_64 ```

* update and copy ```scripts/winwin-env.sh``` in ```/etc/profile.d/winwin-env.sh``` and run ```$ source /etc/profile.d/winwin-env.sh``` to update environment variables in EC2 linux machines

### Starting server on EC2 linux machines

* Copy ```scripts/start_server.sh``` , ``` scripts/stop_server.sh ``` & ``` winwin-service ``` in ```/home/ec2-user``` and run following command
 

```
$ sudo mv winwin-service /etc/init.d/winwin-service
$ chmod +x /etc/init.d/winwin-service
$ sudo sed -i -e 's/\r//g' /etc/init.d/winwin-service
$ chmod +x start_server.sh
$ chmod +x stop_server.sh
$ sudo ./start_server.sh
```

### Stopping server on EC2 linux machines

* Run following command

```
$ sudo ./stop_server.sh
```
