package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

case class Subject(
  _id: Option[ObjectId] = Some(new ObjectId()),
  identifier: String,
  studyIdentifier: String,
  data: Option[Map[String, String]] = Some(Map.empty)
) extends IdentifiableComponent



//package org.obiba.heliotrope.tracker
//
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//import net.liftweb.json.Extraction.decompose
//import java.net.URI
//
///**
// * Class for the subject type, representing an individual subject. Only
// * public data should be visible within the class directly, so that all
// * possibly restricted information can demand authorization through a
// * protocol and corresponding access control mechanisms.
// */
//class Subject private () extends SerializableMongoRecord[Subject] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = Subject
//
//  /**
//   * Field for the public subject identifier.
//   */
//  object identifier extends StringField(this, 32)
//
//  /**
//   * Field for the owning study
//   */
//  object study extends ObjectIdRefField[Subject, Study](this, Study) {
//    override def name = "studyId"
//  }
//
//  /**
//   * Define a set of attributes and values, which can be used by applications.
//   * The actual data type in the map is left open for applications.
//   */
//  object data extends MongoMapField[Subject, Any](this)
//  
//  /**
//   * Generates a URI for this subject
//   */
//  def getURI: URI = {
//    val superURIPath = study.obj.open_!.getURI
//    new URI(null.asInstanceOf[String], superURIPath + "/subject/" + identifier.is, null.asInstanceOf[String])
//  }
//
//  /**
//   * Generates a stub object containing links for this subject
//   */
//  def getLinks: JObject = {
//    JObject(
//      List(
//        JField("url", JString(getURI.getPath())),
//        JField("identifier", JString(identifier.is))
//      )    
//    )
//  }
//}
//
///**
// * Companion object for the Subject class.
// */
//object Subject extends Subject with MongoMetaRecord[Subject] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "subject"
//   */
//  override def collectionName = "subject"
//}
//
///**
// * A Serializer for the Subject class. 
// */
//object SubjectSerializer extends CustomSerializer[Subject](format => (
//  /**
//   * Deserialize a subject from JSON - called from a specific deserializer 
//   * so that it can select which class to instantiate. 
//   */
//  { 
//    case data: JObject => {
//      val sub = Subject.createRecord
//      sub.setFieldsFromJValue(data)
//      sub
//    }
//  },
//  /**
//   * Serialize a subject to JSON - this dynamically injects the studyCode attribute into
//   * the returned JSON. It is not persisted, but is generated from the study field, which
//   * is an object reference. 
//   */
//  { 
//    case sub: Subject =>
//      val fields = sub.study.obj.flatten { study => 
//          val studyJObject = decompose(study)(format)
//          List(JField("study", studyJObject)) 
//        } ++
//        List(JField("url", JString(sub.getURI.getPath())))
//      JObject(sub.asJValue.obj ::: fields.toList)
//  }
//))
