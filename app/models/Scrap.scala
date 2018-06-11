package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._

case class Scrap(uid: String, post_id: Int, album_name: String)

class Scraps(tag: Tag) extends Table[Scrap](tag, "scrap") {
  val users = TableQuery[Users]
  val posts = TableQuery[Posts]

  def uid = column[String]("uid")
  def post_id = column[Int]("post_id")
  def album_name = column[String]("album_name")

  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)
  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)

  def * = (uid, post_id, album_name) <> ((Scrap.apply _).tupled, Scrap.unapply)
}

@Singleton
class ScrapAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val scraps = TableQuery[Scraps]

  def selectAll = db.run(scraps.result)
}
