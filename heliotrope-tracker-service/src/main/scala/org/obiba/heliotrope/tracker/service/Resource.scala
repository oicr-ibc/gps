package org.obiba.heliotrope.tracker.service

import java.net.URI
import org.obiba.heliotrope.tracker.Study
import org.obiba.heliotrope.util._
import cc.spray.typeconversion.SprayJsonSupport
import cc.spray.MethodRejection
import cc.spray.http.HttpMethods
import cc.spray.Rejection
import java.util.regex.Pattern
import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import org.obiba.heliotrope.tracker.IdentifiableComponent

case class MissingResourceException(resource: Resource) extends Exception;
case class InvalidContentException(resource: Resource) extends Exception;

/**
 * Case class for an identifier of a particular resource, e.g., a study or subject name.
 */

abstract class Identifier

case class NoIdentifier() extends Identifier

case class LabelIdentifier(string: String) extends Identifier

/**
 * Abstract class for a resource -- all resources will define an evaluate method. 
 */
abstract class Resource extends SerializationRules {
  
  implicit def uriToString(input: URI): String = input.toString
  implicit def stringToJString(input: String): JString = JString(input)
  implicit def fieldsToValue(input: List[JField]): ValueResponse = ValueResponse(JObject(input ::: commonFields))
  
  def makeURI(s: String): URI = {
    new URI(null.asInstanceOf[String], s, null.asInstanceOf[String])
  }

  val options: Map[String, String]
  
  def withOptions(options: Map[String, String]): Resource

  def evaluate(): Response
  def update(value: JObject): Response = RejectionResponse(MethodRejection(HttpMethods.GET))
  def store(value: JObject): Response = RejectionResponse(MethodRejection(HttpMethods.GET))
  
  def getResourceURI: URI
  
  val currentUriField = JField("requestedResource", JString(getResourceURI))
  lazy val commonFields = List(currentUriField)
}

abstract class ObjectResource[T] extends Resource {
  def getObjectURI(s: T): URI
}

trait ContainerResource {
  def getContainerQuery: Option[MongoDBObjectBuilder]
  def findContainerObject: Option[IdentifiableComponent]
}

trait NamedResource {
  val id: Identifier
  
  def getIdentifier: Option[String] = {
    id match {
      case LabelIdentifier(label) => Some(label)
      case _ => None
    }
  }
}

/**
 * The root resource, corresponding to /, which doesn't evaluate to anything especially
 * useful. 
 */
case class RootResource(options: Map[String, String] = Map.empty) extends Resource {
  
  def getResourceURI: URI = makeURI("/")
  
  def withOptions(options: Map[String, String]) = {
    copy(options = options)
  }

  def evaluate() = MissingResourceResponse(this)
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
//
///**
// * The resource for a list of samples, corresponding to .../sample.
// */
//case class AllSamplesResource(resource: Resource) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    val offset = options.get("offset").flatMap { case Int(x) => Some(x) }.getOrElse(0)
//    val count = options.get("count").flatMap { case Int(x) => Some(x) }.getOrElse(10)
//    val filter = options.get("filter").getOrElse("^")
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        val query = Sample.where(_.study eqs study.id.is).and(_.identifier matches Pattern.compile(filter)).skip(offset).limit(count)
//        ValueResponse(query.fetch)
//      }
//      case ValueResponse(subject: Subject, parameters) => {
//        val query = Sample.where(_.subject eqs subject.id.is).and(_.identifier matches Pattern.compile(filter)).skip(offset).limit(count)
//        ValueResponse(query.fetch)
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//  override def store(options: Map[String, String], value: AnyRef) = {
//    value match {
//      case sample: Sample => {
//        IdentifiedSampleResource(resource, Identifier(sample.identifier.is.toString)).update(options, value)
//      }
//      case _ => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//    }
//  }
//}
//
///**
// * The resource for an identified sample, corresponding to .../sample/xxx */
//case class IdentifiedSampleResource(resource: Resource, id: Identifier) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        optionToResponse(Sample.where(_.study eqs study.id.is).and(_.identifier eqs id.string).get)
//      }
//      case ValueResponse(subject: Subject, parameters) => {
//        optionToResponse(Sample.where(_.subject eqs subject.id.is).and(_.identifier eqs id.string).get)
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//  
//  override def update(options: Map[String, String], value: AnyRef) = {
//    value match {
//      case sample: Sample => {
//        val inner = resource.evaluate(Map.empty)
//        inner match {
//          case ValueResponse(subject: Subject, parameters) => {
//            val study = subject.study.get
//
//            val oldRecord = Sample.where(_.study eqs subject.study.is).and(_.identifier eqs id.string).get
//            val newParameters = if (oldRecord.isEmpty) Seq(ResponseParameterCreated()) else Nil
//
//            sample.study.setFromString(study.toString())
//            sample.subject.setFromString(subject.id.is.toString())
//            sample.identifier.setFromString(id.string)
//            sample.save(true)
//            
//            ValueResponse(sample, newParameters)
//          }
//          case ValueResponse(other: AnyRef, parameters) => 
//          	ExceptionResponse(InvalidContentException(this))
//          case _ => inner
//        }
//      }
//      case _ => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//    }
//  }
//}
//
///**
// * The resource for a list of steps, corresponding to .../step.
// */
//case class AllStepsResource(resource: Resource) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(subject: Subject, parameters) => {
//        ValueResponse(StepProcess.where(_.owner eqs subject.id.is).fetch)
//      }
//      case ValueResponse(sample: Sample, parameters) => {
//        ValueResponse(StepProcess.where(_.owner eqs sample.id.is).fetch)
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//}
//
///**
// * The resource for an identified step, corresponding to .../step/xxx.
// */
//case class IdentifiedStepResource(resource: Resource, id: Identifier) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(subject: Subject, parameters) => {
//        optionToResponse(StepProcess.where(_.owner eqs subject.id.is).and(_.step eqs id.string).get)
//      }
//      case ValueResponse(sample: Sample, parameters) => {
//        optionToResponse(StepProcess.where(_.owner eqs sample.id.is).and(_.step eqs id.string).get)
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//}
//
///**
// * The resource for a list of protocols, corresponding to .../protocol.
// */
//case class AllProtocolsResource(resource: Resource) extends Resource {
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        ValueResponse(study.protocols.is.flatMap { id => StepProtocol.where(_.id eqs id).get })
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//}
//
///**
// * The resource for an identified protocol, corresponding to .../protocol/xxx.
// */
//case class IdentifiedProtocolResource(resource: Resource, id: Identifier) extends Resource {
//
//  def evaluate(options: Map[String, String]) = {
//    val in = resource.evaluate(Map.empty)
//    in match {
//      case ValueResponse(study: Study, parameters) => {
//        optionToResponse(study.protocols.is.collectFirst { case sid => StepProtocol.where(_.identifier eqs id.string).get })
//      }
//      case ValueResponse(value: AnyRef, parameters) => {
//        ExceptionResponse(InvalidContentException(this))
//      }
//      case _ => in
//    }
//  }
//}

/**
 * Extractor for integers, handy as a comfortable way of parsing string data from requests
 */
object Int {
  def unapply(s : String) : Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _ : java.lang.NumberFormatException => None
  }
}
