# Rancher API Maven plugin

A Maven plugin for interacting with [rancher](http://rancher.com).

## Goal
There is only one goal: stack-deploy which purpose is to delete and/or create 
a new rancher stack thanks to a docker-compose file

## Usage
### pom.xml file
```
<plugin>
    <groupId>io.crowdcode.maven.plugins</groupId>
    <artifactId>rancher-maven-plugin</artifactId>
    <version>1.0.1</version>
    <configuration>
        <dockerComposeFile>src/main/resources/docker-compose.yml</dockerComposeFile>
        <accessKey>${ACCESS_KEY}</accessKey>
        <password>${PASSWORD}</password>
        <skip>false</skip>
        <url>http://${HOST}/v2-beta</url>
        <environment>Default</environment>
        <stack>
            <dockerComposeFile>src/main/resources/docker-compose.yml</dockerComposeFile>
            <name>Stackname</name>
            <description>Stackdiscriptions</description>
            <actions>remove,wait:millis,create</actions>
        </stack>
    </configuration>
</plugin>
```
### Command line
All optons can be overidden by using line arguments:
```
- rancher.accessKey # rancher login
- rancher.password # rancher password
- rancher.url #rancher url (should be http://HOST)(v2-beta will be used)
- rancher.environment # environment
- rancher.skip # enable or disable plugin. Default enable
- rancher.stack.name # Name of the stack to delete/create
- rancher.stack.description # Stack description
- rancher.stack.startOnCreate
- rancher.stack.dockerComposeFile # docker-compose file
- rancher.stack.rancherComposeFile # rancher-compose file
- rancher.stack.actions # actions witch has to do (remove/create/wait:time/verify)
```

Examples:
```
mvn rancher:stack-deploy -Drancher.accessKey=XXXX -Drancher.password=YYYYY -D.....
```

skip: 
if set to true disables creating a Stack. This config option is best used together with a maven property

## Tests
```
mvn clean test
```

## Nice to have
- Convert current unit tests into integration tests because it needs 
infrastructure (Rancher).
- Action: verify: polling state instead fix time
- Check maven parameter provided by user.
- More Debug-Logging.
- Better exception handling.
- Update stacks.
- Update single services in a stack.
