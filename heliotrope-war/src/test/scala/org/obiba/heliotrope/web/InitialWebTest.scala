package org.obiba.heliotrope.web

import net.liftweb.http.SessionVar
import net.liftweb.mockweb.MockWeb
import org.specs.Specification
import org.specs.runner.JUnit4
import net.liftweb.http.S
import net.liftweb.mockweb.WebSpec
import net.liftweb.common.Full
import net.liftweb.mocks.MockHttpServletRequest

class InitialWebTest extends JUnit4(InitialWebSpecs)

object InitialWebSpecs extends WebSpec {

  val testUrl = "http://foo.com/test/this?foo=bar"
  val testReq = new MockHttpServletRequest(testUrl, "/test")

  // Create a new session for use in the tests
  val testSession = MockWeb.testS(testUrl) {
    S.session
  }

  object TestVar extends SessionVar[String]("Empty")

  "properly set up S with a String url and session" withSFor(testUrl, testSession) in {
    TestVar("foo!")
    TestVar.is must_== "foo!"
  }

  "properly set up S with a HttpServletRequest" withSFor(testReq) in {
    S.uri must_== "/this"
    S.param("foo") must_== Full("bar")
  }
}