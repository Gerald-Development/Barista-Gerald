# Barista-Gerald
[![Project Status: Active](https://www.repostatus.org/badges/latest/active.svg)](https://www.repostatus.org/#active) [![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0) [![Build Status](https://jenkins.voidtech.de/job/Barista%20Projects/job/dev/badge/icon)](https://jenkins.voidtech.de/job/Barista%20Projects/job/dev/badge/icon)

The Java Version of the [Gerald Discord Bot](https://github.com/Elementalmp4/GeraldCore)

Welcome to Gerald! Gerald is just another bot for your lovely Discord server! To give you a rough overview here are some of the main features:
- No Nitro? No problem! Nitrolite uses some magic code to allow you to use your favourite emotes anywhere with Gerald!
- Chat with GeraldAI: We have implemented a ChatAI into our bot to allow users to chat with him in real time. Type \chat enable in a channel to enable it
- Dig Tunnels: You can dig tunnels to channels of other servers (or your own), this allows you to connect two textchannels. The messages of one channel will be mirrored to the channel and vice versa!
- Play with Gerald: The Bot includes implementations of various "mini-games" like "Would you rather", Deathmatches or Fruit Guessing
- Compile Code: You can use Gerald to let an online compiler run some code for you
- Hugs, Headpats, all your favourite actions accompanied by animated gifs!
- Various other commands: Because we have lots of random commands like 8ball, inspiro, random facts, emojify and much, much more!

**Gerald's Prefix: \\** 

To invite Gerald you can just [click here](https://discord.com/api/oauth2/authorize?client_id=555816892141404163&permissions=805694544&scope=bot%20applications.commands)
## About us
Are you having troubles or questions about Gerald or want to talk to the developers? You can join our development Discord server and get the support you need there:

[![Discord Banner 2](https://discordapp.com/api/guilds/729317146127106059/widget.png?style=banner2)](https://discord.gg/mNmgHgjDGz)

## Built with
- [Java 17 Adopt Open JDK](https://adoptopenjdk.net/) - an open source Java JDK
- [Hibernate](https://hibernate.org/) - Java ORM for persistence
- [Spring Boot](https://spring.io/projects/spring-boot) - Dependency Injection and microservices
- [PostgreSQL](https://www.postgresql.org/) - Database
- [JDA](https://github.com/DV8FromTheWorld/JDA) -  Discord API wrapper for Java
- [Twitch4J](https://github.com/twitch4j/twitch4j) - Twitch API wrapper for Java
- [Gavin](https://github.com/Scot-Survivor/GavinFastAPI) - TensorFlow Chat AI built for the [Gavin Discord Bot](https://github.com/Scot-Survivor/GavinTraining)

## Setting up Gerald yourself
You want to host Gerald yourself? No problem just follow the following steps, and you are good to go:
- Download the latest stable build from the [releases tab](https://github.com/Gerald-Development/Barista-Gerald/releases) or check out our developer builds on our [Jenkins](https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/)    ([direct download](https://jenkins.voidtech.de/job/Barista%20Gerald/lastSuccessfulBuild/artifact/target/original-BaristaGerald-0.0.1-SNAPSHOT.jar))
- Create a file with name "GeraldConfig.properties" with following content:
```
        defaultPrefix=COMMAND_PREFIX
        token=DISCORD_TOKEN
        hibernate.User=POSTGRES_USER
        hibernate.Password=POSTGRES_PASSWORD
```

- Set up a local Postgres Database with a database named "BaristaDB" - You may need a database management panel like PGAdmin to do this!
- If you want to use the ChatAPI you also need to grab the [Gavin API](https://github.com/Scot-Survivor/GavinFastAPI) and a suitable TensorFlow model. We will release ours publicly in the future!

We will soon make a follow-up wiki entry for the whole GeraldConfig topic since there are more options to configure if you like!

### Using Docker

You can run Gerald entirely in Docker using Docker Compose. It is configurable with the following environment variables (supports `.env` file):

| <div style="width:150px">Name</div> |      Default       | Description                                                                          |
| :---------------------------------: | :----------------: | :----------------------------------------------------------------------------------- |
|          `DEFAULT_PREFIX`           |       `\\\\`       | Prefix used if server has no custom set. Escape characters need to be escaped twice. |
|           `DISCORD_TOKEN`           |         -          | Token used to log into Discord.                                                      |
|         `DATABASE_USERNAME`         |     `postgres`     | Username used to authenticate with PostgreSQL.                                       |
|         `DATABASE_PASSWORD`         |         -          | Password used to authenticate with PostgreSQL.                                       |
|           `DATABASE_HOST`           | `barista-database` | Hostname of the PostgreSQL server.                                                   |
|           `DATABASE_PORT`           |       `5432`       | Port on which PostgreSQL is listening.                                               |
|         `DATABASE_DATABASE`         |    `BaristaDB`     | Name of the database to use.                                                         |

Note: Docker Compose automatically consumes the `.env` file in its working directory.

## Contributions
Do you want to contribute to the bot yourself? Great news: you can! Just join our Discord and coordinate with us.

We prefer to plan our development work ahead of implementing them so if you want to contribute to the Gerald code base we would be delighted to see you on our discord server. Alternatively you can take a look at the issues tab where we try to plan and document upcoming features and bugfixes.
