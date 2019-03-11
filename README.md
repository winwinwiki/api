# api

### Travis Build Status

Dev [![Build Status](https://travis-ci.com/winwinwiki/api.svg?branch=dev)](https://travis-ci.com/winwinwiki/api)

Prod [![Build Status](https://travis-ci.com/winwinwiki/api.svg?branch=prod)](https://travis-ci.com/winwinwiki/api)

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
