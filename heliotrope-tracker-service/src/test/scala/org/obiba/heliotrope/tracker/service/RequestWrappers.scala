package org.obiba.heliotrope.tracker.service

import cc.spray._
import test._
import http._
import HttpMethods._
import StatusCodes._

trait RequestWrappers extends SprayTest {
  
  def trackerService: Route
  
    /**
   * Helper method to make testing somewhat easier.
   */
  def testRequest(reqType: HttpMethod, request: String): RoutingResultWrapper = {
    test(HttpRequest(reqType, request)) {
      trackerService
    }
  }

  /**
   * Helper method to make testing of POST requests easier.
   */
  def testPostRequest(request: String, content: Option[HttpContent]): RoutingResultWrapper = {
    test(HttpRequest(POST, request, content = content)) {
      trackerService
    }
  }

  /**
   * Helper method to make testing of PUT requests easier.
   */
  def testPutRequest(request: String, content: Option[HttpContent]): RoutingResultWrapper = {
    test(HttpRequest(PUT, request, content = content)) {
      trackerService
    }
  }

  /**
   * Helper method to make testing of POST requests with JSON payloads easier.
   */
  def testPostRequest(request: String, body: String): RoutingResultWrapper = {
    testPostRequest(request, Some(HttpContent(ContentType(MediaTypes.`application/json`), body)))
  }

  /**
   * Helper method to make testing of PUT requests with JSON payloads easier.
   */
  def testPutRequest(request: String, body: String): RoutingResultWrapper = {
    testPutRequest(request, Some(HttpContent(ContentType(MediaTypes.`application/json`), body)))
  }
}
