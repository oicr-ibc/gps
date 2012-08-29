package org.obiba.heliotrope.snippet
import scala.xml.NodeSeq
import scala.xml.Text
import net.liftweb.http.S
import java.net.URI
import net.liftweb.common.Loggable

class SelectLocale extends Loggable {

  def render( xhtml: NodeSeq ) : NodeSeq  = {
    val locale = S.attr("locale")
        
    //logger.debug("Locale parameter: " + locale)
    //logger.debug("Locale: " + S.locale.getLanguage())
    if (locale.equals(S.locale.getLanguage())) {
      Text("") 
    } else {
      val localeParameter = "locale=" + locale.openOr("en")
      val location = S.uri
      //logger.debug("Query string: " + S.queryString)
      //logger.debug("Components: " + S.queryString.map { x: String => (x.split("&").filterNot { x: String => "".equals(x) || x.startsWith("locale=") } :+ localeParameter) })
      val queryString = S.queryString.map { x: String =>
        "?" + ((x.split("&").filterNot { x: String => x.startsWith("locale=") } :+ localeParameter).mkString("&"))
      }
      //logger.debug(queryString)
      val url = location + queryString.openOr("?" + localeParameter)
      <a href={ url }>{ xhtml }</a>
    }
  }
}