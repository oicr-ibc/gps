package org.obiba.heliotrope.snippet

import org.obiba.heliotrope.storage.MongoTestEnvironment
import org.specs.runner.JUnit4
import org.specs.Specification
import net.liftweb.common.Loggable
import scala.xml.Text
import org.obiba.heliotrope.menu.{StudyLoc, NoSuchStudy, FullStudyInfo}
import net.liftweb.common.Full
import net.liftweb.http.S
import org.specs.specification.Examples
import net.liftweb.http.LiftSession
import net.liftweb.common.Empty
import net.liftweb.util.StringHelpers
import org.obiba.heliotrope.domain.Study
import com.foursquare.rogue.Rogue._

class StudyMongoSpecsTest extends JUnit4(StudyMongoSpecs)

object StudyMongoSpecs extends Specification with MongoTestEnvironment {
  
  val session = new LiftSession("", StringHelpers.randomString(20), Empty)

  def provide = addToSusVerb("provide")
  
  def inSession(a: =>Any) = {
    S.initIfUninitted(session) { a }
  }
  
  def loginUser = inSession {
    //val user: User = User.create
    //user.firstName("XXX")
    //user.lastName("YYYY")
    //user.save
    //User.logUserIn(user)
  }
  
  new SpecContext {
    beforeExample { 
      loginUser
    }
    afterExample  { /* teardown db here */}	
    aroundExpectations(inSession(_))
  }
  
  doBeforeSpec { prepareMongo() }
  
  "StudySnippet" should provide {
    "Put the title and descriptions in the nodes" >> {
      val studySnippet = new StudySnippet
      val str = studySnippet.render(
          <tr class="study_row">
    	    <td><a class="study_link study_title">Name</a></td>
		    <td class="study_description">Description</td>
    	  </tr>)
		  
      str must \\("td").\("a").\(Text("GPS"))
      str must \\("td").\("a").\(Text("PNA"))
      str must \\("td").\(Text("The Genome Potato Sandwich"))
      str must \\("td").\(Text("Prefer Not to Answer"))
    }
  }
  
  "SelectedStudy" should provide {
    "No selected study when appropriate" >> {
      StudyLoc.requestValue.set(Full(NoSuchStudy))
      val selectedStudy = new SelectedStudy
      val str = selectedStudy.render(
          <tr class="study">
    	    <td class="subject_subjectId">Id</td>
    	    <td class="subject_gender">Gender</td>
    	    <td class="subject_enrolmentDate">Enrolment Date</td>
    	    <td class="subject_primaryTissue">Primary Tissue</td>
    	  </tr>)

      str.toString() must_== "No such study"
    }
    
    "Study data when appropriate" >> {
      
      val query = Study where (_.title eqs "GPS")
      
      val study = query.get()
      val requestedStudy = study.map { new FullStudyInfo(_) }
      StudyLoc.requestValue.set(requestedStudy)
      
      val selectedStudy = new SelectedStudy
      val str = selectedStudy.render(
          <tr class="subject_record">
    	    <td class="subject_subjectId"> Id</td>
    	    <td class="subject_gender">Gender</td>
    	    <td class="subject_enrolmentDate">Enrolment Date</td>
    	    <td class="subject_primaryTissue">Primary Tissue</td>
    	  </tr>)
      
      val subjectNodes = str.\\("td").flatMap { _ filter { _.attribute("class") exists { _.text == "subject_subjectId" } } }.map { _.text }
      subjectNodes must contain("GEN-001")
      subjectNodes must contain("GEN-002")
      subjectNodes must contain("GEN-003")
      subjectNodes must notContain("PNA-001")
    }
  }
  
  doAfterSpec { cleanupMongo() }
}