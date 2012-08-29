package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

case class User(
  _id: Option[ObjectId] = Some(new ObjectId()),
  identifier: String,
  passwordDigest: Option[String] = None
)  extends IdentifiableComponent

//package org.obiba.heliotrope.tracker
//
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//
///**
// * Class for the user type, representing an individual user. Only
// * public data should be visible within the class directly, so that all
// * possibly restricted information can demand authorization through a
// * protocol and corresponding access control mechanisms.
// */
//class User private () extends SerializableMongoRecord[User] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = User
//
//  /**
//   * Field for the public subject identifier.
//   */
//  object identifier extends StringField(this, 32)
//
//  /**
//   * Field for the password digest, if set.
//   */
//  object passwordDigest extends StringField(this, 40) {
//    override def optional_? = true
//  }
//}
//
///**
// * Companion object for the Subject class.
// */
//object User extends User with MongoMetaRecord[User] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "subject"
//   */
//  override def collectionName = "user"
//}
//
