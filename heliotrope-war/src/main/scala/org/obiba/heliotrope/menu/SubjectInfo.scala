package org.obiba.heliotrope.menu

import org.obiba.heliotrope.domain.Subject

abstract class SubjectInfo 
case object NoSuchSubject extends SubjectInfo
case object AllSubjects extends SubjectInfo
case class FullSubjectInfo(s: Subject) extends SubjectInfo
