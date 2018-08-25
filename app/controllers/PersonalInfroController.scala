package controllers

import javax.inject.Inject
import models.{User, UserAccess}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, Request}
import services.ActionWithAuth

import scala.collection.mutable.ListBuffer

class PersonalInfroController @Inject()(cc: ControllerComponents, db: UserAccess, auth: ActionWithAuth)(implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /*
   * GET
   *
   * 클라이언트로부터 받는 데이터
   * id
   *
   * 성공 시
   * 200
   */
  def getPersonalInfo = auth(parse.json) { request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val result = takePersonalInfo(id)
    Ok(Json.toJson(result))
  }

  /*
   * PUT
   *
   * 클라이언트로부터 받는 데이터
   * id, pw
   *
   * 성공 시
   * 200
   */

  def updatePersonalInfo = auth(parse.json) { request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val pw = (request.body \ "pw").as[String]

    val result = Json.toJson(takePersonalInfo(id))
    val name = (result \ "name").as[String]
    val gender = (result \ "gender").as[Int]
    val old = (result \ "old").as[Int]
    val email = (result \ "email").as[String]
    val updateUser = User(id, pw, name, gender, old, email)
    db.updatePassword(id, updateUser)
    Ok("")
  }

  /*
   * DELETE
   *
   * 클라이언트로부터 받는 데이터
   * id
   *
   * 성공 시
   * 200
   */

  def leaveUser = auth(parse.json) {request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    db.leaveUser(id)
    Ok("회원탈퇴 완료")
  }

  /*
   * GET
   *
   * 클라이언트로부터 받는 데이터
   * id
   *
   * 성공 시
   * 200
   *
   */

  def takePersonalInfo(id: String) = {
    val result = new ListBuffer[JsValue]()
    db.checkId(id).map(i =>
      i.foreach(data => result += Json.toJson(data))
    )
    result
  }
}
