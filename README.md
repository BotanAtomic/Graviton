<p align="center"><IMG SRC="http://i.gyazo.com/cf951ef31bae8edf9f2bcfceab2aebe7.png"></p>

![Build Status](https://img.shields.io/badge/Login-100%20%-green.svg?style=flat)
![Build Status](https://img.shields.io/badge/Game-30%20%-red.svg?style=flat)
![Build Status](https://img.shields.io/badge/Total-65%20%-orange.svg?style=flat)

#What is it ?

Graviton is a 1.29 dofus emulator, developed in Java. He's supported by gradle and is separated in 2 projects

Graviton - Login : is the login server, manages connections

Graviton - Game : is the game server, manages the in-game

#Dependencies

Graviton contains a lot of util dependencies

Guice by google: for dependency injections

Mina by Apache: for the network

MySQL-Connector by Apache: for JDBC mysql

HikariCP by Zaxxer group : for JBCD mysql pool connection 

Slf4j & Logback for loggers

Lombok : for use @Annotation 

#Exemple 

_Get a logger whith annotation in class : @Slf4j_
```xml
package graviton;

import lombok.Slf4j;

@Slf4j
public class Exemple {
   public Exemple() {
      log.info("Class exemple created");
   }
}
```
_All the information of the database are encrypted (*security mode*):_
```xml
public Database(String ip, String name, String user, String pass) {
   dataConfig = new HikariConfig() {
      {
         setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
         addDataSourceProperty("serverName", CryptManager.decrypt(ip));
         addDataSourceProperty("port", 3306);
         addDataSourceProperty("databaseName", CryptManager.decrypt(name));
         addDataSourceProperty("user", CryptManager.decrypt(user));
         addDataSourceProperty("password", CryptManager.decrypt(pass));
      }
   };
}
```

#Last update 

[Login]

-> cleaner

-> faster

-> using Hikari framwork for database connection

-> More secure :
- encrypt/decrypt all information about database
- encrypt packet before send
- decryption of the received packets

[Game]

-> Add deplacement for player

-> Add chat with flood checker for mute

-> Add items

-> Add emote

-> Add map action

-> Add group (30%)

-> Add spells (place and level)

-> Add boost of statisctics

[Application]

<u>New version of <b>Graviton Manager</b> for manage login and all servers</u>

[![](https://i.gyazo.com/280bd5ad0834320e416af3865eb4ef17.png)]<br/>
[![](https://gyazo.com/361eb7676d21e9c360a78381b49955e6)]<br/>


<p align="center">Thank's to Return for his help > <a href="https://github.com/Romain-P/">Romain-P</a></p>
