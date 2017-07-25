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
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <dockerComposeFile>src/main/resources/docker-compose.yml</dockerComposeFile>
        <accessKey>${ACCESS_KEY}</accessKey>
        <password>${PASSWORD}</password>
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
- rancher.accessKey #rancher login
- rancher.password # rancher password
- rancher.url #rancher url (should be http://HOST)(v2-beta will be used)
- rancher.environment # environment
- rancher.stack.name #Name of the stack to delete/create
- rancher.stack.description #Stack description
- rancher.stack.startOnCreate
- rancher.stack.dockerComposeFilePath #docker-compose filepath
- rancher.stack.rancherComposeFilePath #rancher-compose filepath
- rancher.stack.actions #actions witch has to do (remove/create/wait:time)
```

Examples:
```
mvn rancher:stack-deploy -Drancher.accessKey=XXXX -Drancher.password=YYYYY -D.....
```

## Tests
```
mvn clean test
```

## Nice to have