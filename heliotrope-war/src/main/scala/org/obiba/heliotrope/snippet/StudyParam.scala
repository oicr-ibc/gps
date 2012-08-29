package org.obiba.heliotrope.snippet

import net.liftweb._
import util.Helpers._
import common._
import http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.Menu._
import org.obiba.heliotrope.domain.Study
import org.obiba.heliotrope.menu.StudyInfo
import org.obiba.heliotrope.menu.FullStudyInfo
import net.liftweb.sitemap.Loc.If
import org.obiba.heliotrope.domain.User
import org.obiba.heliotrope.menu.AllStudies
import com.foursquare.rogue.Rogue._
import org.obiba.heliotrope.menu.NoSuchStudy
import scala.xml.NodeSeq
import scala.xml.Text

class CurrentStudyTitle(in: StudyInfo)  {
  def render = "*" #> {
    val locValue = S.location.flatMap { _.currentValue }
    locValue.map { studyInfo => 
      studyInfo match {
        case FullStudyInfo(study) => {
          study.title.is
        }
        case _ => {
          S ? "No study selected"
        }
      }
    }
  }
}

object StudyProperties extends Loggable {
  def render = "*" #> {
    val locValue = S.location.flatMap { _.currentValue }
      logger.info("Location value: " + locValue)
      locValue.map { studyInfo => 
        studyInfo match {
          case FullStudyInfo(study) => {          
            ".study_title" #> study.title.is &
            ".study_description" #> study.description.is &
            ".study_number_patients" #> study.numberOfSubjects()
          }
          case _ => {
            "*" #> Text(S ? "No such study")
          }
        }
      }
  }
}

object StudyMenu extends Loggable {
  def render = "*" #> {
	S.location match {
	  case Full(baseloc: Loc[StudyInfo]) => {
	    val menus: List[Loc[StudyInfo]] = LiftRules.siteMap.toList.flatMap { _.locForGroup("study").map { _.asInstanceOf[Loc[StudyInfo]] } }
	    "li" #> menus.map { loc: Loc[StudyInfo] =>
	      val currentValue: StudyInfo = baseloc.currentValue.open_!
	      
	      if (loc != baseloc) {
	        val url = if (loc != baseloc) loc.createLink(currentValue) else (None: Option[NodeSeq])
	        "a *" #> loc.text.text.apply(currentValue) &
	        "a [href]" #> url
	      } else {
	        "a" #> loc.text.text.apply(currentValue)
	      }
	    }
	  }
	  case _ => {
	    "*" #> Text(S ? "No such study")
      }
	}
  }
}

object StudyMenuItems extends Loggable {

  def loggedIn_? = { User.loggedInUser().isDefined }
  def ifLoggedIn = If(() => loggedIn_?, "You must be logged in")
  
  def parseStudyUrl(b: List[String]) = {
    if (b.isEmpty) {
      Full(AllStudies)
    } else {
      val studyCode = b.head
      val studyQuery = Study where (_.title eqs studyCode)
      val result = studyQuery.get()
      if (result.isEmpty) {
        Full(NoSuchStudy)
      } else {
        Full(FullStudyInfo(result.get))
      }
    }
  }

  def encodeStudyUrl(in: StudyInfo) = {
    in match {
      case AllStudies => {
        Nil
      }
      case FullStudyInfo(study) => {
        study.title.is :: Nil
      }
    }
  }
  
  def studyTitle(in: StudyInfo): String = {
    in match {
      case FullStudyInfo(study) => {
        study.title.is
      }
      case _ => {
        S ? "Unknown study"
      }
    }
  }
  
  def studyTitleSubjectsFunction(in: StudyInfo): NodeSeq = {
    Text(S.?("Subjects for study: %1$s", studyTitle(in)))
  }
  
  def studyTitleNewSubjectFunction(in: StudyInfo): NodeSeq = {
    Text(S.?("New subject for study: %1$s", studyTitle(in)))
  }
  
  def studyTitleFunction(in: StudyInfo): NodeSeq = {
    Text(studyTitle(in))
  }
  
  def menus: List[Menu] = {
    
    // Menu item to create a new subject - to live in a menu group
    val studyNewSubjectMenuItem = Menu.params[StudyInfo]("studyNewSubject", LinkText(studyTitleNewSubjectFunction _),  parseStudyUrl _, encodeStudyUrl _) / "study" / * / "subject" / "new" >> 
      ifLoggedIn >> 
      LocGroup("study") >>
      Loc.Template(() => Templates.apply(List("study", "star", "subject", "new")) openOr <div>Parameter tested</div>)
    
    // Menu item for a list of subjects in a study
    val studySubjectMenuItem = Menu.params[StudyInfo]("studySubjects", LinkText(studyTitleSubjectsFunction _),  parseStudyUrl _, encodeStudyUrl _) / "study" / * / "subject" >> 
      ifLoggedIn >> 
      LocGroup("study") >>
      Loc.Template(() => Templates.apply(List("study", "star", "subject")) openOr <div>Parameter tested</div>)
    
    // Menu item for a study
    val studyMenuItem = Menu.params[StudyInfo]("study", LinkText(studyTitleFunction _),  parseStudyUrl _, encodeStudyUrl _) / "study" / * >> 
      ifLoggedIn >> 
      Loc.Template(() => Templates.apply(List("study")) openOr <div>Parameter tested</div>) submenus (studySubjectMenuItem, studyNewSubjectMenuItem)
    
    // Menu item for a list of studies
    val mainMenuItem = Menu("studies", S ? "Studies") / "study" >> 
      ifLoggedIn >>
      Loc.Template(() => Templates.apply(List("studies")) openOr <div>Parameter tested</div>) submenus (studyMenuItem) 
              
    List(mainMenuItem)
  }
}
