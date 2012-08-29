package org.obiba.heliotrope.tracker.service

import scala.math._
import java.net.URI
import org.obiba.heliotrope.util.URIUtils
import org.obiba.heliotrope.tracker.Step
import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction
import com.mongodb.casbah.commons.MongoDBObjectBuilder

case class StepResource(resource: Resource with ContainerResource, id: Identifier = NoIdentifier(), options: Map[String, String] = Map.empty) 
	extends ObjectResource[Step] {
  
  def withOptions(options: Map[String, String]) = {
    copy(options = options)
  }
  
  def getResourceURI: URI = {
    val parent = resource.getResourceURI.getPath()
    id match {
      case NoIdentifier() =>
        makeURI(parent + "/step")
      case LabelIdentifier(label) =>
        makeURI(parent + "/step/" + URIUtils.encodeUriString(label))
      case _ =>
        makeURI("/")
    }
  }

  def getObjectURI(s: Step): URI = {
    val parent = resource.getResourceURI.getPath()
    makeURI("/study/" + URIUtils.encodeUriString(s.studyIdentifier) +
      (s.subjectIdentifier match {
        case Some(string) =>
          "/subject/" + URIUtils.encodeUriString(string)
        case _ => ""
      }) +
      (s.sampleIdentifier match {
        case Some(string) =>
          "/sample/" + URIUtils.encodeUriString(string)
        case _ => ""
      }) +
      "/step/" + URIUtils.encodeUriString(s.protocolIdentifier))
  }
  
  def toJson(s: Step): JObject = {
    val json = Extraction.decompose(s).asInstanceOf[JObject]
    val encodedStudyIdentifier = URIUtils.encodeUriString(s.studyIdentifier)
    val encodedProtocolIdentifier = URIUtils.encodeUriString(s.protocolIdentifier)
    
    val protocolQuery = new MongoDBObjectBuilder() 
    protocolQuery += "studyIdentifier" -> s.studyIdentifier
    protocolQuery += "identifier" -> s.protocolIdentifier
    val protocol = DAO.protocolDAO.findOne(protocolQuery.result)
    val protocolField = protocol.map { p =>
      JField("data", Extraction.decompose(p.protocolValues()).asInstanceOf[JArray])
    }
    
    val fields = 
      json.obj :+ 
      JField("url", JString(getObjectURI(s).toString)) :+
      JField("protocol",
        JObject(List(
          JField("identifier", JString(s.protocolIdentifier)),
          JField("url", makeURI("/study/" + encodedStudyIdentifier + "/protocol/" + encodedProtocolIdentifier).toString)) ++
          protocolField)) :+
      JField("study",
        JObject(List(
          JField("identifier", JString(s.studyIdentifier)),
          JField("url", makeURI("/study/" + encodedStudyIdentifier).toString))))
    JObject(fields)
  }
  
  private def getResult(builder: MongoDBObjectBuilder): Option[List[JField]] = {
    id match {
      case NoIdentifier() =>
        val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
        val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
        options.get("filter").map { string =>
          builder += "identifier" -> string.r
        }
        val query = builder.result
        val cursor = DAO.stepDAO.find(query).skip(offset).limit(count)
        val total = DAO.stepDAO.count(query)
        val values = cursor.toList.map { toJson(_) }
        val actualCount = values.size
        
        Some(List(JField("data", JArray(values)), JField("count", JInt(actualCount)), JField("offset", JInt(offset)), JField("total", JInt(total))))
      
      case LabelIdentifier(label) =>
        builder += "protocolIdentifier" -> label
        val value = DAO.stepDAO.findOne(builder.result)
        value match {
          case Some(step) =>
            Some(List(JField("data", toJson(step))))
          case _ =>
            None
        }

      case _ =>
        None
    }
  }
  
  /**
   * Evaluates a study resource, searching by the identifier
   */
  def evaluate() = {
    val result = for (
      queryBuilder <- resource.getContainerQuery;
      fields <- getResult(queryBuilder)
    ) yield (fieldsToValue(fields))
    
    result.getOrElse(MissingResourceResponse(this)) 
  }
}

//
///**
// * The resource for a list of subjects, corresponding to .../subject.
// */
//case class AllSubjectsResource(resource: Resource) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
//        val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
//        val filter = options.get("filter").getOrElse("^")
//        val query = Subject.where(_.study eqs study.id.is).and(_.identifier matches Pattern.compile(filter)).skip(offset).limit(count)
//        ValueResponse(query.fetch)
//      }
//      case _ => in
//    }
//  }
//  override def store(options: Map[String, String], value: AnyRef) = {
//    value match {
//      case subject: Subject => {
//        IdentifiedSubjectResource(resource, Identifier(subject.identifier.is.toString)).update(options, value)
//      }
//      case _ => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//    }
//  }
//}
//
///**
// * The resource for an identified subject, corresponding to .../subject/xxx.
// */
//case class IdentifiedSubjectResource(resource: Resource, id: Identifier) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        optionToResponse(Subject.where(_.study eqs study.id.is).and(_.identifier eqs id.string).get)
//      }
//      case _ => in
//    }
//  }
//  override def update(options: Map[String, String], value: AnyRef) = {
//    value match {
//      case subject: Subject => {
//        val inner = resource.evaluate(Map.empty)
//        inner match {
//          case ValueResponse(study: Study, parameters) => {
//            
//            val oldRecord = Subject.where(_.study eqs study.id.is).and(_.identifier eqs id.string).get
//            val newParameters = if (oldRecord.isEmpty) Seq(ResponseParameterCreated()) else Nil
//
//            subject.study.setFromString(study.id.is.toString())
//            subject.identifier.setFromString(id.string)
//            subject.save(true)
//            ValueResponse(subject, newParameters)
//          }
//          case ValueResponse(value: AnyRef, parameters) => {
//            ExceptionResponse(InvalidContentException(this))
//          }
//          case _ => inner
//        }
//      }
//      case _ => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//    }
//  }
//}
