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

&#9989; Alignement

&#9989; Map actions

&#9989; Player group (30%)

&#9989; Spells (place and level only)

&#9989; Boost of statisctics

[Application]

<u>New version of <b>Graviton Manager</b> for manage login and all servers</u>
</p>
<p align="center"><IMG SRC="https://i.gyazo.com/aee0118d86f139ddc16680c8479f612d.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/4e4524c7d85ee4ac452e66a85890b048.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/d2475817eae6ecd688ec7dbae07163ab.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/9edf640580be98eaf9c46d8f72d18fec.png"></p>
<p align="center"><IMG SRC="https://i.gyazo.com/7ad1ca743573ef2ed453e18382bd6d0b.png"></p>
