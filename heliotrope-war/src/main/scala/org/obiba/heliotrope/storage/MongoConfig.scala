package org.obiba.heliotrope.storage

import org.obiba.heliotrope.util.Assertions
import com.mongodb.ServerAddress
import com.mongodb.{Mongo, MongoOptions, ServerAddress}
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import net.liftweb.util.Props
import net.liftweb.common.Logger

object MongoConfig extends Assertions with Logger {
  def init: Unit = {
    
    val dbName = Props.get("mongo.db", "heliotrope")
    val dbUser = Props.get("mongo.user")
    val dbPassword = Props.get("mongo.password")
    
    val srvr = new ServerAddress(
       Props.get("mongo.host", "127.0.0.1"),
       Props.getInt("mongo.port", 27017)
    )
    
    debug("Database name: " + dbName)
    debug("Server address: " + srvr.getHost())
    debug("Server port: " + srvr.getPort())
    
    if (dbUser.isEmpty) {
      MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(srvr), dbName)
    } else {
      debug("Logging into Mongo as user: " + dbUser.openOr("unknown") + ", with password: " + dbPassword.openOr("unknown"))
      assert(!dbUser.isEmpty)
      assert(!dbPassword.isEmpty)
      MongoDB.defineDbAuth(DefaultMongoIdentifier, new Mongo(srvr), dbName, dbUser.openTheBox, dbPassword.openTheBox)
    }
  }
}
