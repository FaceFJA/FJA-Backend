package services

import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.{ExecutionContext, Future}


class ActionWithAuth @Inject()(parser: BodyParsers.Default) (implicit ec: ExecutionContext)
  extends ActionBuilderImpl(parser) {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]) = {
    val userId = request.session.get("id")
    userId match {
      case None => {
        Future.successful(Unauthorized("로그인되지 않음"))
      }
      case Some(u) => {
        block(request)
      }
    }
  }

}
