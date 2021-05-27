#!/bin/sh

echo -e "defaultPrefix=${DEFAULTPREFIX} \n\
    token=$TOKEN\n\
    hibernate.User=$HIBERNATE_USER\n\
    hibernate.Password=$HIBERNATE_PASSWORD\n\
    hibernate.ConnectionURL=$HIBERNATE_CONNECTIONURL" >> GeraldConfig.properties

java -XX:+UseSerialGC -Xss512k -XX:MaxRAM=72m -Xmx512m -jar app.jar