package org.obiba.heliotrope.tracker.service

import org.specs2.mutable._
import cc.spray._
import test._
import http._
import HttpMethods._
import StatusCodes._
import com.mongodb.DB
import java.io.File
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.FileReader
import org.obiba.heliotrope.storage._
import scala.util.parsing.json._
import org.obiba.heliotrope.tracker._
import cc.spray.typeconversion.LiftJsonSupport
import akka.dispatch.Future
import org.obiba.heliotrope.spray.DummyServiceAuthentication
import org.specs2.execute.Pending
import java.util.GregorianCalendar
import com.mongodb.casbah.commons.MongoDBObject


class TrackerServiceSpecification extends MongoSpecification 
	with RequestWrappers 
	with TrackerService 
	with DummyServiceAuthentication
	{
  
  sequential
  
  "The TrackerService" should {

    "when handling GET /" in {
      lazy val req = testRequest(GET, "/")

      "mark the request as handled" in {
        req.handled must beTrue
      }
      
      "response should be a string" in {
        req.response.content.toString() must contain("Spray")
      }
    }

    "when handling GET /study" in {
      lazy val req = testRequest(GET, "/study")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain the identifiers GPS and PNA" in {
        body must (/("data") */("identifier" -> "GPS") and /("data") */("identifier" -> "PNA"))
      }
      "response should contain study URLs" in {
        body must (/("data") */("url" -> "/study/GPS") and /("data") */("url" -> "/study/PNA"))
      }
      "response should start at offset zero" in {
        body must (/("offset" -> 0.0))
      }
      "response should contain two studies" in {
        body must (/("count" -> 2.0))
      }
      "response should have a total of two studies" in {
        body must (/("total" -> 2.0))
      }
      "response should contain primary URL" in {
        body must (/("requestedResource" -> "/study"))
      }
    }
    
    "when handling GET /study/GPS" in {
      lazy val req = testRequest(GET, "/study/GPS")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain the string GPS" in {
        body must (/("data") /("identifier" -> "GPS"))
      }
      "response should contain the correct URL" in {
        body must (/("data") /("url" -> "/study/GPS"))
      }
      "response should contain primary URL" in {
        body must (/("requestedResource" -> "/study/GPS"))
      }
    }
    
    "when handling GET /study/ZZZ" in {
      lazy val req = testRequest(GET, "/study/ZZZ")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
    
    "when handling GET /study/GPS/subject" in {
      lazy val req = testRequest(GET, "/study/GPS/subject")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain TST-001" in {
        body must (/("data") */("identifier" -> "TST-001"))
      }
      "response should not contain PNA-001" in {
        body must not (/("data") */("identifier" -> "PNA-001"))
      }
      "response should contain URL for TST-001" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-001"))
      }
      "response should not contain URL for TST-011" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-011"))
      }
    }

    "when handling GET /study/GPS/subject?offset=10" in {
      lazy val req = testRequest(GET, "/study/GPS/subject?offset=10")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should not contain URL for TST-001" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-001"))
      }
      "response should contain URL for TST-011" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-011"))
      }
    }

    "when handling GET /study/GPS/subject?offset=15" in {
      lazy val req = testRequest(GET, "/study/GPS/subject?offset=15")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should have a count of 5" in {
        body must (/("count" -> 5.0))
      }
    }

    "when handling GET /study/GPS/subject?count=5" in {
      lazy val req = testRequest(GET, "/study/GPS/subject?count=5")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain URL for TST-001" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-001"))
      }
      "response should not contain URL for TST-006" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-006"))
      }
    }

    "when handling GET /study/GPS/subject?count=5&offset=5" in {
      lazy val req = testRequest(GET, "/study/GPS/subject?count=5&offset=5")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain URL for TST-006" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-006"))
      }
      "response should not contain URL for TST-011" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-011"))
      }
    }

    "when handling GET /study/GPS/subject?filter=TST-01%5B23%5D" in {
      lazy val req = testRequest(GET, "/study/GPS/subject?filter=TST-01%5B23%5D")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should not contain URL for TST-001" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-001"))
      }
      "response should contain URL for TST-012" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-012"))
      }
      "response should contain URL for TST-013" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-013"))
      }
      "response should not contain URL for TST-011" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-011"))
      }
      "response should not contain URL for TST-014" in {
        body must not (/("data") */("url" -> "/study/GPS/subject/TST-011"))
      }
    }

    "when handling GET /study/PNA" in {
      lazy val req = testRequest(GET, "/study/PNA")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain the string PNA" in {
        body must (/("data") */("identifier" -> "PNA"))
      }
      "response should not contain the string GPS" in {
        body must not (/("data") */("identifier" -> "GPS"))
      }
    }

    "when handling GET /study/GPS/subject/TST-013" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-013")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain URL for TST-013" in {
        body must (/("data") /("url" -> "/study/GPS/subject/TST-013"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") /("study") /("identifier" -> "GPS"))
      }
      "response should contain URL for GPS" in {
        body must (/("data") /("study") /("url" -> "/study/GPS"))
      }
      "response should not contain URL for PNA" in {
        body must not (/("data") /("study") /("url" -> "/study/PNA"))
      }
    }

    "when handling GET /study/PNA/subject/TST-013" in {
      lazy val req = testRequest(GET, "/study/PNA/subject/TST-013")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain URL for TST-013" in {
        body must (/("data") /("url" -> "/study/PNA/subject/TST-013"))
      }
      "response should contain the PNA study identifier" in {
        body must (/("data") /("study") /("identifier" -> "PNA"))
      }
      "response should contain URL for PNA" in {
        body must (/("data") /("study") /("url" -> "/study/PNA"))
      }
      "response should not contain URL for GPS" in {
        body must not (/("data") /("study") /("url" -> "/study/GPS"))
      }
    }

    "when handling GET /study/GPS/sample" in {
      lazy val req = testRequest(GET, "/study/GPS/sample")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-001-BIO-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-001-BIO-001"))
      }
      "response should contain SAMPLE-TST-002-BIO-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-002-BIO-001"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") */("study") */("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") */("study") */("url" -> "/study/GPS"))
      }
    }

    "when handling GET /study/GPS/sample/SAMPLE-TST-001-BIO-001" in {
      lazy val req = testRequest(GET, "/study/GPS/sample/SAMPLE-TST-001-BIO-001")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-001-BIO-001" in {
        body must (/("data") /("identifier" -> "SAMPLE-TST-001-BIO-001"))
      }
      "response should not contain SAMPLE-TST-002-BIO-001" in {
        body must not (/("data") /("identifier" -> "SAMPLE-TST-002-BIO-001"))
      }
      "response should contain the sample URL" in {
        body must (/("data") /("url" -> "/study/GPS/sample/SAMPLE-TST-001-BIO-001"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") /("study") /("url" -> "/study/GPS"))
      }
      "responses should contain the GPS study identifier" in {
        body must (/("data") /("study") /("identifier" -> "GPS"))
      }
      "response should contain the subject URL" in {
        body must (/("data") /("subject") /("url" -> "/study/GPS/subject/TST-001"))
      }
      "response should contain the TST-001 subject identifier" in {
        body must (/("data") /("subject") /("identifier" -> "TST-001"))
      }
    }

    "when handling GET /study/GPS/sample/SAMPLE-TST-001-BIO-XXX" in {
      lazy val req = testRequest(GET, "/study/GPS/sample/SAMPLE-TST-001-BIO-XXX")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }

    "when handling GET /study/GPS/subject/TST-001/sample" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/sample")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-001-BIO-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-001-BIO-001"))
      }
      "response should not contain SAMPLE-TST-002-BIO-001" in {
        body must not (/("data") */("identifier" -> "SAMPLE-TST-002-BIO-001"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") */("study") */("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") */("study") */("url" -> "/study/GPS"))
      }
      "response should contain the GPS subject identifier" in {
        body must (/("data") */("subject") */("identifier" -> "TST-001"))
      }
      "response should contain the GPS subject URL" in {
        body must (/("data") */("subject") */("url" -> "/study/GPS/subject/TST-001"))
      }
    }

    "when handling GET /study/GPS/subject/TST-013/sample" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-013/sample")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-013-BIO-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-013-BIO-001"))
      }
      "response should contain SAMPLE-TST-013-BIO-002" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-013-BIO-002"))
      }
      "response should not contain SAMPLE-TST-013-ARC-001" in {
        body must not (/("data") */("identifier" -> "SAMPLE-TST-013-ARC-001"))
      }
    }

    "when handling GET /study/PNA/subject/TST-013/sample" in {
      lazy val req = testRequest(GET, "/study/PNA/subject/TST-013/sample")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-013-BIO-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-013-BIO-001"))
      }
      "response should not contain SAMPLE-TST-013-BIO-002" in {
        body must not (/("data") */("identifier" -> "SAMPLE-TST-013-BIO-002"))
      }
      "response should contain SAMPLE-TST-013-ARC-001" in {
        body must (/("data") */("identifier" -> "SAMPLE-TST-013-ARC-001"))
      }
    }

    "when handling GET /study/GPS/subject/TST-001/sample/SAMPLE-TST-001-BIO-001" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/sample/SAMPLE-TST-001-BIO-001")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain SAMPLE-TST-001-BIO-001" in {
        body must (/("data") /("identifier" -> "SAMPLE-TST-001-BIO-001"))
      }
      "response should not contain SAMPLE-TST-001-BIO-002" in {
        body must not (/("data") /("identifier" -> "SAMPLE-TST-001-BIO-002"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") /("study") /("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") /("study") /("url" -> "/study/GPS"))
      }
      "response should contain the GPS subject identifier" in {
        body must (/("data") /("subject") /("identifier" -> "TST-001"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") /("subject") /("url" -> "/study/GPS/subject/TST-001"))
      }
    }

    "when handling GET /study/GPS/subject/TST-001/sample/SAMPLE-TST-002-BIO-001" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/sample/SAMPLE-TST-002-BIO-001")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }

    "when handling GET /study/GPS/protocol" in {
      lazy val req = testRequest(GET, "/study/GPS/protocol")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain a protocol with the name enrolment" in {
        body must (/("data") */("identifier" -> "enrolment"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") */("study") */("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") */("study") */("url" -> "/study/GPS"))
      }
      "response should contain the protocol URL" in {
        body must (/("data") */("url" -> "/study/GPS/protocol/enrolment"))
      }
    }

    "when handling GET /study/GPS/protocol/enrolment" in {
      lazy val req = testRequest(GET, "/study/GPS/protocol/enrolment")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response must be a protocol with the name enrolment" in {
        body must (/("data") /("identifier" -> "enrolment"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") /("study") /("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") /("study") /("url" -> "/study/GPS"))
      }
      "response should contain the protocol URL" in {
        body must (/("data") /("url" -> "/study/GPS/protocol/enrolment"))
      }
      "response should contain a field gender" in {
        body must (/("data") /("values") */("name" -> "gender"))
      }
      "response should contain a field institution" in {
        body must (/("data") /("values") */("name" -> "institution"))
      }
      "response should contain a field primaryTissue" in {
        body must (/("data") /("values") */("name" -> "primaryTissue"))
      }
      "response should contain a field primaryPhysician" in {
        body must (/("data") /("values") */("name" -> "primaryPhysician"))
      }
      // Additional fields added as part of a step definition
      "response should contain a field completed" in {
        body must (/("data") /("values") */("name" -> "completed"))
      }
      "response should contain a field lastUpdated" in {
        body must (/("data") /("values") */("name" -> "lastUpdated"))
      }
      "response should contain a field lastUpdatedBy" in {
        body must (/("data") /("values") */("name" -> "lastUpdatedBy"))
      }
    }

    "when handling GET /study/GPS/protocol/sampleRegistration" in {
      lazy val req = testRequest(GET, "/study/GPS/protocol/sampleRegistration")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response must be a protocol with the name sampleRegistration" in {
        body must (/("data") /("identifier" -> "sampleRegistration"))
      }
      "response must not be a protocol with the name enrolment" in {
        body must not (/("data") /("identifier" -> "enrolment"))
      }
      "response should contain the GPS study identifier" in {
        body must (/("data") /("study") /("identifier" -> "GPS"))
      }
      "response should contain the GPS study URL" in {
        body must (/("data") /("study") /("url" -> "/study/GPS"))
      }
      "response should contain the protocol URL" in {
        body must (/("data") /("url" -> "/study/GPS/protocol/sampleRegistration"))
      }
      "response should contain a field source" in {
        body must (/("data") /("values") */("name" -> "source"))
      }
      // Additional fields added as part of a step definition
      "response should contain a field completed" in {
        body must (/("data") /("values") */("name" -> "completed"))
      }
      "response should contain a field lastUpdated" in {
        body must (/("data") /("values") */("name" -> "lastUpdated"))
      }
      "response should contain a field lastUpdatedBy" in {
        body must (/("data") /("values") */("name" -> "lastUpdatedBy"))
      }
    }

    "when handling GET /study/GPS/subject/TST-001/step" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/step")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain a step owned by TST-001" in {
        body must (/("data") */("subjectIdentifier" -> "TST-001"))
      }
      "response should contain the step URL" in {
        body must (/("data") */("url" -> "/study/GPS/subject/TST-001/step/enrolment"))
      }
      "response should contain the step protocol URL" in {
        body must (/("data") */("protocol") */("url" -> "/study/GPS/protocol/enrolment"))
      }
    }

    "when handling GET /study/GPS/subject/TST-001/step/enrolment" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/step/enrolment")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should be a step with the name enrolment" in {
        body must (/("data") /("subjectIdentifier" -> "TST-001"))
      }
      "response should contain the step URL" in {
        body must (/("data") /("url" -> "/study/GPS/subject/TST-001/step/enrolment"))
      }
      "response should contain the step protocol URL" in {
        body must (/("data") /("protocol") /("url" -> "/study/GPS/protocol/enrolment"))
      }
      "response should contain the correct data value for gender" in {
        body must (/("data") /("data") */("gender" -> "F"))
      }
      "response should contain the correct data value for primary tissue" in {
        body must (/("data") /("data") */("primaryTissue" -> "breast"))
      }
      "response should contain a field gender" in {
        body must (/("data") /("protocol") /("data") */("name" -> "gender"))
      }
      "response should contain a field primaryTissue" in {
        body must (/("data") /("protocol") /("data") */("name" -> "primaryTissue"))
      }
      // Additional fields added as part of a step definition
      "response should contain a field completed" in {
        body must (/("data") /("data") /("completed" -> "2011-06-05T04:00:00.000Z"))
      }
      "response should contain a field lastUpdated" in {
        body must (/("data") /("data") /("lastUpdated" -> "2011-06-05T04:00:00.000Z"))
      }
      "response should contain a field lastUpdatedBy" in {
        body must (/("data") /("data") /("lastUpdatedBy" -> "admin"))
      }
      "response should contain a field gender" in {
        body must (/("data") /("protocol") /("data") */("name" -> "completed"))
      }
      "response should contain a field gender" in {
        body must (/("data") /("protocol") /("data") */("name" -> "lastUpdated"))
      }
      "response should contain a field gender" in {
        body must (/("data") /("protocol") /("data") */("name" -> "lastUpdatedBy"))
      }
    }

    "when handling GET /study/GPS/subject/TST-001/sample/SAMPLE-TST-001-BIO-001/step" in {
      lazy val req = testRequest(GET, "/study/GPS/subject/TST-001/sample/SAMPLE-TST-001-BIO-001/step")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should be valid JSON" in {
        JSON parseFull body must beSome
      }
      "response should contain a step owned by TST-001" in {
        body must (/("data") */("subjectIdentifier" -> "TST-001"))
      }
      "response should contain a step owned by SAMPLE-TST-001-BIO-001" in {
        body must (/("data") */("sampleIdentifier" -> "SAMPLE-TST-001-BIO-001"))
      }
      "response should contain a property dnaQuality with value 'moderate'" in {
        body must (/("data") */("dnaQuality" -> "moderate"))
      }
    }

    "when handling PUT /study/xxx with a body" in {
      lazy val req = testPutRequest("/study/xxx", """
{"identifier": "xxx", "description":"My description", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get
      lazy val reqNew = testRequest(GET, "/study/xxx")
      lazy val bodyNew = reqNew.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '201 Created'" in {
        req.response.status must_== StatusCodes.Created
      }
      "response should contain the specified identifier" in {
        body must /("data") /("identifier" -> "xxx")
      }
      "response should contain the specified description" in {
        body must /("data") /("description" -> "My description")
      }
      "subsequent request marks the request as handled" in {
        reqNew.handled must beTrue
      }
      "subsequent request response should be a string" in {
        reqNew.response.content.as[String](StringUnmarshaller).isRight must beTrue
      }
      "subsequent request response should be valid JSON" in {
        JSON parseFull bodyNew must beSome
      }
      "subsequent request response should contain the identifier xxx" in {
        bodyNew must /("data") /("identifier" -> "xxx")
      }
    }

    "when handling POST /study with a body" in {
      lazy val req = testPostRequest("/study", """
{"identifier":"yyy", "description":"POST description", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get
      lazy val reqNew = testRequest(GET, "/study/yyy")
      lazy val bodyNew = reqNew.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should contain the specified identifier" in {
        body must /("data") /("identifier" -> "yyy")
      }
      "response should contain the specified description" in {
        body must /("data") /("description" -> "POST description")
      }
      "subsequent request marks the request as handled" in {
        reqNew.handled must beTrue
      }
      "subsequent request response should be a string" in {
        reqNew.response.content.as[String](StringUnmarshaller).isRight must beTrue
      }
      "subsequent request response should be valid JSON" in {
        JSON parseFull bodyNew must beSome
      }
      "subsequent request response should contain the identifier yyy" in {
        bodyNew must /("data") /("identifier" -> "yyy")
      }
    }

    "when handling PUT /study/MISSING/subject/TST-001 with a body" in {
      lazy val req = testPutRequest("/study/MISSING/subject/TST-001", """
{"identifier": "TST-001", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
      

    "when handling PUT /study/GPS/subject/NEW-001 with a body" in {
      lazy val req = testPutRequest("/study/GPS/subject/NEW-001", """
{"identifier": "NEW-001", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get
      lazy val reqNew = testRequest(GET, "/study/GPS/subject/NEW-001")
      lazy val bodyNew = reqNew.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '201 Created'" in {
        req.response.status must_== StatusCodes.Created
      }
      "response should contain the specified identifier" in {
        body must /("data") /("identifier" -> "NEW-001")
      }
      "study should contain the specified identifier" in {
        body must /("data") /("study") /("identifier" -> "GPS")
      }
      "subsequent request should mark the request as handled" in {
        reqNew.handled must beTrue
      }
      "subsequent request response status should be '200 OK'" in {
        reqNew.response.status must_== StatusCodes.OK
      }
      "subsequent request response should be a string" in {
        reqNew.response.content.as[String](StringUnmarshaller).isRight must beTrue
      }
      "subsequent request response should be valid JSON" in {
        JSON parseFull bodyNew must beSome
      }
      "subsequent request response should contain the identifier NEW-001" in {
        bodyNew must /("data") /("identifier" -> "NEW-001")
      }
      "subsequent request response should contain the GPS study identifier" in {
        bodyNew must /("data") /("study") /("identifier" -> "GPS")
      }
    }

    "when handling POST /study/MISSING/subject with a body" in {
      lazy val req = testPostRequest("/study/MISSING/subject", """
{"identifier":"TST-001", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }

    "when handling PUT /study/GPS/subject/TST-001/sample/SAMPLE-TST-001-NEW-001 with a body" in {
      lazy val req = testPutRequest("/study/GPS/subject/TST-001/sample/SAMPLE-TST-001-NEW-001", """
{"identifier": "SAMPLE-TST-001-NEW-001", "data":{"creator":"swatt"}}
      """)
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get
      lazy val reqNew = testRequest(GET, "/study/GPS/subject/TST-001/sample/SAMPLE-TST-001-NEW-001")
      lazy val bodyNew = reqNew.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response should contain the specified identifier" in {
        body must /("data") /("identifier" -> "SAMPLE-TST-001-NEW-001")
      }
      "response status should be '201 Created'" in {
        req.response.status must_== StatusCodes.Created
      }
      "study should contain the specified identifier" in {
        body must /("data") /("study") /("identifier" -> "GPS")
      }
      "subject should contain the specified identifier" in {
        body must /("data") /("subject") /("identifier" -> "TST-001")
      }
      "subsequent request should mark the request as handled" in {
        reqNew.handled must beTrue
      }
      "subsequent request response status should be '200 OK'" in {
        reqNew.response.status must_== StatusCodes.OK
      }
      "subsequent request response should be a string" in {
        reqNew.response.content.as[String](StringUnmarshaller).isRight must beTrue
      }
      "subsequent request response should be valid JSON" in {
        JSON parseFull bodyNew must beSome
      }
      "subsequent request response should contain the identifier SAMPLE-TST-001-NEW-001" in {
        bodyNew must /("data") /("identifier" -> "SAMPLE-TST-001-NEW-001")
      }
      "subsequent request response should contain the GPS study identifier" in {
        bodyNew must /("data") /("study") /("identifier" -> "GPS")
      }
      "subsequent request response should contain the NEW-001 subject identifier" in {
        bodyNew must /("data") /("subject") /("identifier" -> "TST-001")
      }
    }
    
    "when handling PUT /study/MISSING/subject/TST-001/sample/SAMPLE-NEW-001-BIO-001 with a body" in {
      lazy val req = testPutRequest("/study/MISSING/subject/TST-001/sample/SAMPLE-NEW-001-BIO-001", "{\"data\":{\"creator\":\"swatt\"}}")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
      
    "when handling PUT /study/GPS/subject/MISSING-XXX/sample/SAMPLE-NEW-001-BIO-001 with a body" in {
      lazy val req = testPutRequest("/study/GPS/subject/MISSING-XXX/sample/SAMPLE-NEW-001-BIO-001", "{\"data\":{\"creator\":\"swatt\"}}")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
      
    "when handling POST /study/MISSING/subject/TST-001/sample with a body" in {
      lazy val req = testPostRequest("/study/MISSING/subject/TST-001/sample", "{\"identifier\":\"SAMPLE-NEW-001-BIO-001\", \"data\":{\"creator\":\"swatt\"}}")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
      
    "when handling POST /study/GPS/subject/MISSING-XXX/sample with a body" in {
      lazy val req = testPostRequest("/study/GPS/subject/MISSING-001/sample", "{\"identifier\":\"SAMPLE-NEW-001-BIO-001\", \"data\":{\"creator\":\"swatt\"}}")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beTrue
      }
      "response status should be '404 Not Found'" in {
        req.response.status must_== StatusCodes.NotFound
      }
    }
      
    "when handling POST /study/GPS/sample" in {
      lazy val req = testPostRequest("/study/GPS/sample", """{"identifier":"SAMPLE-TST-001-BIO-003","source":"FFPE","type":"","dnaQuality":"","dnaConcentration":""}""")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beFalse
      }
    }
      
    "when handling PUT /study/GPS/sample/SAMPLE-TST-001-BIO-003" in {
      lazy val req = testPutRequest("/study/GPS/sample/SAMPLE-TST-001-BIO-003", """{"source":"FFPE","type":"","dnaQuality":"","dnaConcentration":""}""")
      lazy val body = req.response.content.as[String](StringUnmarshaller).right.get

      "mark the request as handled" in {
        req.handled must beFalse
      }
    }

    "leave GET requests to other paths unhandled" in {
      test(HttpRequest(GET, "/kermit")) {
        trackerService
      }.handled must beFalse
    }

    "return a MethodNotAllowed error for POST requests to the root path" in {
      test(HttpRequest(POST, "/")) {
        trackerService
      }.rejections mustEqual Set(MethodRejection(GET))
    }

  }
}

