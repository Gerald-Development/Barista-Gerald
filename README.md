# Barista-Gerald
[![Project Status: WIP](https://www.repostatus.org/badges/latest/wip.svg)](https://www.repostatus.org/#wip)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://jenkins.voidtech.de/buildStatus/icon?job=Barista+Gerald)](https://jenkins.voidtech.de/job/Barista%20Gerald/)

The Java Version of the [Gerald Discord Bot](https://github.com/Elementalmp4/GeraldCore)

This project is still WIP but if you'd like to run the bot locally you can fetch the latest build from our jenkins and create a `GeraldConfig.properties` with following content: 

```
defaultPrefix=YOUR_PREFIX_GOES_HERE
hibernate.User=[YourDBUser]
hibernate.Password=[yourDBPassword]
```

and execute the jar
You are required to also have a local postgres database with a created database "BaristaDB" to run the bot.

Are you a maintainer?
To build the project through maven use maven goal `clean package` or for tests only maven goal `clean test`
