<p align="center"><IMG SRC="https://i.gyazo.com/760ac25569c32430a3d1817a77e0fd6e.png"></p>

![Build Status](https://img.shields.io/badge/Login-100-green.svg?style=plastic)
![Build Status](https://img.shields.io/badge/Game-50-red.svg?style=plastic)
![Build Status](https://img.shields.io/badge/Total-75-orange.svg?style=plastic)


#What is it ?

Graviton is a 1.29 dofus emulator, developed in Java. He's supported by gradle and is separated in 4 projects

Graviton - Login : is the login server, manages connections

Graviton - Game : is the game server, manages the in-game

Graviton - Bot : is bot using socket for create a multiple connections to server

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

_Inject setting with configuration file : @InjectSetting(value)_ (Thank's Return)
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
      System.err.println(key); //Write "graviton" in the Console
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
<details> 
  <summary>Click here for see the changelog</summary>
 Changelog (game) 06/05/2016

 - Maps / Cells 100%
 - Deplacement for player 100%
 - Chat with flood checker for mute 80%
 - Command by chat 100%
 - Command by console for admin 100%
 - Items 100%
 - Panoply 100%
 - Emotes 100%
 - Guild 60%
 - Alignement 90%
 - Map actions 100%
 - Player group 100%
 - Experience (Job/Player/Mount/Guild) 100%
 - Job 1%
 - Zone/SubZone 80%
 - Creature 10%
 - Spells 10% (place and level only)
 - Boost of statisctics 100%
 - Monster on map
 - Fight 1%
 - Friends & Ennemies 90%
 - System for pods 100%
 - Exchange with other player 100%
 - Bank & Trunk 100%
 - Animation 100%
 - Channel 80%
 - Speak 100%
</details>
</p>

[Application]
</p>
<u>New version of <b>Graviton Manager</b> for manage login and all servers</u>
</p>
See the <a href = 'https://github.com/Babouche-/Graviton-Manager/'>project</a>

</p>
<u>New version of <b>Graviton Bot</b> for test the server</u>
</p>
See the <a href = 'https://github.com/Babouche-/Graviton-Bot/'>project</a>

