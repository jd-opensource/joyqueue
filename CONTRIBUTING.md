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

Lunch JoyQueue with single node is useful for development and testing. Once mvn install finished, 
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

A rough outline of what a contribution's workflow looks like:

1. Submit or select an issue you want to handle. 
2. Fork this repo, develop and test your code changes.
3. Submit a pull request against this repo's master branch.
   - Attaching related issue number  
4. Your pull request may be merged once all configured checks pass,including:
   - passed tests in CI.
   - Review from At least one maintainer
      

## Committing 

Rebase commits is preferred to make more readable commit history.

### Commit messages

Commit messages should be in the following format:
   * Describe what is done
   * Use the active voice
   * Capitalize fist word
   * Reference the Github issue by number 
   
Examples

```
  bad:  A new storage metric 
  good: Introduce a new storage monitor metric (#34)
  
```   

 