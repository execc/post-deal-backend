FROM openjdk:8-jre-alpine
MAINTAINER Waves Enterprise <>

ENV JAVA_OPTS="-Xmx256M"

ADD *.jar app.jar
RUN apk --no-cache add curl

RUN chmod +x app.jar
RUN eval $SET_ENV_CMD
CMD ["/bin/sh", "-c", "eval ${SET_ENV_CMD} ; /usr/bin/java -jar app.jar"]