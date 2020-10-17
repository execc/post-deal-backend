FROM openjdk:8-jre-alpine

ENV JAVA_MEM="-Xmx128M"
ENV JAVA_EXT=""

ENV TZ=Europe/Moscow

ADD *.jar app.jar
CMD java $JAVA_MEM $JAVA_EXT -jar /app.jar
EXPOSE 8080
