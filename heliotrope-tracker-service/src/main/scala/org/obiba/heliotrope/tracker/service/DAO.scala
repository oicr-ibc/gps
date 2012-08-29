package org.obiba.heliotrope.tracker.service

import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import org.obiba.heliotrope.tracker._
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.global._

object DAO {
  
  val baseConnection = MongoConnection()("tracker")
  
  object studyDAO extends SalatDAO[Study, ObjectId](collection = baseConnection("study"))
  
  object subjectDAO extends SalatDAO[Subject, ObjectId](collection = baseConnection("subject"))
  
  object sampleDAO extends SalatDAO[Sample, ObjectId](collection = baseConnection("sample"))

  object protocolDAO extends SalatDAO[Protocol, ObjectId](collection = baseConnection("protocol"))

  object stepDAO extends SalatDAO[Step, ObjectId](collection = baseConnection("step"))

  object userDAO extends SalatDAO[User, ObjectId](collection = baseConnection("user"))
}
  
