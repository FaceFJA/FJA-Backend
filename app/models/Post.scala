package model

import java.sql.Date

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import javax.inject.{Inject, Singleton}
import slick.jdbc.MySQLProfile.api._
import play.api.libs.json._

import JsonMapper.mapper

object JsonMapper {
  implicit val mapper = MappedColumnType.base[JsValue, String](
    { json => Json.stringify(json) },
    { string => Json.parse(string) }
  )
}
case class Post(post_id: Int,
                title: String,
                text: String,
                upload_date: Date,
                valid_date: Date,
                star: BigDecimal,
                category: String)

class Posts(tag: Tag) extends Table[Post] (tag, "Post"){
  def post_id = column[Int]("post_id", O.PrimaryKey)
  def title = column[String]("title")
  def text = column[String]("text")
  def upload_date = column[Date]("upload_date")
  def valid_date = column[Date]("valid_date")
  def star = column[BigDecimal]("star")
  def category = column[String]("category")
  def * = (post_id, title, text, upload_date, valid_date, star, category) <> ((Post.apply _ ).tupled, Post.unapply)
}

@Singleton
class PostAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val posts = TableQuery[Posts]
  def selectAll = db.run(posts.result)
}
