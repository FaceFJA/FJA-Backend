package controllers

import javax.inject._
import play.api.mvc._
import models.UserAccess
import models.User
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext


@Singleton
class AssessController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  def assessList = Action.async { request =>

  }
}
