package org.obiba.heliotrope.menu

import org.obiba.heliotrope.domain.Study

abstract class StudyInfo 
case object NoSuchStudy extends StudyInfo
case object AllStudies extends StudyInfo
case class FullStudyInfo(s: Study) extends StudyInfo
