package controllers

import javax.inject._
import play.api.mvc._
import models.{User, UserAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.collection.mutable.ListBuffer

@Singleton
class HomeController @Inject()(cc: ControllerComponents, db: UserAccess) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  def main = Action {
    Ok("Hello, world!")
  }
  /*
  def main = Action.async {
    val result = new ListBuffer[JsValue]()
    db.selectAll.map(i => {
      i.foreach { data => {
          result += Json.toJson(data)
        }
      }
      Ok(Json.toJson(result.toList))
    })
  }
  */

}
