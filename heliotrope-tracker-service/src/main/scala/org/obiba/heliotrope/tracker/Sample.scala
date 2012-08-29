package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

case class Sample(
  _id: Option[ObjectId] = Some(new ObjectId()),
  identifier: String,
  studyIdentifier: String,
  subjectIdentifier: String,
  data: Option[Map[String, String]] = Some(Map.empty)
) extends IdentifiableComponent

//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//import net.liftweb.json.Extraction.decompose
//
//import java.net.URI
//
//class Sample private () extends SerializableMongoRecord[Sample] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = Sample
//
//  /**
//   * The sample identifier field
//   */
//  object identifier extends StringField(this, 32)
//
//  /**
//   * Field for a study. This is technical redundant, but we will likely find it
//   * useful, and we're using MongoDB here, therefore: so what?
//   */
//  object study extends ObjectIdRefField[Sample, Study](this, Study) {
//    override def name = "studyId"
//  }
//
//  /**
//   * Field for a subject.
//   */
//  object subject extends ObjectIdRefField[Sample, Subject](this, Subject) {
//    override def name = "subjectId"
//  }
//
//  /**
//   * Define a set of attributes and values, which can be used by applications.
//   * The actual data type in the map is left open for applications.
//   */
//  object data extends MongoMapField[Sample, Any](this)
//
//  /**
//   * Generates a URI for this sample
//   */
//  def getURI: URI = {
//    val superURIPath = subject.obj.open_!.getURI
//    new URI(null.asInstanceOf[String], superURIPath + "/sample/" + identifier.is, null.asInstanceOf[String])
//  }
//}
//
//object Sample extends Sample with MongoMetaRecord[Sample] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "sample"
//   */
//  override def collectionName = "sample"
//}
//
///**
// * A Serializer for the Sample class. 
// */
//object SampleSerializer extends CustomSerializer[Sample](format => (
//  /**
//   * Deserialize a sample from JSON - called from a specific deserializer 
//   * so that it can select which class to instantiate. 
//   */
//  { 
//    case data: JObject => {
//      val sample = Sample.createRecord
//      sample.setFieldsFromJValue(data)
//      sample
//    }
//  },
//  /**
//   * Serialize a sample to JSON
//   */  
//  { 
//    case s: Sample =>
//      val fields = 
//        (s.study.obj.flatten { study => List(JField("study", study.getLinks)) }) ++
//        (s.subject.obj.flatten { subject => List(JField("subject", subject.getLinks)) }) ++
//        List(JField("url", JString(s.getURI.getPath())))
//      JObject(s.asJValue.obj ::: fields.toList)
//  }
//))