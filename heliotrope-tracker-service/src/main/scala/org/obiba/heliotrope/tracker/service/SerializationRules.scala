package org.obiba.heliotrope.tracker.service

import net.liftweb.json.Serializer
import org.bson.types.ObjectId
import net.liftweb.json._

/*
 * Case class handling of query options. These are passed in and allow the resource 
 * request to be filtered in useful ways. Among other things, these are needed to 
 * handle the paging needed by the grid logic, and filtering. 
 */

class ObjectIdSerializer extends Serializer[ObjectId] {  
  private val ObjectIdClass = classOf[ObjectId]  
  
  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ObjectId] = {  
    case (TypeInfo(ObjectIdClass, _), json) => json match {  
      case JObject(JField("$oid", JString(s)) :: Nil) if (ObjectId.isValid(s)) =>  
        new ObjectId(s)  
      case x => throw new MappingException("Can't convert " + x + " to ObjectId")  
    }  
  }  
  
  def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {  
    case x: ObjectId => 
      JString(x.toString())
  }  
}

trait SerializationRules {
  implicit val formats = Serialization.formats(net.liftweb.json.NoTypeHints) + new ObjectIdSerializer()
}

