package models

import javax.inject.{Inject, Singleton}
import model.Posts
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._

case class Comment(uid: String,
                   comment_id: Int,
                   post_id: Int,
                   like: Int,
                   unlike: Int,
                   text: String)

class Comments(tag: Tag) extends Table[Comment] (tag, "Comment"){
  def uid = column[String]("uid")
  def comment_id = column[Int]("comment_id", O.PrimaryKey, O.AutoInc)
  def post_id = column[Int]("post_id")
  def like = column[Int]("like")
  def unlike = column[Int]("unlike")
  def text = column[String]("text")
  def * = (uid, comment_id, post_id, like, unlike, text) <> ((Comment.apply _ ).tupled, Comment.unapply)

  val users = TableQuery[Users]
  val posts = TableQuery[Posts]

  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)
  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
}

@Singleton
class CommentAccess @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val comments = TableQuery[Comments]

  def selectAll = db.run(comments.result)
  def findCommentsByPostId(postId: Int) = {
    db.run(comments.filter(_.post_id == postId).result)
  }
  def findCommentById(commentId: Int) = {
    db.run(comments.filter(_.comment_id == commentId).result)
  }
  def increaseLike(commentId: Int, value: Int) = {
    val column = for { l <- comments if l.comment_id == commentId } yield l.like
    db.run(column.update(value))
  }
  def increaseUnlike(commentId: Int, value: Int) = {
    val column = for { l <- comments if l.comment_id == commentId } yield l.unlike
    db.run(column.update(value))
  }
}