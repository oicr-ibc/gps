package org.obiba.heliotrope.domain

import org.obiba.heliotrope.storage.MongoTestEnvironment
import org.specs.runner.JUnit4
import org.specs.Specification
import com.foursquare.rogue.Rogue._

class SubjectSpecsTest extends JUnit4(SubjectSpecs)

object SubjectSpecs extends Specification with MongoTestEnvironment {
  
  def provide = addToSusVerb("provide")
  
  doBeforeSpec { prepareMongo() }
  
  "Subject GEN-001" should provide {
    "mere existence" >> {
      val query = Subject where (_.subjectId eqs "GEN-001")
      val result = query.fetch()
      result.size must_== 1
    }
  }
  
  doAfterSpec { cleanupMongo() }
}