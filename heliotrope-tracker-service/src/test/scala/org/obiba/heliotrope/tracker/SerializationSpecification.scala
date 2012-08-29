//package org.obiba.heliotrope.tracker
//
//import org.specs2.mutable._
//import scala.util.parsing.json._
//import cc.spray._
//import http._
//import MediaTypes._
//import net.liftweb.json.Serialization
//import net.liftweb.json._
//import scala.collection.immutable._
//import java.util.Date
//import org.joda.time.{ DateTime, DateTimeZone }
//
///**
// * Tests the serialization and deserialization of the storage systems.
// */
//class SerializationSpecification extends Specification {
//  implicit val formats = DefaultFormats + StudySerializer + SubjectSerializer + SampleSerializer + StepProtocolSerializer
//
//  "A Study object" should {
//
//    val study = Study.createRecord
//    val contentType = ContentType(`application/json`)
//
//    study.identifier.setFromString("GPS")
//    study.description.setFromString("Description")
//
//    val data = Map.empty[String, Any] + ("startDate" -> new Date(1333041163000L))
//    study.data.setFromAny(data)
//
//    "serialize to JSON" in {
//      val jsonSource = Serialization.write(study)
//
//      "with the correct primary key" in {
//        jsonSource must /("_id" -> study.id.is)
//      }
//
//      "with the correct code" in {
//        jsonSource must /("identifier" -> "GPS")
//      }
//
//      "with the correct description" in {
//        jsonSource must /("description" -> "Description")
//      }
//
//      "with the correct startDate" in {
//        jsonSource must /("data") / ("startDate") / ("$dt" -> "2012-03-29T17:12:43.000Z")
//      }
//    }
//  }
//}