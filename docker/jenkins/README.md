
# Jenkins docker build and start

## Docker build 
docker build -t jenkins/jenkins:lts_python35 .


## Docker run 
docker run -p 80:8080 -p 50000:50000 -v /export/docker/jenkins/jenkins_home:/var/jenkins_home -v /var/run/docker.sock:/var/run/docker.sock -v $(which docker):/usr/bin/docker -u0 -d jenkins/jenkins:lts_python35