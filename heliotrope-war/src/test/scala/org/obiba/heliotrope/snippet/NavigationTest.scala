package org.obiba.heliotrope.snippet {

import org.specs._
import org.specs.runner.JUnit4
import org.specs.runner.ConsoleRunner
import net.liftweb._
import http._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.builtin.snippet._
import org.specs.matcher._
import org.specs.specification._
import Helpers._
import net.liftweb.mockweb.{WebSpec,MockWeb}
import net.liftweb.mocks.MockHttpServletRequest
import net.liftweb.sitemap.{SiteMap,Loc}
import org.obiba.heliotrope.menu.InitialSiteMap
import org.obiba.heliotrope.storage.MongoTestEnvironment
import scala.xml.MetaData
import org.obiba.heliotrope.domain.User
import org.specs.mock.Mocker
import net.liftweb.sitemap.MenuItem
import org.obiba.heliotrope.menu.StudyInfo

class NavigationTestSpecsAsTest extends JUnit4(NavigationTestSpecs)
object NavigationTestSpecsRunner extends ConsoleRunner(NavigationTestSpecs)

object NavigationBoot extends Loggable {
  def boot() {
    InitialSiteMap.setupSiteMap
    
    LiftRules.htmlProperties.default.set((r: Req) =>
    	new Html5Properties(r.userAgent))  
    	
    LiftRules.addToPackages("org.obiba.heliotrope")
  }
}

object NavigationTestSpecs extends WebSpec(NavigationBoot.boot _) with MongoTestEnvironment with Mocker {

  doBeforeSpec { prepareMongo() }

  doAfterSpec { cleanupMongo() }
  
  def hasLoc() {
    S.request match {
      case Full(req) => {
        logger.debug(req.toString())
        LiftRules.siteMap.map { sm => 
          val loc = sm.findLoc(req)
          loc must notBeEmpty
        }
      }
      case _ => fail("No request in S")
    }    
  }
  
  "SiteMap" should {
    
    "have a /study loc" withSFor(new MockHttpServletRequest("http://foo.com/test/study", "/test")) in {
      hasLoc()
    }

    "have a /status loc" withSFor(new MockHttpServletRequest("http://foo.com/test/status", "/test")) in {
      hasLoc()
    }

    "have a /sample loc" withSFor(new MockHttpServletRequest("http://foo.com/test/sample", "/test")) in {
      hasLoc()
    }

    "have a /submission loc" withSFor(new MockHttpServletRequest("http://foo.com/test/submission", "/test")) in {
      hasLoc()
    }
     
    "have an index loc" withSFor(new MockHttpServletRequest("http://foo.com/test", "/test")) in {
      hasLoc()
    }

    "have a /study/GPS loc" withSFor(new MockHttpServletRequest("http://foo.com/test/study/GPS", "/test")) in {
      hasLoc()
    }

    "have a /study/GPS/subject loc" withSFor(new MockHttpServletRequest("http://foo.com/test/study/GPS/subject", "/test")) in {
      hasLoc()
    }

    "have a /study/GPS/subject/new loc" withSFor(new MockHttpServletRequest("http://foo.com/test/study/GPS/subject/new", "/test")) in {
      hasLoc()
    }
    
    "have a simple menu when not logged in" withSFor(new MockHttpServletRequest("http://foo.com/test/index", "/test")) in {
      val out = Menu.builder(<span></span>).toString()
      out must include("Home")
      out must notInclude("Submissions")
      out must notInclude("Studies")
      out must notInclude("Status")
    }

    "have a full menu when logged in" withSFor(new MockHttpServletRequest("http://foo.com/test/index", "/test")) in {
      User.logUserIn(User.findOrCreateUser("test").open_!)
      val out = Menu.builder(<span></span>).toString()
      out must include("Home")
      out must include("Submissions")
      out must include("Studies")
      out must include("Status")
    }
    
    "have a menu group when logged in" withSFor(new MockHttpServletRequest("http://foo.com/test/study/GPS", "/test")) in {
      User.logUserIn(User.findOrCreateUser("test").open_!)
      val item = <ul><li><a href="yyy">XXX</a></li></ul>
      S.withAttrs(item.first.attributes) {        
        val out = StudyMenu.render(item).toString()
        out must include("/study/GPS")
        out must include("Subjects for study")
        out must include("New subject for study")
      }
    }

    "disable a menu item for same page" withSFor(new MockHttpServletRequest("http://foo.com/test/study/GPS/subject", "/test")) in {
      User.logUserIn(User.findOrCreateUser("test").open_!)
      val item = <ul><li><a href="yyy">XXX</a></li></ul>      
      S.withAttrs(item.first.attributes) {
        val out = StudyMenu.render(item).toString()
        
        // Checks that the link isn't present, but the text is
        out must notInclude("href=\"/study/GPS/subject\"")
        out must include("Subjects for study")
      }
    }
  }
  
  "Subject menus" should {
    "have a /subject loc" withSFor(new MockHttpServletRequest("http://foo.com/test/subject", "/test")) in { 
      hasLoc()
    }    

    "have a /subject/GEN-001 loc" withSFor(new MockHttpServletRequest("http://foo.com/test/subject/GEN-001", "/test")) in {
      hasLoc()
    }
  }
}

}

