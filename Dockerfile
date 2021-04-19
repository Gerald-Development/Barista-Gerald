FROM maven:3-openjdk-8 AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DfinalName=build

RUN ls -l --block-size=M ./target

FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/original-build.jar ./app.jar

CMD echo -e "defaultPrefix=$DEFAULTPREFIX\n\
token=$TOKEN\n\
hibernate.User=$HIBERNATE_USER\n\
hibernate.Password=$HIBERNATE_PASSWORD\n\
hibernate.ConnectionURL=$HIBERNATE_CONNECTIONURL" > GeraldConfig.properties\
    && java -XX:+UseSerialGC -Xss512k -XX:MaxRAM=72m -Xmx512m -jar app.jar

