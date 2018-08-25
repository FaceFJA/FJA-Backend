package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import model.Posts
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
  def getAlbumList(id: String) = {
    db.run(scraps.filter(r => r.uid==id).distinctOn(_.album_name).map(_.album_name).result)
  }
  def addScrap(scrap: Scrap) = db.run(DBIO.seq(scraps+=scrap))
  def getPostInAlbum(id: String, album: String) = {
    db.run(scraps.filter(r => r.uid==id && r.album_name==album).map(_.post_id).result)
  }
  def deleteScrap(id: String, album: String, post_id: Int) = {
    db.run(scraps.filter(r => r.uid==id && r.album_name==album && r.post_id==post_id).delete)
  }
}
