package org.obiba.heliotrope.snippet

import net.liftweb.http.S
import scala.annotation.target.field
import net.liftweb.http.LiftScreen
import net.liftweb.util.Helpers
import org.obiba.heliotrope.domain.Subject
import org.obiba.heliotrope.domain.Study
import net.liftweb.http.SHtml
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.common.Empty
import scala.xml.NodeSeq
import net.liftweb.common.Loggable
import scala.xml.Elem

/**
 * Initial screen to create a new subject with some fields. At this stage, the 
 * most important fields are the subject identifier and the study. 
 */

object SubjectScreen extends LiftScreen with Loggable {
	
  object subject extends ScreenVar(Subject.createRecord)

  addFields(() => subject.is.subjectId)
  
  val study = new Field { 
    
    val query = Study.findAll.map { study => (study, study.title.is) }
    
    type ValueType = List[Study] 
    
    override def name = subject.fieldByName("study").map { _.displayName }.openOr(S ? "Studies")
    override implicit def manifest = buildIt[List[Study]] 
    override def default = List()
    override def toForm: Box[NodeSeq] = 
      SHtml.selectObj[Study](
          Study.findAll.map { study => (study, study.title.is) },
          Empty,
          x => this.set(List(x)))
  }
  
  // Core localization is not working as it should, but I'd rather we handled it
  // anyway. These should probably come from our own trait, though. 
  override def cancelButton: Elem = <button>{S ? "Cancel"}</button>

  override def finishButton: Elem = <button>{S ? "Finish"}</button>

  //val shouldSave = field("Save?", false)

  //val likeCats = builder("Do you like cats?", "") ^/
  //(s => if (Helpers.toBoolean(s)) Nil else "You have to like cats") make

  def finish() {
    val studyResult = study.is.map { _.id.is }
    logger.info(studyResult)
    subject.study.set(studyResult)
    subject.is.save
    S.notice(subject.is.subjectId.toString+" Saved in the database")
    S.redirectTo("/study")
  }
}