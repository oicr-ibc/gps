package org.obiba.heliotrope.snippet

import org.specs.runner.ConsoleRunner
import org.obiba.heliotrope.menu.InitialSiteMap
import net.liftweb.http.LiftRules
import org.specs.runner.JUnit4
import net.liftweb.common.Loggable
import net.liftweb.http.Req
import net.liftweb.http.Html5Properties
import org.specs.mock.Mocker
import org.obiba.heliotrope.storage.MongoTestEnvironment
import net.liftweb.mockweb.WebSpec
import net.liftweb.mocks.MockHttpServletRequest

class SubjectScreenTestSpecsAsTest extends JUnit4(SubjectScreenTestSpecs)
object SubjectScreenTestSpecsRunner extends ConsoleRunner(SubjectScreenTestSpecs)

object SubjectScreenBoot extends Loggable {
  def boot() {
    InitialSiteMap.setupSiteMap
    
    LiftRules.htmlProperties.default.set((r: Req) =>
    	new Html5Properties(r.userAgent))  
    	
    LiftRules.addToPackages("org.obiba.heliotrope")
  }
}

object SubjectScreenTestSpecs extends WebSpec(SubjectScreenBoot.boot _) with MongoTestEnvironment with Mocker {

  doBeforeSpec { prepareMongo() }

  doAfterSpec { cleanupMongo() }
  
  "Subject screen" should {
    "contain a subject identifier" withSFor(new MockHttpServletRequest("http://foo.com/test/study", "/test")) in {      
      SubjectScreen.screenFields must exist { _.name == "subjectId" }
    }

    "contain a study menu" withSFor(new MockHttpServletRequest("http://foo.com/test/study", "/test")) in {      
      SubjectScreen.screenFields must exist { _.name == "Studies" }
    }
  }
}