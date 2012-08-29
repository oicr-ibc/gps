package org.obiba.heliotrope.snippet

import org.obiba.heliotrope.domain.Study
import scala.xml.NodeSeq
import _root_.net.liftweb.util.Helpers._
import scala.xml.Text
import net.liftweb.http.S

class StudySnippet {
  
  def render = 
    ".study_row" #> Study.findAll.map { study => 
      ".study_id *" #> study.id.toString &
      ".study_title *" #> study.title &
      ".study_description *" #> study.description &
      ".study_link [href]" #> ("/study/" + urlEncode(study.title.is))
    }

}
