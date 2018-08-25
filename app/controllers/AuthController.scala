package controllers

import javax.inject._
import play.api.mvc._
import models.UserAccess
import models.User
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


@Singleton
class AuthController @Inject()(cc: ControllerComponents, db: UserAccess) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * id, pw, 이름, 성별, 이메일
   *
   * 회원가입 성공 시:
   * 201 작성됨
   *
   * 회원가입 실패 시:
   * 409 충돌: 아이디가 이미 존재할 때
   * 406 허용되지 않음 : 데이터가 잘못되었을 때 (ex: 데이터 변조 전송)
   *
   */
  def join = Action(parse.json).async { request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val query = db.checkId(id)
    query.map { duplicated =>
      duplicated.length match {
        case 0 => doJoin(request.body)
        case _ => Conflict("이미 존재하는 유저")
      }
    }
  }


  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * (유효성 검사를 위한) 이메일
   *
   * 유효한 이메일의 기준: 이메일 형식을 준수해야 한다.
   *
   * 이메일이 유효할 때:
   * 202 허용됨
   *
   * 이메일이 유효하지 않을 때:
   * 406 허용되지 않음
   */
  def checkEmail = Action(parse.json).async { request: Request[JsValue] =>
    val email = (request.body \ "email").as[String]
    val query = db.checkEmail(email)
    query.map { duplicated =>
      duplicated.length match {
        case 0 => Accepted("이메일 사용 가능")
        case _ => NotAcceptable("이메일 사용 불가")
      }
    }
  }

  def doJoin(jsonBody: JsValue) = {
    val uid = (jsonBody \ "id").as[String]
    val pw = (jsonBody \ "pw").as[String]
    val name = (jsonBody \ "name").as[String]
    val gender = (jsonBody \ "gender").as[Int]
    val old = (jsonBody \ "old").as[Int]
    val email = (jsonBody \ "email").as[String]
    val newUser = User(uid, pw, name, gender, old, email)
    db.joinNewUser(newUser)
    Ok("회원가입 성공")
  }

  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * id, pw
   *
   * 존재하는 계정일 때:
   * 200 성공
   *
   * 존재하지 않는 계정일 :
   * 406 허용되지 않음
   */
  def login = Action(parse.json).async { request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val pw = (request.body \ "pw").as[String]
    db.loginEval(id, pw).map { result =>
      result match {
        case true => Ok("로그인 성공").withSession("id" -> id)
        case false => NotAcceptable("로그인 실패")
      }
    }
  }

  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * name, email
   *
   * 존재하는 계정일 때:
   * 200 성공
   *
   * 존재하지 않는 계정일 때:
   * 404 찾을 수 없음
   */
  def findId = Action(parse.json).async { request: Request[JsValue] =>
    val name = (request.body \ "name").as[String]
    val email = (request.body \ "email").as[String]
    db.findId(name, email).map { result =>
      result.length match {
        case 0 => NotFound("찾을 수 없음")
        case _ => Ok(Json.toJson(result))
      }
    }
  }

  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * id, email
   *
   * 존재하는 계정일 때:
   * 200 성공
   *
   * 존재하지 않는 계정일 때:
   * 404 찾을 수 없음
   */
  def findPassword = Action(parse.json).async { request: Request[JsValue] =>
    val id = (request.body \ "id").as[String]
    val email = (request.body \ "email").as[String]
    db.findPassword(id, email).map { result =>
      result.length match {
        case 0 => NotFound("찾을 수 없음")
        case _ => Ok(Json.toJson(result))
      }
    }
  }

}
