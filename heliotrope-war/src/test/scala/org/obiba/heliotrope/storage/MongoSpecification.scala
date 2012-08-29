package org.obiba.heliotrope.storage

import org.specs.Specification
import net.liftweb.mongodb.MongoDB
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.mongodb.DB
import java.io.File
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.FileReader

trait MongoTestEnvironment {
  
  var mongoDb: Option[DB] = None
  
  def prepareMongo(): Unit = {
    MongoConfig.init
    mongoDb = MongoDB.getDb(DefaultMongoIdentifier)
    
    // Now to initialize the database...
    
    val bootSourceFile = new File("src/test/resources/initial.js")
    val bootSource = scala.io.Source.fromFile(bootSourceFile).getLines.reduceLeft(_+"\n"+_)
    
    mongoDb match {
      case Some(theDb) => {
        theDb.eval(bootSource);
      }
      case None => {

      }
    }
  }
  
  def cleanupMongo(): Unit = {
    MongoDB.close
  }
}