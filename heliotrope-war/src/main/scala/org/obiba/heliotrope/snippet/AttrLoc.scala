package org.obiba.heliotrope.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.S

object AttrLoc {
  
  def render = 
    ("* [" + S.attr("attr").openOr("value") + "]") #> (S ? S.attr("value").openOr("value"))

}
