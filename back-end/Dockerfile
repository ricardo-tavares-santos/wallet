FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /opt/app
ARG JAR_FILE=target/wallet-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} wallet.jar

ENTRYPOINT ["java","-jar","wallet.jar"]