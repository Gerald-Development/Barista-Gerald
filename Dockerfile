FROM maven:3-openjdk-8 AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src
RUN mvn clean package -DfinalName=build

RUN ls -l --block-size=M ./target

FROM openjdk:8-jdk-alpine

WORKDIR /app
COPY --from=builder /app/target/original-build.jar ./app.jar

COPY ./entrypoint.sh entrypoint.sh

RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["./entrypoint.sh"]

