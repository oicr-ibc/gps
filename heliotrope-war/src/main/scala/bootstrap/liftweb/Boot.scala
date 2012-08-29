package bootstrap.liftweb

import net.liftweb._
import util._
import net.liftweb.http._
import net.liftweb.http.auth._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import net.liftweb.sitemap.Menu._
import Helpers._
import common.Full
import org.obiba.heliotrope.auth.{UserAuth, LDAPAuthModule}
import org.obiba.heliotrope.domain.User
import org.obiba.heliotrope.api.StudyRestService
import org.obiba.heliotrope.storage.MongoConfig
import org.obiba.heliotrope.menu.StudyLoc
import org.obiba.heliotrope.menu.InitialSiteMap
import java.net.URL
import java.util.Locale
import net.liftweb.common.Box
import net.liftweb.http.provider.HTTPCookie
import net.liftweb.http.provider.HTTPRequest
import net.liftweb.common.Empty
import scala.xml.Text
import org.obiba.heliotrope.snippet.StudyMenu
import net.liftweb.mapper.MapperRules
import net.liftweb.mapper.BaseMapper

case class StudyMenuInfo(theStudyId: String)

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  
  def localeCalculator(request : Box[HTTPRequest]): Locale = 
      request.flatMap(r => {
        def localeCookie(in: String): HTTPCookie = {
          HTTPCookie("selected.locale",Full(in),
            Full(S.hostName).filter { ! _.equals("localhost") },
            Full(S.contextPath),Full(2629743),Empty,Empty)
        }
        def localeFromString(in: String): Locale = {
          val x = in.split("_").toList; new Locale(x.head,x.last)
        }
        def calcLocale: Box[Locale] = {
          S.findCookie("selected.locale").map(
            _.value.map(localeFromString)
          ).openOr(Full(LiftRules.defaultLocaleCalculator(request)))
        }
        S.param("locale") match {
          case Full(null) => calcLocale
          case f@Full(selectedLocale) => {
            S.addCookie(localeCookie(selectedLocale))
            tryo(localeFromString(selectedLocale))
          }
          case _ => calcLocale
        }
      }).openOr(Locale.getDefault())
      
  def boot {
    
    LiftRules.htmlProperties.default.set((r: Req) =>
    	new Html5Properties(r.userAgent))  
  
    LiftRules.configureLogging()
    LiftRules.localeCalculator = localeCalculator _
    
    // where to search snippet
    LiftRules.addToPackages("org.obiba.heliotrope")
    
    InitialSiteMap.setupSiteMap
      
    LiftRules.dispatch.append {
      case Req("logout" :: Nil, _, GetRequest) => S.request.foreach(_.request.session.terminate) //
        S.redirectTo("/index")
    }
    
    LiftRules.dispatch.append(StudyRestService)

	UserAuth.register(LDAPAuthModule)

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
      Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)
    
    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
      Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))
    
    MongoConfig.init
  }
}
