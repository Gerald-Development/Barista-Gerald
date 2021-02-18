# Barista-Gerald
___
[![Project Status: WIP](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip) [![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://jenkins.voidtech.de/buildStatus/icon?job=Barista+Gerald)](https://jenkins.voidtech.de/job/Barista%20Gerald/)

The Java Version of the [Gerald Discord Bot](https://github.com/Elementalmp4/GeraldCore)

Welcome to Gerald! Gerald is just another bot for your lovely Discord server! To give you a rough overview here are some of the main features:
- Chat with GeraldAI: We have implemented an ChatAI into our bot to allow users to chat with him in real time. Type /chat enable in a channel to enable it
- Dig Tunnels: You can dig tunnels to channels of other servers, this allows you two connect two textchannels, the messages of one channel will be mirrored to the channel on the other side and vice versa!
- Play with Gerald: The Bot includes implementations of various "mini-games" like "Would you rather", Deathmatches or Fruit Guessing
- Compile Code: You can use Gerald to let an online compiler run some code for you
- Various other commands: Because we have lots of random commands like 8ball, inspiro, random facts, emojify and much much more!

To invite Gerald you can just [click here](https://discord.com/api/oauth2/authorize?client_id=555816892141404163&permissions=805694544&scope=bot)
## About us
___
Are you having troubles or questions about Gerald or want to talk to the developers? You can join our development Discord server and get the support you need there:
**Discord invite link is coming soon, we are currently making our development server ready for the public**
**You can message us on Discord privately in the meantime: Montori#4707 / ElementalMP4#7458**

Optionally you can also send us an [email](mailto:gerald@voidtech.de).

## Built with
___
- [Java 8 Adopt Open JDK](https://adoptopenjdk.net/) - an open source Java JDK
- [Hibernate](https://hibernate.org/) - Java ORM for persistence
- [Spring Boot](https://spring.io/projects/spring-boot) - Dependency Injection and microservices
- [PostgreSQL](https://www.postgresql.org/) - Database
- [JDA](https://github.com/DV8FromTheWorld/JDA) -  Discord API wrapper for Java

## Setting up Gerald yourself
___
You want to host Gerald yourself? No problem just follow the following steps and you are good to go:
- Download the latest build from our [Jenkins](https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/)    ([direct download](https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/artifact/target/original-BaristaGerald-0.0.1-SNAPSHOT.jar))
- Create a file with name "GeraldConfig.properties" with following content:
```
        defaultPrefix=COMMAND_PREFIX
        token=DISCORD_TOKEN
        hibernate.User=POSTGRES_USER
        hibernate.Password=POSTGRES_PASSWORD
        personalityForgeToken=CHAT_API_TOKEN
```
- Set up a local Postgres Database with a database named "BaristaDB"
- If you want to use the ChatAPI you also need to grab a token from [Personality Forge](https://www.personalityforge.com/)

We will soon make a follow up wiki entry for the whole GeraldConfig topic since there are more options to configure if you like!

## Contributions
___
Do you want to contribute to the bot yourself? Great news: you can! Just join our Discord and coordinate with us. 
We are currently not maintaining our GitHub issues due to the fact that we are a small team and mostly write all the code ourselves, if you want to contribute in that way check in after a week or two when we have finally managed to maintain our repo page!
