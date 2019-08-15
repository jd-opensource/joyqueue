
FROM openjdk:8-jdk


#RUN apt-get update -y && apt-get install maven -y
#RUN apt-get install -y  --no-install-recommends apt-utils net-tools
#RUN apt-get install vim -y
#RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone

ARG JOYQUEUE_WEB_TARBALL

ADD ${JOYQUEUE_WEB_TARBALL} /

RUN cp -r /joyqueue-web-*  /joyqueue-web

WORKDIR /joyqueue-web

