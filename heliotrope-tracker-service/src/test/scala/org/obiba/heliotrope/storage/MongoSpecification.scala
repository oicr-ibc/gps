package org.obiba.heliotrope.storage

import org.specs2.mutable._
import org.specs2.specification._
import org.specs2.execute._
import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBAddress
import java.io.File
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.FileReader
import org.slf4j.{ Logger, LoggerFactory }

trait MongoSpecification extends Specification {

  var mongoDb: Option[Mongo] = None

  def setups: Fragments = {
    step {
      mongoDb = Some(new Mongo(new DBAddress("127.0.0.1:27017", "tracker")))

      // Now to initialize the database...

      val bootSourceFile = new File("src/test/resources/test.js")
      val bootSource = scala.io.Source.fromFile(bootSourceFile).getLines.reduceLeft(_ + "\n" + _)

      mongoDb.map { 
        _.getDB("tracker").eval(bootSource) 
      }
      Text("Successfully initialized MongoDB from " + bootSourceFile)
    }
  }

  def teardowns: Fragments = {
    step {
      mongoDb.map { _.close }
      Text("Disconnected from MongoDB")
    }
  }

  override def map(fs: => Fragments) = setups ^ fs ^ teardowns
}
