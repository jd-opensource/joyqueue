# Contributing to JoyQueue 
Thanks for your help improving the project!

JoyQueue licensed under Apache 2.0 and accepts contributions via Github pull requests. This document outlines some of conventions on building, running, and testing,
the development workflow, commit message formatting.
 
## Building and setting up a development workspace
### Prerequisites

* Java 1.8 or higher
* Maven 3.0 or higher
* Git 
### Get the repository

```
  git clone git@github.com:chubaostream/joyqueue.git
  cd joyqueue
  
```
### Building and testing

Skip test and install 

```
  mvn -Dmaven.test.skip=true -PCompileFrontend install 
```

Run unit test

```
  mvn test 
```

## Running JoyQueue

Lunch JoyQueue with single node is useful for development and testing. Once you install finish, 
binary package(.tar.gz) can be found on joyqueue-distribution server/web module target directory.
Unpack them and run JoyQueue server and portal respectively. 

* Run JoyQueue server 

```
 bin/server-start.sh
 
```
* Run JoyQueue portal 
```
 bin/start.sh 
 
```

## Contribution flow


## Format of the commit message

 