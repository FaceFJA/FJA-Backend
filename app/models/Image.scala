package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._

import java.sql.Blob

case class Image(post_id: Int, comment_id: Int, data: Blob, order: Int)

class Images(tag: Tag) extends Table[Image](tag, "image") {
  val posts = TableQuery[Posts]
  val comments = TableQuery[Comments]

  def post_id = column[Int]("post_id")
  def comment_id = column[Int]("comment_id")
  def data = column[Blob]("data")
  def order = column[Int]("order")

  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
  def comment_id_FK = foreignKey("comment_id_fk", comment_id, comments)(_.comment_id)

  def * = (post_id, comment_id, data, order) <> ((Image.apply _).tupled, Image.unapply)
}

@Singleton
class ImageAccess @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val images = TableQuery[Images]

  def selectAll = db.run(images.result)
}