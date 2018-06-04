package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._


case class User(uid: String,
                pw: String,
                name: String,
                gender: Int,
                old: Int,
                email: String)

class Users(tag: Tag) extends Table[User] (tag, "User") {
  def uid = column[String]("uid", O.PrimaryKey)
  def pw = column[String]("pw")
  def name = column[String]("name")
  def gender = column[Int]("gender")
  def old = column[Int]("old")
  def email = column[String]("email")
  def * = (uid, pw, name, gender, old, email) <> ((User.apply _ ).tupled, User.unapply)
}

@Singleton
class UserAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val users = TableQuery[Users]
  def selectAll = db.run(users.result)
}