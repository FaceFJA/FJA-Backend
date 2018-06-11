package models

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
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

case class Expert(uid: String, category: JsValue)

class Experts(tag: Tag) extends Table[Expert](tag, "expert") {
  val users = TableQuery[Users]

  def uid = column[String]("uid")
  def category = column[JsValue]("category")

  def uid_FK = foreignKey("uid_fk", uid, users)(_.uid)

  def * = (uid, category) <> ((Expert.apply _).tupled, Expert.unapply)
}

@Singleton
class ExpertAccess @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  val experts = TableQuery[Experts]

  def selectAll = db.run(experts.result)
}
