package org.obiba.heliotrope.tracker.service

import cc.spray.Rejection
import net.liftweb.json.JsonAST.JObject
import net.liftweb.json.JsonAST.JValue

/**
 * The message sent to the actor for handling a GET request. This encapsulates the resource
 * and a completer function, which will be called on the result of the evaluate method on
 * that resource to generate the response to the client. This is a partial function, as the
 * completer function isn't set up to serialize just anything. 
 */
abstract class Request {
  val resource: Resource
  val options: Map[String, String]
};

/**
 * Case class for a request to get a resource. 
 */
case class GetRequest(resource: Resource, options: Map[String, String] = Map.empty) extends Request;

/**
 * Case class for a request to put a resource - this writes the value over the named
 * resource, which might or might not exist. 
 */
case class PutRequest(value: JObject, resource: Resource, options: Map[String, String] = Map.empty) extends Request;

/**
 * Case class for a request to write a resource - this writes the value at the named
 * resource, but not over it.
 */
case class PostRequest(value: JObject, resource: Resource, options: Map[String, String] = Map.empty) extends Request;

/**
 * Abstract class for a response parameter. These are a set of case classes that will
 * be added into the response sent back to the HttpService actor, which will typically
 * use them to modify/transform the Http response. 
 */
abstract class ResponseParameter;

/**
 * Case class for a "created" response parameter. This doesn't need much in the way
 * of any arguments (some do) but ought to inform generation of the status code 201
 * rather than 200, when a new resource has been created. 
 */
case class ResponseParameterCreated() extends ResponseParameter;

/**
 * Abstract class for a response. There are different types of response, as a response
 * may be pass to the HttpService either as a value to be serialized, as a rejection, or
 * as an exception. 
 */
abstract class Response {
  val value: AnyRef
  val parameters: Seq[ResponseParameter];
}

abstract class ResponseValue;

case class NoValue() extends ResponseValue

case class ObjectValue(fields: Product) extends ResponseValue

case class PagedListValue(items: List[Product], offset: Int, count: Int, total: Int) extends ResponseValue

/**
 * Case class for a value response - these should be serialized by the HttpService.
 */
case class ValueResponse(value: JValue, parameters: Seq[ResponseParameter] = Seq()) extends Response;

/**
 * Case class for an exception response. Typically these should (by default) generate a 500
 * error, although other parameters and the type of exception could modify that. 
 */
case class ExceptionResponse(value: Throwable, parameters: Seq[ResponseParameter] = Seq()) extends Response;

/**
 * Case class for a rejection response. This allows the business layer to send back a spray
 * rejection. 
 */
case class RejectionResponse(value: Rejection, parameters: Seq[ResponseParameter] = Seq()) extends Response;

/**
 * Case class for a missing resource response. This allows the business layer to send back an appropriate
 * response. 
 */
case class MissingResourceResponse(value: Resource, parameters: Seq[ResponseParameter] = Seq()) extends Response;

