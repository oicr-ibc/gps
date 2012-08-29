package org.obiba.heliotrope.menu

import scala.xml.NodeSeq
import scala.xml.Text

import org.bson.types.ObjectId
import org.obiba.heliotrope.domain.Study
import org.obiba.heliotrope.domain.User

import com.foursquare.rogue.Rogue._

import net.liftweb.common._
import net.liftweb.http.ParsePath
import net.liftweb.http.RewriteRequest
import net.liftweb.http.RewriteResponse
import net.liftweb.http.S
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap._

object StudyLoc extends Loc[StudyInfo] with Loggable {
  
  def name = "study"
    
  def defaultValue = Full(NoSuchStudy) 
  
  def params: List[LocParam[StudyInfo]] = {
    def loggedIn_? = { User.loggedInUser().isDefined }
    def ifLoggedIn = If(() => loggedIn_?, "You must be logged in")
    List(ifLoggedIn)
  }
    
  val text = new Loc.LinkText(calcLinkText _)
  
  def calcLinkText(in: StudyInfo): NodeSeq = {
    logger.debug("Requested link text: " + in)
    Text(S ? "Studies")
  }
  
  val link = new Loc.Link[StudyInfo](List("study"), false) {  
    override def createLink(in: StudyInfo) = {  
      Full(Text("/study"))
    } 
  } 
  
  override def title(in : StudyInfo) = {
    in match {
      case FullStudyInfo(study) => 
        Text(study.title.is)
      case _ => Text("No study")
    }
  }
  
  override def rewrite = Full({
    case RewriteRequest(ParsePath(List("study", studyId), _, _, _), _, _) => {
      
      if (! ObjectId.isValid(studyId)) {
        (RewriteResponse("study" :: Nil), NoSuchStudy)
        
      } else {
        logger.info("Study id: " + studyId)
        val objectId = new ObjectId(studyId)
        
        val query = Study where (_.id eqs objectId)
        query.fetch() match {
          case List(study) => {
            (RewriteResponse("subject" :: Nil), FullStudyInfo(study))
          }
          case _ => {
            (RewriteResponse("study" :: Nil), NoSuchStudy)
          }
        }
      }
    }
  })
}
