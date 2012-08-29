package org.obiba.heliotrope.snippet

import net.liftweb.util.Helpers._
import net.liftweb.sitemap.Loc
import net.liftweb.http.S
import scala.xml.Text

class Breadcrumbs {

  def render = "*" #> {
	val breadcrumbs: List[Loc[_]] =
		for {
			currentLoc <- S.location.toList
			loc <- currentLoc.breadCrumbs
		} yield loc
	"li *" #> breadcrumbs.map {
		loc => ".link *" #> loc.title &
			".link [href]" #> loc.createDefaultLink.getOrElse(Text("#"))
    }
  }
}