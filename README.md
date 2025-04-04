# Service - Template
Template for back-end services

# Stack
- Java 21
- Spring Boot
- Spring Security
- Postgres

# How to run
    - Set "JWT_SECRET_KEY" env variable which should be a hash

# Details

- By default, it is necessary to implement a login system. If the system is not going to use login, it is necessary to release the routes

- The ***GreetingController*** controller is only used to test the login system and can be removed

- The CORS config should probably be changed to suit the client that will consume the service
