package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._
import java.sql.Blob

import model.Posts

case class Image(post_id: Option[Int], comment_id: Option[Int], data: String, order: Int)

class Images(tag: Tag) extends Table[Image](tag, "Image") {
  val posts = TableQuery[Posts]
  val comments = TableQuery[Comments]

  def post_id = column[Option[Int]]("post_id", O.Default(None))
  def comment_id = column[Option[Int]]("comment_id", O.Default(None))
  def data = column[String]("data")
  def order = column[Int]("order")

  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
  def comment_id_FK = foreignKey("comment_id_fk", comment_id, comments)(_.comment_id)

  def * = (post_id, comment_id, data, order) <> ((Image.apply _).tupled, Image.unapply)
}

@Singleton
class ImageAccess @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val images = TableQuery[Images]

  def selectAll = db.run(images.result)
  def findImagesByPostId(postId: Int) = {
    db.run(images.filter(_.post_id === postId).result)
  }
  def findImageByCommentId(commentId: Int) = {
    db.run(images.filter(_.comment_id === commentId).result)
  }
  def insertImage(image: Image) = {
    db.run(images ++= Seq(image))
  }
}