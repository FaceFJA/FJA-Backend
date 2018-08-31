package controllers

import javax.inject.Inject
import models.{Scrap, ScrapAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import services.ActionWithAuth

import scala.collection.mutable.ListBuffer

class ScrapController @Inject()(cc: ControllerComponents, db: ScrapAccess, auth: ActionWithAuth)(implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  def getAlbumList = auth { request =>
    val id = request.session.get("id").getOrElse("")
    val result = ListBuffer[JsValue]()
    db.getAlbumList(id).map(i =>
      i.foreach(data => result += Json.toJson(data))
    )
    Ok(Json.toJson(result))
  }

  def addScrap = Action(parse.json) { request: Request[JsValue] =>
    val id = request.session.get("id").getOrElse("")
    val post_id = (request.body \ "post_id").as[Int]
    val album = (request.body \ "album_name").as[String]
    val newScrap = Scrap(id, post_id, album)
    db.addScrap(newScrap)
    Ok("스크랩 성공")
  }

  def getPostInScrap = Action(parse.json) { request: Request[JsValue] =>
    val id = request.session.get("id").getOrElse("")
    val album = (request.body \ "album_name").as[String]
    val result = ListBuffer[JsValue]()
    db.getPostInAlbum(id, album).map(i =>
      i.foreach(data => result += Json.toJson(data))
    )
    Ok(Json.toJson(result))
  }

  def deleteScrap = Action(parse.json) { request: Request[JsValue] =>
    val id = request.session.get("id").getOrElse("")
    val album = (request.body \ "album_name").as[String]
    val post_id = (request.body \ "post_id").as[Int]
    db.deleteScrap(id, album, post_id)
    Ok("삭제 성공")
  }

}
