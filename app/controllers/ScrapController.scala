package controllers

import javax.inject.Inject
import models.{Scrap, ScrapAccess, UserAccess}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, Request}

import scala.collection.mutable.ListBuffer

class ScrapController @Inject()(cc: ControllerComponents, db: ScrapAccess)(implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  def getAlbumList = Action(parse.json) {request: Request[JsValue] => {
    val id = (request.body \ "id").as[String]
    val result = ListBuffer[JsValue]()
    db.getAlbumList(id).map(i =>
      i.foreach(data => result += Json.toJson(data))
    )
    Ok(Json.toJson(result))
  }}

  def addScrap = Action(parse.json) {request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val post_id = (request.body \ "post_id").as[Int]
    val album = (request.body \ "album_name").as[String]
    val newScrap = Scrap(id, post_id, album)
    db.addScrap(newScrap)
    OK("스크랩 성공")
  }

  def getPostInScrap = Action(parse.json) {request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val album = (request.body \ "album_name").as[String]
    val result = ListBuffer[JsValue]()
    db.getPostInAlbum(id, album).map(i =>
      i.foreach(data => result += Json.toJson(data))
    )
    Ok(Json.toJson(result))
  }

  def deleteScrap = Action(parse.json) {request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val album = (request.body \ "album_name").as[String]
    val post_id = (request.body \ "post_id").as[Int]
    db.deleteScrap(id, album, post_id)
    Ok("삭제 성공")
  }

}
