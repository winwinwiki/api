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

* Starting application server

```
export WINWIN_DB_HOST=localhost
export WINWIN_DB_NAME=winwindb
export WINWIN_DB_USER=winwindbuser
export WINWIN_DB_PASSWORD=winwindbpassword
./mvnw spring-boot:run
```

The service should be up on port 80. all apis are listed in http://localhost:80/swagger-ui.html
