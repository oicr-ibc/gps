package org.obiba.heliotrope.domain

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.util.Helpers
import net.liftweb.json.JsonAST.JValue
import net.liftweb.common.Box
import net.liftweb.json.Extraction
import com.foursquare.rogue.Rogue._
import org.bson.types.ObjectId
import net.liftweb.mongodb.record.field.ObjectIdField

/**
 * Class for the study type, representing an individual study.
 */
class Study private() extends MongoRecord[Study] with ObjectIdPk[Study] {
  def meta = Study

  /**
   * The title field, typically an acronym.
   */
  object title extends StringField(this, 32)
  
  /**
   * The description field, a longer description of the study.
   */
  object description extends StringField(this, 255)
 
  /**
   * Returns the number of subjects in this study.
   */
  def numberOfSubjects() = {
    Subject where (_.study contains new ObjectId(this.id.is.toString())) count()
  }
}

/**
 * Companion object for the Study class.
 */
object Study extends Study with MongoMetaRecord[Study] {
  
  /**
   * Define the collection name, by default using the MongoDB collection "study"
   */
  override def collectionName = "study"
}
