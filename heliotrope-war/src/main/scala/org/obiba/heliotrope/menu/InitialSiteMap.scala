package org.obiba.heliotrope.menu

import org.obiba.heliotrope.snippet.StudyMenuItems
import org.obiba.heliotrope.snippet.SubjectMenuItems
import net.liftweb.http.LiftRules
import net.liftweb.sitemap.Menu
import net.liftweb.sitemap.Loc._
import org.obiba.heliotrope.domain.User
import net.liftweb.sitemap.SiteMap
import net.liftweb.http.S

object InitialSiteMap {
  
  def setupSiteMap {
    def loggedIn_? = { User.loggedInUser().isDefined }
    def ifLoggedIn = If(() => loggedIn_?, "You must be logged in")
        
    // Build SiteMap
    val entries = List(
      Menu("home", S ? "Home") / "index",
      
      Menu("submissions", S ? "Submissions") / "submission" >> ifLoggedIn,
      Menu("samples", S ? "Samples") / "sample" >> ifLoggedIn,
      Menu("status", S ? "Status") / "status" >> ifLoggedIn,
      Menu("logout", S ? "Log out") / "logout" >> ifLoggedIn
    ) ::: StudyMenuItems.menus ::: SubjectMenuItems.menus
    
    LiftRules.setSiteMap(SiteMap(entries:_*))
  }
}