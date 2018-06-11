package models

import model.Posts
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._

case class Comment(uid: String,
                   comment_id: Int,
                   post_id: Int,
                   like: Int,
                   unlike: Int)

class  Comments(tag: Tag) extends Table[Comment] (tag, "Comment"){
  def uid = column[String]("uid")
  def comment_id = column[Int]("comment_id", O.PrimaryKey)
  def post_id = column[Int]("post_id")
  def like = column[Int]("like")
  def unlike = column[Int]("unlike")
  def * = (uid, comment_id, post_id, like, unlike) <> ((Comment.apply _ ).tupled, Comment.unapply)

  val users = TableQuery[Users]
  val posts = TableQuery[Posts]

  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)
  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
}