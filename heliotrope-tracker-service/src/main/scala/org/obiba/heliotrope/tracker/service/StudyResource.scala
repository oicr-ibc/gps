package org.obiba.heliotrope.tracker.service

import scala.math._
import java.net.URI
import org.obiba.heliotrope.tracker.Study
import org.obiba.heliotrope.util.URIUtils
import net.liftweb.json.JsonAST._
import net.liftweb.json.{Extraction,DefaultFormats}
import com.mongodb.DBObject
import com.mongodb.casbah.commons._
import com.mongodb.casbah.WriteConcern
import org.obiba.heliotrope.tracker.IdentifiableComponent

/**
 * The resource for an identified study, corresponding to /study/xxx.
 */
case class StudyResource(id: Identifier = NoIdentifier(), options: Map[String, String] = Map.empty) 
	extends ObjectResource[Study] with ContainerResource with NamedResource {
  
  def withOptions(options: Map[String, String]) = {
    copy(options = options)
  }
  
  def getResourceURI: URI = {
    id match {
      case NoIdentifier() =>
        makeURI("/study")
      case LabelIdentifier(label) =>
        makeURI("/study/" + URIUtils.encodeUriString(label))
      case _ =>
        makeURI("/")
    }
  }

  def getObjectURI(s: Study): URI = {
    makeURI("/study/" + URIUtils.encodeUriString(s.identifier))
  }
  
  def toJson(s: Study): JObject = {
    val json = Extraction.decompose(s).asInstanceOf[JObject]
    JObject(json.obj :+ JField("url", JString(getObjectURI(s).toString)))
  }
  
  def getContainerQuery(): Option[MongoDBObjectBuilder] = {
    id match {
      case LabelIdentifier(label) =>
        Some(new MongoDBObjectBuilder() += ("studyIdentifier" -> label))
      case _ =>
        None
    }
  }
  
  def findContainerObject(): Option[IdentifiableComponent] = {
    id match {
      case LabelIdentifier(label) => 
        DAO.studyDAO.findOne(MongoDBObject("identifier" -> label))
      case _ =>
        None
    }
  }

  /**
   * Evaluates a study resource, searching by the identifier
   */
  def evaluate() = {
    
    id match {
      
      case NoIdentifier() =>
        val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
        val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
        val filterQuery = options.get("filter").map { string =>
          MongoDBObject("identifier" -> string.r)
        }.getOrElse(MongoDBObject.empty)
        
        val cursor = DAO.studyDAO.find(filterQuery).skip(offset).limit(count)
        val total = DAO.studyDAO.count(filterQuery)
        val values = cursor.toList.map { toJson(_ ) }
        List(JField("data", JArray(values)), JField("count", JInt(min(count, total))), JField("offset", JInt(offset)), JField("total", JInt(total)))
        
      case LabelIdentifier(label) =>
        val value = DAO.studyDAO.findOne(MongoDBObject("identifier" -> label))
        value match {
          case Some(study) =>
            List(JField("data", toJson(study)))
          case _ =>
            MissingResourceResponse(this)
        }
        
      case _ =>
        MissingResourceResponse(this)
    }
  }
  
  /**
   * Updates a study resource. If the resource exists, we update the fields, which can even 
   * include the study identifier. Not that we recommend that. If the study doesn't exist,
   * a new one will be created. 
   */
  override def update(newValue: JObject) = {
    
    implicit val formats = DefaultFormats
    
    id match {
      
      case NoIdentifier() =>
        super.update(newValue)

      case LabelIdentifier(label) =>
        val value = DAO.studyDAO.findOne(MongoDBObject("identifier" -> label))
        val newParameters = if (value.isEmpty) Seq(ResponseParameterCreated()) else Nil        
        val newStudy: Study = Extraction.extract[Study](newValue)

        value match {
          case Some(study) =>
            
            // At this point, we have data, and we have a study value, and we can proceed to 
            // write field data from the request into the study, and then stuff in back into
            // the database. 
            
            if (study.identifier != newStudy.identifier) {
		      DAO.studyDAO.update(MongoDBObject("identifier" -> study.identifier), MongoDBObject("identifier" -> newStudy.identifier))
		      DAO.protocolDAO.update(MongoDBObject("studyIdentifier" -> study.identifier), MongoDBObject("studyIdentifier" -> newStudy.identifier))
		      DAO.subjectDAO.update(MongoDBObject("studyIdentifier" -> study.identifier), MongoDBObject("studyIdentifier" -> newStudy.identifier))
		      DAO.sampleDAO.update(MongoDBObject("studyIdentifier" -> study.identifier), MongoDBObject("studyIdentifier" -> newStudy.identifier))
		      DAO.stepDAO.update(MongoDBObject("studyIdentifier" -> study.identifier), MongoDBObject("studyIdentifier" -> newStudy.identifier))
		    }

            DAO.studyDAO.update(q = MongoDBObject("identifier" -> newStudy.identifier), t = newStudy, upsert = false, multi = false, wc = WriteConcern.Normal)
            evaluate()
          case _ =>
            // If we're doing a put but don't have a resource, we can actually create a new
            // one. This is common, as a POST (store) on an unlabelled resource behaves like
            // an update on a labelled resource. 
            DAO.studyDAO.insert(newStudy)
            evaluate() match {
              case ValueResponse(x, y) => ValueResponse(x, y :+ ResponseParameterCreated())
              case other: Response => other
            }
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
