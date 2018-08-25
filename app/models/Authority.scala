package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import model.Posts
import slick.jdbc.MySQLProfile.api._

case class Authority(post_id: Int, uid: String, stared: Int)

class Authorities(tag: Tag) extends Table[Authority](tag, "authority") {
  val posts = TableQuery[Posts]
  val users = TableQuery[Users]

  def post_id = column[Int]("post_id")
  def uid = column[String]("uid")
  def stared = column[Int]("stared")

  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)

  def * = (post_id, uid, stared) <> ((Authority.apply _).tupled, Authority.unapply)
}

@Singleton
class AuthorityAccess @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val authorities = TableQuery[Authorities]

  def selectAll = db.run(authorities.result)
}