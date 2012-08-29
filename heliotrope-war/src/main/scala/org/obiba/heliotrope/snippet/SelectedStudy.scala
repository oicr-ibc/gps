package org.obiba.heliotrope.snippet

import org.bson.types.ObjectId
import org.obiba.heliotrope.domain.Study
import org.obiba.heliotrope.domain.Subject
import com.foursquare.rogue.Rogue._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb._
import org.obiba.heliotrope.menu._
import scala.xml.Text
import net.liftweb.http.S
import scala.xml.NodeSeq

/**
 * Snippet that grabs the current study identifier and puts that study into a box
 * in the request variables, where it can be used later. This can then be used as a 
 * wrapper in a template to set up the study context for later use in display. 
 */
class SelectedStudy extends Loggable {
  
  def render =
    "*" #> {
      val locValue = S.location.flatMap { _.currentValue }
      locValue.map { studyInfo => 
      studyInfo match {
        case FullStudyInfo(study) => {
          val studyId = study.id.is
          logger.debug("Selected study id: " + studyId)
          
          val query = Subject where (_.study contains new ObjectId(studyId.toString())) fetch
          
          
          ".subject_record" #> query.map { sub: Subject =>
            ".subject_id *" #> sub.id.toString &
            ".subject_subjectId *" #> sub.subjectId &
            ".subject_gender *" #> sub.protocolData.is.gender.asHtml &
            ".subject_enrolmentDate *" #> sub.protocolData.is.enrolmentDate.asHtml &
            ".subject_primaryTissue *" #> sub.protocolData.is.primaryTissue.asHtml &
            ".subject_link [href]" #> SubjectMenuItems.subjectMenuItem.createLink(FullSubjectInfo(sub)).get
          }
        }
        case _ => {
          "*" #> Text(S ? "No such study")
        }
      }
    }
  }
}