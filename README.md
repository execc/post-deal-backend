# deals-app

## Setting up
Before building a project, create a file  `.gradle/gradle.properties`
in you HOME directory.

In this file enter the following properties:
```
mavenUser=<you login in Waves Enterprise Nexus>
mavenPassword=<you password in Waves Enterprise Nexus>
```

Then initialize git repository inside the project by doing the following
commands in project root directory:
```
git init
git add .
git commit -m "Initial commit"
```

## Building (all modules)
This command will build all modules in the project and publish corresponding
applications into a docker registry

`./gradlew clean dockerTag`

Alternatively run the following command to push all docker images (both applications and smart contracts) to remote docker registry
`./gradlew clena dockerPush`

## Running from IDE
Before you run an application you need to start Decentralized Application Platform with the following command in `docker-compose` directory:
```
docker-compose -f docker-compose-base.yml up -d
```
You also need to either:
 - build a smart contract using `./gradlew dockerTag` command to run contracts locally
 - or deploy (push) a smart contract using `./gradlew dockerPush` command to run contracts remotely

If you're using a local node deployment (docker-compose file) you also need to start you nodes
by executing `docker-compose -f docker-compose.yml up -d` command in `docker-compose` directory.

### Pre-existing user names
During code generation a pre-existing user is created with credentials: `user/user`.
You can use that user to login into application and make calls to APIs.
All users are managed via `docker-compose/config/vst-oauth2/user-companies.json` file.