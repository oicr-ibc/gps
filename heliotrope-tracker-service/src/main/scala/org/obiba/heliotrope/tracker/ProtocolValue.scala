//package org.obiba.heliotrope.tracker
//
//import net.liftweb.common._
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//
///**
// * Defines a set of protocol value types. 
// */
//object ProtocolValueType extends Enumeration {
//  type ProtocolValueType = Value
//  val String, Integer, Float, Date, Boolean = Value
//}
//
///**
// * A class and companion for a protocol value. Every value has a type, and this
// * is all data driven, so the type can be set through the front end.
// */
//class ProtocolValue private () extends BsonRecord[ProtocolValue] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = ProtocolValue
//
//  /**
//   * The name of the protocol value - not unique, but used within the scope of
//   * the containing protocol.
//   */
//  object name extends StringField(this, 64)
//
//  /**
//   * The data type of the protocol, mapped as a string. This is an enumeration,
//   * so only valid data types are available.
//   */
//  object dataType extends EnumNameField(this, ProtocolValueType) {
//    override def name = "type"
//  }
//  
//  /**
//   * Defines the control type that is intended to display this particular
//   * field. 
//   */
//  object controlType extends StringField(this, 32)
//
//  /**
//   * Defines a set of (optional) parameters that are recommended for display
//   * and interaction purposes. 
//   */
//  object controlParams extends MongoMapField[ProtocolValue, String](this) {
//    override def optional_? = true
//  }
//
//  /**
//   * Set to true if this field cannot be set. If this is set to true, the UI
//   * should not attempt to set a new value. This will often be combined with
//   * a controlType of "hidden" to set implicit fields for a step that can
//   * be read but not edited. These implicit fields will typically define 
//   * who conducted a step, or when it was last updated. 
//   */
//  object readOnly extends BooleanField(this) {
//    override def defaultValue: Boolean = false
//  }
//
//  /**
//   * The label to use for rendering. This is a map field, with the key
//   * being a locale name.
//   */
//  object label extends MongoMapField[ProtocolValue, String](this)
//
//  /**
//   * A map of roles, so that each role has certain access rights.
//   */
//  object roles extends MongoMapField[ProtocolValue, AccessType](this)
//
//  /**
//   * Whether or or not the field should be audited. By default, it's supposed
//   * to be audited.
//   */
//  object audit extends BooleanField(this) {
//    override def optional_? = true
//  }
//}
//
//object ProtocolValue extends ProtocolValue with BsonMetaRecord[ProtocolValue]
//
