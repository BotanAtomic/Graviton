<p align="center"><IMG SRC="https://i.gyazo.com/760ac25569c32430a3d1817a77e0fd6e.png"></p>

![Build Status](https://img.shields.io/badge/Login-100%20%-green.svg?style=flat)
![Build Status](https://img.shields.io/badge/Game-30%20%-red.svg?style=flat)
![Build Status](https://img.shields.io/badge/Total-65%20%-orange.svg?style=flat)

#What is it ?

Graviton is a 1.29 dofus emulator, developed in Java. He's supported by gradle and is separated in 4 projects

Graviton - Login : is the login server, manages connections

Graviton - Game : is the game server, manages the in-game

Graviton - Defender : [NOT NOW]

Graviton - Manager : for manage Manager and servers with an application

#Dependencies

Graviton contains a lot of util dependencies

Guice by google: for dependency injections

Mina by Apache: for the network

MySQL-Connector by Apache: for JDBC mysql

HikariCP by Zaxxer group : for JBCD mysql pool connection 

Slf4j & Logback for loggers

Lombok : for use @Getter & @Setter

JOOQ : for typesafe SQL query construction and execution.

#Exemple 

_Inject setting with configuration file : @InjectSetting(value)_
```xml
#File config.propreties
server.key = graviton
```

```xml
package graviton;

import graviton.api.InjectSetting;

public class Exemple {
   @InjectSetting("server.key")
   String key;
   
   public Exemple() {
      log.info(key);
      //Write "graviton" in the Console
   }
}
```

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
_Get and set all object whith annotation in class : @Data_
```xml
package graviton;

import lombok.Data;

@Data
public class Exemple {
   private Object first;
   private Object second;
   public Exemple() {
      this.setSecond(getFirst());
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

-> new system for configuration (Injection)

-> cleaner

-> faster

-> using Hikari framwork for database connection

-> More secure :
- encrypt/decrypt all information about database
- encrypt packet before send
- decryption of the received packets

[Game]

&#9989; Deplacement for player

&#9989; Chat with flood checker for mute

&#9989; Command by chat

&#9989; Items 

&#9989; Emotes

&#9989; Guild (60%)

&#9989; Alignement

&#9989; Map actions

&#9989; Player group 

&#9989; Spells (place and level only)

&#9989; Boost of statisctics

&#9989; Monster on map

[Application]

<u>New version of <b>Graviton Manager</b> for manage login and all servers</u>
</p>
<p align="center"><IMG SRC="https://i.gyazo.com/d0fd9654b2c24593b040d1c24d7b9be2.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/1ab31a8c1e2d873b031b3808e834816e.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/775847439d8119cee261a2c7bd60683c.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/46af196de4cef681b3745d1f52fb572d.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/72802a92416a4cc411c325416a5ec117.png"></p>
