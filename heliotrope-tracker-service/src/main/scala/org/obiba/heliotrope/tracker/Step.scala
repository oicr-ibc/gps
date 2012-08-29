package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

case class Step(
  _id: Option[ObjectId] = Some(new ObjectId()),
  studyIdentifier: String,
  protocolIdentifier: String,
  subjectIdentifier: Option[String] = None,
  sampleIdentifier: Option[String] = None,
  data: Map[String, AnyRef] = Map.empty
)

//package org.obiba.heliotrope.tracker
//
//import java.net.URI
//import net.liftweb.common._
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//
///**
// * Class for the step process type, representing data for an individual step.
// */
//class StepProcess private () extends SerializableMongoRecord[StepProcess] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = StepProcess
//
//  /**
//   * Field for an object identifier referring to the owner of the step, which
//   * will be either a Study or a Subject. 
//   */
//  object stepProtocol extends ObjectIdRefField[StepProcess, StepProtocol](this, StepProtocol)
//
//  /**
//   * Name for the owning StepProtocol, as a string. This is stored redundantly
//   * for performance, as it will be the same as the attached StepProtocol. Note
//   * that this will have an effect on performance of renaming the StepProtocol
//   * internally - the label is separate from the internal name used.
//   */
//  object step extends StringField(this, 32)
//
//  /**
//   * Field for an object identifier referring to the owner of the step, which
//   * will be either a Study or a Subject. Note that this is simply an identifier
//   * but because identifiers are unique across collections, queries can assume
//   * that this will find all steps for a given owning object. 
//   */
//  object owner extends ObjectIdField[StepProcess](this) {
//    private var _obj: Box[Either[Subject, Sample]] = Empty
//    private var _calcedObj = false
//    
//   /*
//    * get the referenced object, caching if needed. 
//    */
//    def obj = synchronized {
//      if (!_calcedObj) {
//        _calcedObj = true
//        this._obj = 
//          valueBox.flatMap { v => 
//            ownerType.is match {
//              case "subject" =>
//                Subject.findAny(v).map { sub => Left(sub) }
//              case "sample" =>
//                Sample.findAny(v).map { sam => Right(sam) }
//              case _ => Empty
//            }
//          }
//      }
//      _obj
//    }
//  }
//
//  /**
//   * Field for the type of the owning object, as a string. Most of the time, 
//   * this is not needed, but when starting from a given step process, it does 
//   * specify which collection to search for the owning object. 
//   */
//  object ownerType extends StringField(this, 32)
//  
//  /**
//   * Define a set of attributes and values, which can be used by applications.
//   * The actual data type in the map is left open for applications.
//   */
//  object data extends MongoMapField[StepProcess, AnyRef](this) {
//    
//    /**
//     * Transforms a value into a JSON version. This is way too naive, but is a 
//     * reasonable start for basic values. We ought really to follow the same
//     * pattern that liftweb-record-mongodb does, in terms of the way types are
//     * tagged at the JSON level. 
//     */
//    private def fieldValue(x: AnyRef) = x.toString()
//    
//    /**
//     * Transforms a list if fields into JSON. This overrides the default for 
//     * MongoMapField, which serializes into nothing. 
//     */
//    private def fieldList(values: Map[String, AnyRef]): List[JField] = {
//      values map { entry =>
//        entry match {
//          case (key, x) =>
//            JField(key, JString(fieldValue(x)))
//        }
//      } toList
//    }
//    
//    /**
//     * Override which actually generates the JSON needed. 
//     */
//    override def asJValue: JObject = JObject(fieldList(value))
//  }
//  
//  /**
//   * Returns a URI for a step within a steppable object, namely a subject or a sample. 
//   */
//  def getURI: URI = {
//    val ownerObject = owner.obj.open_!
//    val superURIPath = ownerObject.fold(sample => sample.getURI, subject => subject.getURI)
//    val identifier = stepProtocol.obj.open_!.identifier
//    new URI(null.asInstanceOf[String], superURIPath + "/step/" + identifier.is, null.asInstanceOf[String])
//  }
//}
//
///**
// * Companion object for the StepProcess class.
// */
//object StepProcess extends StepProcess with MongoMetaRecord[StepProcess] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "step_process"
//   */
//  override def collectionName = "step_process"
//}
//
///**
// * A Serializer for the StepProcess class. 
// */
//object StepProcessSerializer extends CustomSerializer[StepProcess](format => (
//  /**
//   * Deserialize a step process from JSON - called from a specific deserializer 
//   * so that it can select which class to instantiate. 
//   */
//  { 
//    case data: JObject => {
//      val sp = StepProcess.createRecord
//      sp.setFieldsFromJValue(data)
//      sp
//    }
//  },
//  /**
//   * Serialize a step process to JSON
//   */  
//  { 
//    case s: StepProcess => 
//      val fields = 
//        (s.stepProtocol.obj.flatten { protocol => List(JField("protocol", protocol.getLinks)) }) ++
//        List(JField("url", JString(s.getURI.getPath())))
//      JObject(s.asJValue.obj ::: fields.toList)
//  }
//))
