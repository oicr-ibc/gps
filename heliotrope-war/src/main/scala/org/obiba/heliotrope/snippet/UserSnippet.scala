package org.obiba.heliotrope.snippet

import scala.xml.NodeSeq
import net.liftweb.http.DispatchSnippet
import org.obiba.heliotrope.auth.UserAuth
import net.liftweb.http.S
import net.liftweb.common.Box
import scala.xml.MetaData
import scala.xml.Text
import org.obiba.heliotrope.domain.User
import net.liftweb.common.Loggable

class UserSnippet extends DispatchSnippet with Loggable {

  def dispatch: DispatchIt = 
  Map("name" ->      userName _,
      "loginForm" -> loginForm _)

  def calcUser: Box[User] = User.loggedInUser()

  def loginForm(in: NodeSeq): NodeSeq =
    if (User.loggedInUser().isDefined) 
      NodeSeq.Empty
    else 
      UserAuth.loginPresentation.map(l => <div>{l}</div>)

  def userName(in: NodeSeq): NodeSeq = {
    val user: Box[String] = User.loggedInUser().map(_.wholeName)
    logger.debug("Currently logged in as: " + user)
    if (user.isEmpty) {
      Text(S.?("Not logged in"))
    } else {
      Text(S.?("Logged in as: %s", user openOr ""))
    }
  }
}
