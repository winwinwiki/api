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
$ export WINWIN_DB_PORT=5432
$ export WINWIN_ENV=Development

$ export AWS_ACCESS_KEY_ID=<AWS_ACCESS_KEY_ID>
$ export AWS_SECRET_KEY=<AWS_SECRET_KEY>
$ export AWS_REGION=<AWS_REGION>
$ export AWS_REGION2=<AWS_REGION2>

$ export AWS_COGNITO_CLIENT_ID=<AWS_COGNITO_CLIENT_ID>
$ export AWS_COGNITO_USER_POOL_ID=<AWS_COGNITO_USER_POOL_ID>
$ export AWS_COGNITO_ENDPOINT=<AWS_COGNITO_ENDPOINT>
$ export AWS_COGNITO_IDENTITY_POOL_ID=<AWS_COGNITO_IDENTITY_POOL_ID>

$ export AWS_S3_BUCKET=<AWS_S3_BUCKET>

$ export AWS_ES_ENDPOINT=<AWS_ES_ENDPOINT>
$ export AWS_ES_ENDPOINT_PORT=<AWS_ES_ENDPOINT_PORT>
$ export AWS_ES_ENDPOINT_SCHEME=<AWS_ES_ENDPOINT_SCHEME>
$ export AWS_ES_INDEX=<AWS_ES_INDEX>
$ export AWS_ES_INDEX_TYPE=<AWS_ES_INDEX_TYPE>

$ export SLACK_UPLOAD_FILE_API_URL=https://slack.com/api/files.upload
$ export SLACK_AUTH_TOKEN=<SLACK_AUTH_TOKEN>
$ export SLACK_CHAT_POST_MESSAGE_API_URL=https://slack.com/api/chat.postMessage

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
