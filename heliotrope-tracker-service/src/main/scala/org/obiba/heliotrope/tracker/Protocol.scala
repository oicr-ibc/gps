package org.obiba.heliotrope.tracker

import org.bson.types.ObjectId

object ProtocolValueType extends Enumeration {
  val String = Value("string")
  val Integer = Value("integer")
  val Float = Value("float")
  val Date = Value("date")
  val Boolean = Value("boolean")
}

object ProtocolControlType extends Enumeration {
  val Text = Value("text")
  val Select = Value("select")
  val Checkbox = Value("checkbox")
  val Date = Value("date")
  val Hidden = Value("hidden")
}

/**
 * Defines a set of access levels. These are defined at the protocol value
 * level, and the specified user should be accounted for in the data that
 * is returned. 
 */
object AccessType extends Enumeration {
  val Read = Value("read")
  val Write = Value("write")
  val ReadWrite = Value("read_write")
  val None = Value("none")
}

case class ProtocolValue(
  name: String,
  dataType: ProtocolValueType.Value = ProtocolValueType.String,
  controlType: ProtocolControlType.Value = ProtocolControlType.Text,
  label: Map[String, String] = Map.empty,
  controlParams: Map[String, String] = Map.empty,
  accessType: Map[String, AccessType.Value] = Map.empty,
  readOnly: Boolean = false,
  range: List[String] = List.empty
)

case class Protocol(
  _id: Option[ObjectId] = Some(new ObjectId()),
  identifier: String,
  studyIdentifier: String,
  instantaneous: Boolean = false,
  label: Map[String, String] = Map.empty,
  values: List[ProtocolValue] = List.empty
) extends IdentifiableComponent {
  
  private final val startedField =
    ProtocolValue("started", ProtocolValueType.Date, ProtocolControlType.Date, Map("default" -> "Started"), readOnly = false)
  
  private final val instantaneousFields = List(
    ProtocolValue("lastUpdated", ProtocolValueType.Date, ProtocolControlType.Hidden, Map("default" -> "Last updated"), readOnly = true),
    ProtocolValue("completed", ProtocolValueType.Date, ProtocolControlType.Date, Map("default" -> "Completed"), readOnly = false),
    ProtocolValue("lastUpdatedBy", ProtocolValueType.String, ProtocolControlType.Hidden, Map("default" -> "Last updated by"), readOnly = true)
  )
      
  def protocolValues(): List[ProtocolValue] = {
    values ++ (if (! instantaneous) Some(startedField) else None) ++ instantaneousFields 
  }
}

//import java.net.URI
//import net.liftweb.common._
//import net.liftweb.mongodb.record._
//import net.liftweb.mongodb.record.field._
//import net.liftweb.record.field._
//import net.liftweb.json._
//
///**
// * Class for the step protocol type, representing an individual step.
// */
//class StepProtocol private () extends SerializableMongoRecord[StepProtocol] {
//
//  /**
//   * Required meta property, used by MongoRecord
//   */
//  def meta = StepProtocol
//
//  /**
//   * The name of the step, mostly for internal purposes. Steps are scoped within
//   * a study, somehow.
//   */
//  object identifier extends StringField(this, 64)
//
//  /**
//   * Field for the owning study
//   */
//  object study extends ObjectIdRefField[StepProtocol, Study](this, Study) {
//    override def name = "studyId"
//  }
//
//  /**
//   * Set to true if this step is instantaneous. This controls how the coordination
//   * fields are added to the user-defined fields. An instantaneous step only has
//   * an end time, not a start time. 
//   */
//  object instantaneous extends BooleanField(this) {
//    override def defaultValue: Boolean = false
//  }
//
//  /**
//   * The label, which is a BSON object associative array, keyed by locale name
//   */
//  object label extends MongoMapField[StepProtocol, String](this)
//
//  /**
//   * A list of protocol values, as defined by the ProtocolValue class
//   */
//  object values extends BsonRecordListField(this, ProtocolValue) {
//    
//    private lazy final val lastUpdated = {
//      var field = ProtocolValue.createRecord
//      field.label.setFromJValue(JString("Last updated"))
//      field.name.setFromJValue(JString("lastUpdated"))
//      field.controlType.setFromJValue(JString("hidden"))
//      field.dataType.setFromJValue(JString("Date"))
//      field.readOnly.setFromJValue(JBool(true))
//      field.asJValue
//    }
//    
//    private lazy final val started = {
//      var field = ProtocolValue.createRecord
//      field.label.setFromJValue(JString("Started"))
//      field.name.setFromJValue(JString("started"))
//      field.controlType.setFromJValue(JString("date"))
//      field.dataType.setFromJValue(JString("Date"))
//      field.readOnly.setFromJValue(JBool(false))
//      field.asJValue
//    }
//    
//    private lazy final val completed = {
//      var field = ProtocolValue.createRecord
//      field.label.setFromJValue(JString("Completed"))
//      field.name.setFromJValue(JString("completed"))
//      field.controlType.setFromJValue(JString("date"))
//      field.dataType.setFromJValue(JString("Date"))
//      field.readOnly.setFromJValue(JBool(false))
//      field.asJValue
//    }
//    
//    private lazy final val completedBy = {
//      var field = ProtocolValue.createRecord
//      field.label.setFromJValue(JString("Last updated by"))
//      field.name.setFromJValue(JString("lastUpdatedBy"))
//      field.controlType.setFromJValue(JString("hidden"))
//      field.dataType.setFromJValue(JString("String"))
//      field.readOnly.setFromJValue(JBool(true))
//      field.asJValue
//    }
//    
//    override def asJValue = JArray(lastUpdated :: (if (instantaneous.is) List() else List(started)) ::: completed :: completedBy :: value.map(_.asJValue))
//  }
//
//  /**
//   * Returns the URL for this resource. A StepProtocol is embedded in a study
//   */
//  def getURI: URI = {
//    val superURIPath = study.obj.open_!.getURI
//    new URI(null.asInstanceOf[String], superURIPath + "/protocol/" + identifier.is, null.asInstanceOf[String])
//  }
//
//  /**
//   * Generates a stub object containing links for this protocol. This stub object contains the
//   * values, mainly to make it easier to generate the form as well as render the values in a 
//   * single request. 
//   */
//  def getLinks: JObject = {
//    JObject(
//      List(
//        JField("url", JString(getURI.getPath())),
//        JField("identifier", JString(identifier.is)),
//        JField("values", values.asJValue)
//      )    
//    )
//  }
//}
//
///**
// * Companion object for the Study class.
// */
//object StepProtocol extends StepProtocol with MongoMetaRecord[StepProtocol] {
//
//  /**
//   * Define the collection name, by default using the MongoDB collection "step_protocol"
//   */
//  override def collectionName = "step_protocol"
//}
//
///**
// * A Serializer for the StepProtocol class. 
// */
//object StepProtocolSerializer extends CustomSerializer[StepProtocol](format => (
//  /**
//   * Deserialize a step protocol from JSON - called from a specific deserializer 
//   * so that it can select which class to instantiate. 
//   */
//  { 
//    case data: JObject => {
//      val sp = StepProtocol.createRecord
//      sp.setFieldsFromJValue(data)
//      sp
//    }
//  },
//  /**
//   * Serialize a step protocol to JSON
//   */  
//  { 
//    case s: StepProtocol => 
//      val fields = (s.study.obj.flatten { study => List(JField("study", study.getLinks)) }) ++
//        List(JField("url", JString(s.getURI.getPath())))
//      JObject(s.asJValue.obj ::: fields.toList)
//  }
//))
