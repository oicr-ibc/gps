package org.obiba.heliotrope.domain

import net.liftweb.util.Helpers
import net.liftweb.json.JsonAST.JValue
import net.liftweb.common.Box
import net.liftweb.json.Extraction

import net.liftweb.record.field.StringField
import net.liftweb.record.field.BooleanField

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.field.BsonRecordField
import net.liftweb.mongodb.record.BsonRecord
import net.liftweb.mongodb.record.BsonMetaRecord
import net.liftweb.mongodb.record.field.BsonRecordListField

/**
 * Class and companion for a label. This will always have a "default" value, as well
 * as others for each locale. This allows a protocol's values to
 * be localized.
 */
class ProtocolLabel private () extends BsonRecord[ProtocolLabel] {
  def meta = ProtocolLabel
  object default extends StringField(this, 100)
}
object ProtocolLabel extends ProtocolLabel with BsonMetaRecord[ProtocolLabel]

/**
 * Class and companion for a set of roles. This governs who is allowed to read values, 
 * and who is allowed to change them. 
 */
class ProtocolRoles private () extends BsonRecord[ProtocolRoles] {
  def meta = ProtocolRoles
  object default extends StringField(this, 100)
}
object ProtocolRoles extends ProtocolRoles with BsonMetaRecord[ProtocolRoles]

/**
 * A class and companion for a protocol value. Every value has a type, and this
 * is all data driven, so the type can be set through the front end. 
 */
class ProtocolValue private () extends BsonRecord[ProtocolValue] {
  def meta = ProtocolValue
  object name extends StringField(this, 64)
  object dataType extends StringField(this, 64)  { override def name = "type" }
  object label extends BsonRecordField(this, ProtocolLabel)
  object roles extends BsonRecordField(this, ProtocolRoles)
  object audit extends BooleanField(this)
}
object ProtocolValue extends ProtocolValue with BsonMetaRecord[ProtocolValue]

/**
 * Class for the study type, representing an individual protocol.
 */
class Protocol private () extends MongoRecord[Protocol] with ObjectIdPk[Protocol] {
  def meta = Protocol

  /**
   * The label, which is a BSON object associative array, keyed by locale name
   */
  object label extends BsonRecordField(this, ProtocolLabel)
  object values extends BsonRecordListField(this, ProtocolValue)
}

/**
 * Companion object for the Study class.
 */
object Protocol extends Protocol with MongoMetaRecord[Protocol] {
  
  /**
   * Define the collection name, by default using the MongoDB collection "protocol"
   */
  override def collectionName = "protocol"
}
