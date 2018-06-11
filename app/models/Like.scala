package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._

case class Like(comment_id: Int, uid: String, islike: Int)

class Likes(tag: Tag) extends Table[Like](tag, "like") {
  val comments = TableQuery[Comments]
  val users = TableQuery[Users]

  def comment_id = column[Int]("comment_id")
  def uid = column[String]("uid")
  def islike = column[Int]("islike")

  def comment_id_FK = foreignKey("comment_id_fk", comment_id, comments)(_.comment_id)
  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)

  def * = (comment_id, uid, islike) <> ((Like.apply _).tupled, Like.unapply)
}

@Singleton
class LikeAccess @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val likes = TableQuery[Likes]

  def selectAll = db.run(likes.result)
}
