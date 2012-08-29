package org.obiba.heliotrope.domain

import org.obiba.heliotrope.storage.MongoTestEnvironment
import org.specs.runner.JUnit4
import org.specs.Specification
import com.foursquare.rogue.Rogue._

class InitialMongoSpecsTest extends JUnit4(InitialMongoSpecs)

object InitialMongoSpecs extends Specification with MongoTestEnvironment {
  
  def provide = addToSusVerb("provide")
  
  doBeforeSpec { prepareMongo() }
  
  "Mongo database" should provide {
    "two studies" >> {
      Study.findAll.length must_== 2
    }
    "four subjects" >> {
      Subject.findAll.length must_== 4
    }
    "one protocol" >> {
      Protocol.findAll.length must_== 1
    }
    "protocol by label name" >> {
      val query = Protocol where (_.label.subfield(_.default) eqs "Enrolment")
      query.count() must_== 1
    }
    "protocol contains three values" >> {
      val query = Protocol where (_.label.subfield(_.default) eqs "Enrolment")
      val result = query.fetch()
      result.size must_== 1
      result.head.values.is.size must_== 3
    }
  }
  
  doAfterSpec { cleanupMongo() }
}