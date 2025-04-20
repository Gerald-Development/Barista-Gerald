FROM maven:3.9-amazoncorretto-17-alpine AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DfinalName=build

FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=builder /app/target/original-build.jar ./app.jar
COPY GeraldConfig.properties /app/GeraldConfig.properties
CMD java -XX:+UseSerialGC -Xss512k -XX:MaxRAM=72m -Xmx512m -jar app.jar

