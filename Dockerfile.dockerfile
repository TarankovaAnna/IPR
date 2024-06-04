FROM adoptopenjdk/openjdk11:latest*  
VOLUME /tmp*  
ARG JAR_FILE=mock-0.0.1-SNAPSHOT.jar*  
ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64*  
ENV PATH=$PATH:$JAVA_HOME/bin*  
COPY ${JAR_FILE} app.jar*  
ENTRYPOINT ["java","-jar","app.jar"]