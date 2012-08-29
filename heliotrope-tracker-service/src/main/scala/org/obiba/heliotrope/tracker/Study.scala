package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId
import net.liftweb.json.JsonAST._
import java.net.URI
import org.obiba.heliotrope.util.URIUtils

case class Study(
  _id: Option[ObjectId] = Some(new ObjectId()),
  identifier: String,
  description: String,
  data: Option[Map[String, String]] = Some(Map.empty)
) extends IdentifiableComponent

//
///**
// * Class for the study type, representing an individual study.
// */
//class Study private () extends SerializableMongoRecord[Study] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = Study
//
//  /**
//   * The code field, typically an acronym.
//   */
//  object identifier extends StringField(this, 32)
//
//  /**
//   * The description field, a longer description of the study.
//   */
//  object description extends StringField(this, 255)
//
//  /**
//   * Define a set of attributes and values, which can be used by applications.
//   * The actual data type in the map is left open for applications.
//   */
//  object data extends MongoMapField[Study, AnyVal](this)
//  
//  /**
//   * The study protocol, as far as it stands at present, which is a set of
//   * steps. 
//   */
//  object protocols extends ObjectIdRefListField[Study, StepProtocol](this, StepProtocol) {
//    override def name = "protocolIds"
//  }
//  
//  /**
//   * Generates a URI for this study
//   */
//  def getURI: URI = {
//    new URI(null.asInstanceOf[String], "/study/" + identifier.is, null.asInstanceOf[String])
//  }
//  
//  /**
//   * Generates a stub object containing links for this study
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
//object Study extends Study with MongoMetaRecord[Study] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "study"
//   */
//  override def collectionName = "study"
//}
//
///**
// * A Serializer for the Study class. 
// */
//object StudySerializer extends CustomSerializer[Study](format => (
//  /**
//   * Serialize a study to JSON
//   */
//  { 
//    case data: JObject => {
//      val study = Study.createRecord
//      study.setFieldsFromJValue(data)
//      study
//    }
//  },
//  /**
//   * Deserialize a study from JSON - called from a specific deserializer 
//   * so that it can select which class to instantiate. 
//   */
//  { 
//    case study: Study =>
//      study.asJValue ++ JField("url", JString(study.getURI.getPath()))
//  }
//))
