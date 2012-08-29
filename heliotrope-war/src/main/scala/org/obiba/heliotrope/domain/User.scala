package org.obiba.heliotrope.domain {

import net.liftweb.common.Box
import net.liftweb.common.Empty
import net.liftweb.common.Full
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.field.ObjectIdPk
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.record.field.StringField
import net.liftweb.mongodb.record.field.DateField
import java.util.Date
import net.liftweb.record.field.OptionalPasswordField
import net.liftweb.common.Loggable
import net.liftweb.record.field.OptionalStringField
import net.liftweb.record.field.OptionalDateTimeField
import com.foursquare.rogue.Rogue._
import com.foursquare.rogue.AbstractFindAndModifyQuery
import org.joda.time.DateTime

abstract case class UserOption
case class FamilyNameUserOption(string: String) extends UserOption
case class GivenNameUserOption(string: String) extends UserOption
case class EmailUserOption(string: String) extends UserOption

class User private() extends MongoRecord[User] with ObjectIdPk[User] {
  
  def meta = User
  
  object userName extends StringField(this, 20)
  object givenName extends OptionalStringField(this, 32)
  object familyName extends OptionalStringField(this, 32)
  object email extends OptionalStringField(this, 64)
  object lastLoggedIn extends OptionalDateTimeField(this)
  object password extends OptionalPasswordField(this) {
    val minPasswordLength = 8
  }
    
  def niceName() : String = {
    return userName.is
  }

  def setRole(role: String) : Unit = {
    
  }
  
  def wholeName() : String = {
    return userName.is
  }
  
  def validPassword_?(pwd: String): Boolean = {
    return password.match_?(pwd)
  }
}

object User extends User with MongoMetaRecord[User] with Loggable {
  
  override def collectionName = "user"
    
  private object currentUser extends SessionVar[Box[User]](Empty)
  
  /**
   * Finds a user by name. 
   */
  def findUser(userName: String): Box[User] = {

    logger.debug("Called findUser: userName: " + userName)
    
    val query = User where (_.userName eqs userName)
    val result = query.get()
    result.foreach { user =>
      logger.debug("Found user: " + user)
    }
    result.flatMap(Full(_))
  }
  
  /**
   * Find or create a user, and then update them by throwing data at Rogue until
   * some of it finds its way into the object. 
   */
  def findOrCreateUser(userName: String, options: UserOption*): Box[User] = {
    
    logger.debug("Called findOrCreateUser: userName: " + userName)
    
    var user = findUser(userName).or {
      logger.debug("Creating new user: userName: " + userName)
      Full(User.createRecord.userName(userName).save)
    }
    
    logger.debug("About to update: " + user)
    
    var query: AbstractFindAndModifyQuery[User, User] = User where (_.userName eqs userName)
    
    options.map { option: UserOption => 
      logger.debug("Adding update: " + option)
      option match {
        case FamilyNameUserOption(string) => {
          query = query findAndModify (_.familyName setTo string)
        }
        case GivenNameUserOption(string) => {
          query = query findAndModify (_.givenName setTo string)
        }
        case EmailUserOption(string) => {
          query = query findAndModify (_.email setTo string)
        }
      }
    }
    query = query findAndModify (_.lastLoggedIn setTo new DateTime())
    
    val result = query.updateOne()
    result.flatMap(Full(_))
  }
  
  def logUserIn(user: User) : Unit = {
    currentUser.set(Full(user))
  }
  
  def logUserOut() : Unit = {
    currentUser.set(Empty)
  }
  
  def loggedInUser() : Box[User] = {
    currentUser.get
  }
}

}