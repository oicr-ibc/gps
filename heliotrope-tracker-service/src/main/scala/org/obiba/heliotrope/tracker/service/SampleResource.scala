package org.obiba.heliotrope.tracker.service

import scala.math._
import scala.collection.JavaConversions
import java.net.URI
import org.obiba.heliotrope.util.URIUtils
import org.obiba.heliotrope.tracker.Sample
import net.liftweb.json.JsonAST._
import net.liftweb.json.{Extraction,DefaultFormats,MappingException}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.WriteConcern
import org.obiba.heliotrope.tracker.IdentifiableComponent
import org.obiba.heliotrope.tracker.Study
import org.obiba.heliotrope.tracker.Subject
import net.liftweb.json.DefaultFormats
import cc.spray.MethodRejection
import cc.spray.MalformedRequestContentRejection
import cc.spray.http.HttpMethods

case class SampleResource(resource: Resource with ContainerResource, id: Identifier = NoIdentifier(), options: Map[String, String] = Map.empty) 
	extends ObjectResource[Sample] with NamedResource with ContainerResource {
  
  def withOptions(options: Map[String, String]) = {
    copy(options = options)
  }
  
  def getResourceURI: URI = {
    val parent = resource.getResourceURI.getPath()
    id match {
      case NoIdentifier() =>
        makeURI(parent + "/sample")
      case LabelIdentifier(label) =>
        makeURI(parent + "/sample/" + URIUtils.encodeUriString(label))
      case _ =>
        makeURI("/")
    }
  }

  def getObjectURI(s: Sample): URI = {
    val parent = resource.getResourceURI.getPath()
    makeURI("/study/" + URIUtils.encodeUriString(s.studyIdentifier) + "/sample/" + URIUtils.encodeUriString(s.identifier))
  }
  
  def toJson(s: Sample): JObject = {
    val json = Extraction.decompose(s).asInstanceOf[JObject]
    val encodedStudyIdentifier = URIUtils.encodeUriString(s.studyIdentifier)
    val encodedSubjectIdentifier = URIUtils.encodeUriString(s.subjectIdentifier)
    val fields = 
      json.obj :+ 
      JField("url", JString(getObjectURI(s).toString)) :+
      JField("study",
        JObject(List(
          JField("identifier", JString(s.studyIdentifier)),
          JField("url", makeURI("/study/" + encodedStudyIdentifier).toString)))) :+
      JField("subject",
        JObject(List(
          JField("identifier", JString(s.subjectIdentifier)),
          JField("url", makeURI("/study/" + encodedStudyIdentifier + "/subject/" + encodedSubjectIdentifier).toString))))
    JObject(fields)
  }
  
  def getContainerQuery(): Option[MongoDBObjectBuilder] = {
    resource.getContainerQuery match {
      case Some(builder) =>
        id match {
          case LabelIdentifier(label) =>
            Some(builder += ("sampleIdentifier" -> label))
          case _ => None
        }
      case _ =>
        None
    }
  }

  def findContainerObject(): Option[IdentifiableComponent] = None

  private def getResult(builder: MongoDBObjectBuilder): Option[List[JField]] = {
    id match {
      case NoIdentifier() =>
        val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
        val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
        options.get("filter").map { string =>
          builder += "identifier" -> string.r
        }
        val query = builder.result
        val cursor = DAO.sampleDAO.find(query).skip(offset).limit(count)
        val total = DAO.sampleDAO.count(query)
        val values = cursor.toList.map { toJson(_) }
        val actualCount = values.size
        
        Some(List(JField("data", JArray(values)), JField("count", JInt(actualCount)), JField("offset", JInt(offset)), JField("total", JInt(total))))
      
      case LabelIdentifier(label) =>
        builder += "identifier" -> label
        val value = DAO.sampleDAO.findOne(builder.result)
        value match {
          case Some(sample) =>
            Some(List(JField("data", toJson(sample))))
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
  
    /**
   * Updates a sample resource. If the resource exists, we update the fields, which can even 
   * include the sample identifier. Not that we recommend that. If the study doesn't exist,
   * a new one will be created. 
   */
  override def update(newValue: JObject) = {
    
    id match {
      case NoIdentifier() =>
        RejectionResponse(MethodRejection(HttpMethods.GET))
      case LabelIdentifier(label) =>
        resource.findContainerObject match {
          
          // If the container is a study, add its identifier to the object for deserialization. 
          case Some(study: Study) =>
            val augmentedValue = JObject(newValue.obj :+ JField("studyIdentifier", JString(study.identifier)))
            updateNamedSample(label, augmentedValue)
          
          // If the container is a subject, add both its identifier and the study identifier
          // to the object for deserialization. 
          case Some(subject: Subject) =>
            val augmentedValue = JObject(newValue.obj
                .:+(JField("studyIdentifier", JString(subject.studyIdentifier)))
                .:+(JField("subjectIdentifier", JString(subject.identifier))))
            updateNamedSample(label, augmentedValue)
          case _ =>
            MissingResourceResponse(this)
        }
    }
  }
    
  private def updateNamedSample(identifier: String, newValue: JObject): Response = {

    implicit val formats = DefaultFormats

    val existing = for (
      query <- resource.getContainerQuery;
      identifiedQuery = query += "identifier" -> identifier;
      found <- DAO.sampleDAO.findOne(identifiedQuery.result)
    ) yield (found)

    val newParameters = if (existing.isEmpty) Seq(ResponseParameterCreated()) else Nil
    
    try {
      val newSample: Sample = Extraction.extract[Sample](newValue)

      existing match {
        case Some(sample) =>
        
          // At this point, we have data, and we have a study value, and we can proceed to 
          // write field data from the request into the study, and then stuff in back into
          // the database. 
        
          DAO.sampleDAO.update(q = MongoDBObject("identifier" -> newSample.identifier), t = newSample, upsert = false, multi = false, wc = WriteConcern.Normal)
          evaluate()
        case _ =>
          // If we're doing a put but don't have a resource, we can actually create a new
          // one. This is common, as a POST (store) on an unlabelled resource behaves like
          // an update on a labelled resource. 
          DAO.sampleDAO.insert(newSample)
          evaluate() match {
            case ValueResponse(x, y) => ValueResponse(x, y :+ ResponseParameterCreated())
            case other: Response => other
          }
      }
    } catch {
      case e: MappingException =>
        RejectionResponse(MalformedRequestContentRejection(e.msg))
    }
  }
  
  /**
   * Stores a study resource. This fakes an update on an identified resource if we find 
   * an identifier. 
   */
  override def store(value: JObject) = {
    value \ "identifier" match {
      case JString(x) =>
        this.copy(id = LabelIdentifier(x)).update(value)
      case _ =>
        super.store(value)
    }
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
