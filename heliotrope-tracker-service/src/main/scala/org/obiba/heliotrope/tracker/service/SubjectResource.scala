package org.obiba.heliotrope.tracker.service

import scala.math._
import scala.collection.JavaConversions
import java.net.URI
import org.obiba.heliotrope.util.URIUtils
import org.obiba.heliotrope.tracker.Subject
import net.liftweb.json.JsonAST._
import net.liftweb.json.{Extraction,DefaultFormats}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.WriteConcern
import org.obiba.heliotrope.tracker.IdentifiableComponent
import org.obiba.heliotrope.tracker.Study
import cc.spray.MethodRejection

case class SubjectResource(resource: Resource with ContainerResource, id: Identifier = NoIdentifier(), options: Map[String, String] = Map.empty) 
	extends ObjectResource[Subject] with ContainerResource with NamedResource  {
  
  def withOptions(options: Map[String, String]) = {
    copy(options = options)
  }
  
  def getResourceURI: URI = {
    val parent = resource.getResourceURI.getPath()
    id match {
      case NoIdentifier() =>
        makeURI(parent + "/subject")
      case LabelIdentifier(label) =>
        makeURI(parent + "/subject/" + URIUtils.encodeUriString(label))
      case _ =>
        makeURI("/")
    }
  }

  def getObjectURI(s: Subject): URI = {
    val parent = resource.getResourceURI.getPath()
    makeURI(parent + "/subject/" + URIUtils.encodeUriString(s.identifier))
  }
  
  def toJson(s: Subject): JObject = {
    val json = Extraction.decompose(s).asInstanceOf[JObject]
    val fields = 
      json.obj :+ 
      JField("url", JString(getObjectURI(s).toString)) :+
      JField("study",
        JObject(List(
          JField("identifier", JString(s.studyIdentifier)),
          JField("url", makeURI("/study/" + URIUtils.encodeUriString(s.studyIdentifier)).toString))))
    JObject(fields)
  }
  
  def getContainerQuery(): Option[MongoDBObjectBuilder] = {
    resource.getContainerQuery match {
      case Some(builder) =>
        id match {
          case LabelIdentifier(label) =>
            Some(builder += ("subjectIdentifier" -> label))
          case _ => None
        }
      case _ =>
        None
    }
  }
  
  def findContainerObject(): Option[IdentifiableComponent] = {
    id match {
      case NoIdentifier() => None
      case LabelIdentifier(label) =>
        for (
          query <- resource.getContainerQuery;
          val newQuery = query += "identifier" -> label;
          found <- DAO.subjectDAO.findOne(query.result)
       ) yield (found)
    }
  }

  private def getResult(builder: MongoDBObjectBuilder): Option[List[JField]] = {
    id match {
      case NoIdentifier() =>
        val offsetValue = options.get("offset")
        val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
        val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
        options.get("filter").map { string =>
          builder += "identifier" -> string.r
        }
        val query = builder.result
        val cursor = DAO.subjectDAO.find(query).skip(offset).limit(count)
        val total = DAO.subjectDAO.count(query)
        val values = cursor.toList.map { toJson(_) }
        val actualCount = values.size
        
        Some(List(JField("data", JArray(values)), JField("count", JInt(actualCount)), JField("offset", JInt(offset)), JField("total", JInt(total))))
      
      case LabelIdentifier(label) =>
        builder += "identifier" -> label
        val value = DAO.subjectDAO.findOne(builder.result)
        value match {
          case Some(subject) =>
            Some(List(JField("data", toJson(subject))))
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
   * Updates a study resource. If the resource exists, we update the fields, which can even 
   * include the study identifier. Not that we recommend that. If the study doesn't exist,
   * a new one will be created. 
   */
  override def update(newValue: JObject) = {
    
    id match {
      case NoIdentifier() =>
        super.update(newValue)
      case LabelIdentifier(label) =>
        resource.findContainerObject match {
          case Some(study: Study) =>
            val augmentedValue = JObject(newValue.obj :+ JField("studyIdentifier", JString(study.identifier)))
            updateNamedSubject(label, augmentedValue)
          case _ =>
            MissingResourceResponse(this)
        }
    }
  }
    
  private def updateNamedSubject(identifier: String, newValue: JObject): Response = {

    implicit val formats = DefaultFormats
    
    val existing = for (
      query <- resource.getContainerQuery;
      identifiedQuery = query += "identifier" -> identifier;
      found <- DAO.subjectDAO.findOne(identifiedQuery.result)
    ) yield (found)
    
    
    val newParameters = if (existing.isEmpty) Seq(ResponseParameterCreated()) else Nil
    val newSubject: Subject = Extraction.extract[Subject](newValue)

    existing match {
      case Some(subject) =>
        
        // At this point, we have data, and we have a study value, and we can proceed to 
        // write field data from the request into the study, and then stuff in back into
        // the database. 
        
        if (subject.identifier != newSubject.identifier) {
	      DAO.studyDAO.update(MongoDBObject("identifier" -> subject.identifier), MongoDBObject("identifier" -> newSubject.identifier))
	      DAO.sampleDAO.update(MongoDBObject("subjectIdentifier" -> subject.identifier), MongoDBObject("subjectIdentifier" -> newSubject.identifier))
	      DAO.stepDAO.update(MongoDBObject("subjectIdentifier" -> subject.identifier), MongoDBObject("subjectIdentifier" -> newSubject.identifier))
	    }

        DAO.subjectDAO.update(q = MongoDBObject("identifier" -> newSubject.identifier), t = newSubject, upsert = false, multi = false, wc = WriteConcern.Normal)
        evaluate()
      case _ =>
        // If we're doing a put but don't have a resource, we can actually create a new
        // one. This is common, as a POST (store) on an unlabelled resource behaves like
        // an update on a labelled resource. 
        DAO.subjectDAO.insert(newSubject)
        evaluate() match {
          case ValueResponse(x, y) => ValueResponse(x, y :+ ResponseParameterCreated())
          case other: Response => other
        }
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
