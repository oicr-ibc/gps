package org.obiba.heliotrope.snippet

import net.liftweb._
import util.Helpers._
import common._
import http._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.Menu._
import net.liftweb.common.Loggable
import org.obiba.heliotrope.domain.User
import org.obiba.heliotrope.domain.Subject
import org.obiba.heliotrope.menu._
import com.foursquare.rogue.Rogue._
import scala.xml._
import org.bson.types.ObjectId

class Subjects {
  def render = 
    ".subject_row" #> Subject.findAll.map { subject => 
      ".subject_id *" #> subject.id.toString &
      ".subject_subjectId *" #> subject.subjectId &
      ".subject_link [href]" #> ("/subject/" + urlEncode(subject.subjectId.is))
    }
}

object SubjectMenu extends Loggable {
  def render = "*" #> {
	S.location match {
	  case Full(baseloc: Loc[SubjectInfo]) => {
	    val menus: List[Loc[SubjectInfo]] = LiftRules.siteMap.toList.flatMap { _.locForGroup("subject").map { _.asInstanceOf[Loc[SubjectInfo]] } }
	    "li" #> menus.map { loc: Loc[SubjectInfo] =>
	      val currentValue: SubjectInfo = baseloc.currentValue.open_!
	      
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
	    "*" #> Text(S ? "No such subject")
      }
	}
  }
}

class SelectedSubject extends Loggable {
  
  def render =
    "*" #> {
      val locValue = S.location.flatMap { _.currentValue }
      locValue.map { subjectInfo => 
      subjectInfo match {
        case FullSubjectInfo(subject) => {
          val subjectId = subject.id.is
          logger.debug("Selected subject id: " + subjectId)
          
          val query = Subject where (_.id eqs new ObjectId(subjectId.toString())) fetch
          
          ".subject_record" #> query.map { sub: Subject =>
            ".subject_id *" #> sub.id.toString &
            ".subject_subjectId *" #> sub.subjectId &
            ".subject_gender *" #> sub.protocolData.is.gender.asHtml &
            ".subject_enrolmentDate *" #> sub.protocolData.is.enrolmentDate.asHtml &
            ".subject_primaryTissue *" #> sub.protocolData.is.primaryTissue.asHtml
          }
        }
        case _ => {
          "*" #> Text(S ? "No such subject")
        }
      }
    }
  }
}

object SubjectMenuItems extends Loggable {

  def loggedIn_? = { User.loggedInUser().isDefined }
  def ifLoggedIn = If(() => loggedIn_?, "You must be logged in")
  
  def subjectIdentifierFunction(in: SubjectInfo): NodeSeq = {
    Text(subjectIdentifier(in))
  }

  def parseSubjectUrl(b: List[String]) = {
    if (b.isEmpty) {
      Full(AllSubjects)
    } else {
      val subjectCode = b.head
      val subjectQuery = Subject where (_.subjectId eqs subjectCode)
      val result = subjectQuery.get()
      if (result.isEmpty) {
        Full(NoSuchSubject)
      } else {
        Full(FullSubjectInfo(result.get))
      }
    }
  }

  def encodeSubjectUrl(in: SubjectInfo) = {
    in match {
      case AllSubjects => {
        Nil
      }
      case FullSubjectInfo(subject) => {
        subject.subjectId.is :: Nil
      }
    }
  }

  def subjectIdentifier(in: SubjectInfo): String = {
    in match {
      case FullSubjectInfo(subject) => {
        subject.subjectId.is
      }
      case _ => {
        S ? "Unknown subject"
      }
    }
  }

  lazy val subjectMenuItem = Menu.params[SubjectInfo]("subject", LinkText(subjectIdentifierFunction _),  parseSubjectUrl _, encodeSubjectUrl _) / "subject" / * >> 
    ifLoggedIn >> 
    Hidden >>
    Loc.Template(() => Templates.apply(List("subject", "star", "show")) openOr <div>Parameter tested</div>)
    
  lazy val mainMenuItem = Menu("subjects", S ? "Subjects") / "subject" >> 
    ifLoggedIn >>
    Hidden >>
    Loc.Template(() => Templates.apply(List("subject")) openOr <div>Parameter tested</div>) submenus (subjectMenuItem) 

  def menus: List[Menu] = List(mainMenuItem)
}