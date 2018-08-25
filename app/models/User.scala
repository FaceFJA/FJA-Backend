package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._
import play.api.libs.json._

case class User(uid: String,
                pw: String,
                name: String,
                gender: Int,
                old: Int,
                email: String)

object User {
  implicit val userWrites = new Writes[User] {
    def writes(obj: User) = Json.obj(
      "uid" -> obj.uid,
      "pw" -> obj.pw,
      "name" -> obj.name,
      "gender" -> obj.gender,
      "old" -> obj.old,
      "email" -> obj.email
    )
  }
}

class Users(tag: Tag) extends Table[User] (tag, "User") {
  def uid = column[String]("uid", O.PrimaryKey)
  def pw = column[String]("pw")
  def name = column[String]("name")
  def gender = column[Int]("gender")
  def old = column[Int]("old")
  def email = column[String]("email")
  def * = (uid, pw, name, gender, old, email) <> ((User.apply _).tupled, User.unapply)
}

@Singleton
class UserAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  val users = TableQuery[Users]

  def selectAll = db.run(users.result)

  def checkEmail(checkingEmail: String) = {
    db.run(users.filter(_.email == checkingEmail).result)
  }

  def checkId(checkingId: String) = {
    db.run(users.filter(_.uid == checkingId).result)
  }

  def joinNewUser(user: User) = {
    db.run(DBIO.seq(users += user))
  }

  def loginEval(id: String, pw: String) = {
    db.run(users.filter(r => r.uid == id && r.pw == pw).exists.result)
  }

  def findId(name: String, email: String) = {
    db.run(users.filter(r => r.name == name && r.email == email).map(_.uid).result)
  }

  def findPassword(id: String, email: String) = {
    db.run(users.filter(r => r.uid == id && r.email == email).map(_.pw).result)
  }

  def updatePassword(id: String, row: Users#TableElementType) = {
    db.run(users.filter(_.uid == id).update(row))
  }
}