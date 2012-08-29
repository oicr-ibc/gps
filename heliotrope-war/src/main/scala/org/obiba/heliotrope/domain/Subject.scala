package org.obiba.heliotrope.domain

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.ObjectIdRefField
import net.liftweb.common.Full
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.mongodb.record.field.DateField
import net.liftweb.mongodb.record.field.BsonRecordField
import net.liftweb.mongodb.record.field.MongoListField
import org.bson.types.ObjectId
import net.liftweb.http.SHtml
import net.liftweb.common.Box
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.http.S

/**
 * A BsonRecord for the embedded subject data. This defines the initial
 * form of the protocol data. 
 */
class ProtocolData private () extends BsonRecord[ProtocolData] {
  def meta = ProtocolData

  /**
   * Field for the gender
   */
  object gender extends StringField(this, 1) {
    override def optional_? = true
    override def displayName = S ? "Gender"
  }
  
  /**
   * Field for the enrolment date
   */
  object enrolmentDate extends DateField(this) {
    override def optional_? = true
    override def displayName = S ? "Enrolment Date"
    override def asHtml: NodeSeq = {
      Text(valueBox.map { 
        v => owner.meta.formats.dateFormat.format(v)
      } openOr "")
    }
  }
  
  /**
   * Field for the primary tissue type
   */
  object primaryTissue extends StringField(this, 100) {
    override def optional_? = true
    override def displayName = S ? "Primary Tissue"
  }
}

/**
 * Companion object for the embedded subject data.
 */
object ProtocolData extends ProtocolData with BsonMetaRecord[ProtocolData]

/**
 * Class for the subject type, representing an individual subject. Only 
 * public data should be visible within the class directly, so that all
 * possibly restricted information can demand authorization through a
 * protocol and corresponding access control mechanisms. 
 */
class Subject private() extends MongoRecord[Subject] with ObjectIdPk[Subject] {
  def meta = Subject

  /**
   * Field for the public subject identifier. 
   */
  object subjectId extends StringField(this, 32) {
    override def displayName = S ? "Subject ID"
  }
  
  /**
   * Field for a list of studies.
   */
  object study extends MongoListField[Subject, ObjectId](this) {
    override def displayName = S ? "Studies"
  }
  
  /**
   * Reference to the other protocol data, in an embedded ProtocolData
   * record. 
   */
  object protocolData extends BsonRecordField(this, ProtocolData)
  
  //object refDocId extends ObjectIdRefField(this, Study) {
  //  override def options = Study.findAll.map(rd => (Full(rd.id.is), rd.title.is))
  //}
}

/**
 * Companion object for the Subject class.
 */
object Subject extends Subject with MongoMetaRecord[Subject] {

  /**
   * Define the collection name, by default using the MongoDB collection "subject"
   */
  override def collectionName = "subject"
}
