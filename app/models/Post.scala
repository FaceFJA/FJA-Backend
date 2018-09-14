package model

import java.sql.{Date, Timestamp}
import java.time.LocalDate

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._
import play.api.libs.json._
import JsonMapper.mapper
import models.Users

object JsonMapper {
  implicit val mapper = MappedColumnType.base[JsValue, String](
    { json => Json.stringify(json) },
    { string => Json.parse(string) }
  )
}

case class Post(post_id: Int,
                title: String,
                text: String,
                upload_date: Long,
                valid_date: Long,
                star: Double,
                category: String,
                is_assess: Int,
                user_id: String)

class Posts(tag: Tag) extends Table[Post] (tag, "Post"){

  val users = TableQuery[Users]

  def post_id = column[Int]("post_id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def text = column[String]("text")
  def upload_date = column[Long]("upload_date")
  def valid_date = column[Long]("validate_date")
  def star = column[Double]("star")
  def category = column[String]("category")
  def is_assess = column[Int]("is_assess")
  def user_id = column[String]("user_id")

  def user_id_FK = foreignKey("user_id_fk", user_id, users)(_.uid)

  def * = (post_id, title, text, upload_date, valid_date, star, category, is_assess, user_id) <> ((Post.apply _ ).tupled, Post.unapply)
}

@Singleton
class PostAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val posts = TableQuery[Posts]
  def selectAll = db.run(posts.result)
  def selectAssess = {
    db.run(posts.filter(_.is_assess === 1).result)
  }
  def selectDiscuss = {
    db.run(posts.filter(_.is_assess === 1).result)
  }
  def findPostById(postId: Int) = {
    db.run(posts.filter(_.post_id === postId).result)
  }
  def markStar(postId: Int, star: Double) = {
    val column = for { l <- posts if l.post_id === postId } yield l.star
    db.run(column.update(star))
  }
  def getFashionistaByDate(date: Long) = {
    db.run(posts.filter(r => r.is_assess === 1 && r.upload_date < date).sortBy(_.star.desc).take(3).result)
  }
  def post(post: Post) = {
    db.run(posts += post)
  }
  def size = {
    db.run(posts.length.result)
  }
}
