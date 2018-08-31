package models

import play.api.libs.json._
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._
import model.Posts

/*
object JsonMapper {
  implicit val mapper = MappedColumnType.base[JsValue, String](
    { json => Json.stringify(json) },
    { string => Json.parse(string) }
  )
}
*/

case class Fashionista(category: String,
                       post_id: Int,
                       uid: String,
                       kind: Int,
                       rank: BigDecimal,
                       votes: Int)

class Fashionistas(tag: Tag) extends Table[Fashionista] (tag, "Fashionista"){
  def category = column[String]("category")
  def post_id = column[Int]("post_id")
  def uid = column[String]("uid")
  def kind = column[Int]("kind")
  def rank = column[BigDecimal]("rank")
  def votes = column[Int]("votes")
  def * = (category, post_id, uid, kind, rank, votes) <> ((Fashionista.apply _ ).tupled, Fashionista.unapply)

  val posts = TableQuery[Posts]
  val users = TableQuery[Users]

  def post_id_FK = foreignKey("post_id_fk", post_id, posts)(_.post_id)
  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)
}