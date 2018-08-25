package controllers

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Base64, Date}

import javax.inject._
import model.PostAccess
import models.{CommentAccess, ImageAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.ActionWithAuth

import scala.collection.mutable.ListBuffer


@Singleton
class FashionistaController @Inject() (cc: ControllerComponents, actionWithAuth: ActionWithAuth, postDB: PostAccess) extends AbstractController(cc) {

  /*
   * GET
   *
   * 클라이언트로부터 받는 데이터:
   * date
   *
   * date 형식:
   * yyyy-MM-dd
   *
   * 성공 시
   * 200 성공
   */
  def getStarByDate(date: String) = actionWithAuth {
    val parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(date)
    val timestamp = new Timestamp(parsedDate.getTime)
    val timestampInt = timestamp.getTime
    var fashionistaList = new ListBuffer[JsObject]()
    postDB.getFashionistaByDate(Int(timestampInt)).map { i =>
      i.foreach { column =>
        val fashionista = Json.obj(
          "post_id" -> column.post_id,
          "title" -> column.title,
          "star" -> column.star
        )
        fashionistaList += fashionista
      }
    }
    Ok(Json.toJson(fashionistaList))
  }
}
