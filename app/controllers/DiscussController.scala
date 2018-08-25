package controllers

import java.util.Base64

import javax.inject._
import model.PostAccess
import models.{CommentAccess, ImageAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.ActionWithAuth

import scala.collection.mutable.ListBuffer


@Singleton
class DiscussController @Inject()(cc: ControllerComponents,
                                  actionWithAuth: ActionWithAuth,
                                  postDB: PostAccess,
                                  imageDB: ImageAccess,
                                  commentDB: CommentAccess)
                                 (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /*
   * GET
   *
   * 클라이언트로부터 받는 것:
   * 없음
   *
   * 리스트 반환 규격: JsonArray
   * 예시:
   * [
   *    {
   *      "post_id": 10,
   *      "title": "Lorem ipsum dolor sit",
   *      "text": "aaaaddddssss",
   *      "image": "base64",
   *      "category": "some category",
   *      "star": 4.7
   *    },
   * ]
   * 성공 시
   * 200 성공
   */
  def discussList(needCategory: String) = actionWithAuth.async {
    postDB.selectDiscuss.map { i =>
      var list = new ListBuffer[Seq[(String, JsValue)]]()
      i.foreach { column =>
        val postId = column.post_id
        val title = column.title
        val text = column.text
        val category = column.category
        val star = column.star
        var image = ""
        imageDB.findImagesByPostId(postId).map { j =>
          j.size match {
            case 0 => image = null
            case _ => {
              val blobLength = Int(j(0).data.length)
              image = Base64.getEncoder.encodeToString(j(0).data.getBytes(1, blobLength))
            }
          }
        }
        if (category.equals(needCategory)) {
          list += Seq(
            "post_id" -> JsNumber(postId),
            "title" -> JsString(title),
            "text" -> JsString(text),
            "image" -> JsString(image),
            "category" -> JsString(category),
            "star" -> JsNumber(star)
          )
        }
      }
      Ok(Json.toJson(list))
    }
  }

  /*
   * GET
   *
   * 클라이언트로부터 받는 것:
   * postId
   *
   * 글 반환 양식:
   * {
   *    "post_id": 10,
   *    "title": "Lorem ipsum",
   *    "text": "wefwefwrgsd",
   *    "images": [ "base", "64" ],
   *    "category": "wfwef",
   *    "star": 3.7,
   *    "comments": 375
   * }
   *
   * 성공 시
   * 200 성공
   */
  def getDiscussPost(postId: Int) = actionWithAuth.async {
    postDB.findPostById(postId).map { i =>
      var result = Json.obj("" -> "")
      i.foreach { post =>
        val title = post.title
        val text = post.text
        val category = post.category
        val star = post.star
        var images = Seq[String]()
        imageDB.findImagesByPostId(postId).map { j =>
          images = j.map { image =>
            Base64.getEncoder.encodeToString(image.data.getBytes(1, Int(image.data.length)))
          }
        }
        var comments = 0
        commentDB.findCommentsByPostId(postId).map { k =>
          comments = k.length
        }
        result = Json.obj(
          "post_id" -> postId,
          "title" -> title,
          "text" -> text,
          "images" -> images,
          "category" -> category,
          "star" -> star,
          "comments" -> comments
        )
      }
      Ok(result)
    }
  }

  /*
   * PUT
   *
   * 클라이언트로부터 받는 것:
   * postId
   *
   * {
   *    "star": 3.7
   * }
   *
   * 성공 시
   * 200 성공
   */
  def markStarToPost(postId: Int) = actionWithAuth(parse.json) { request =>
    val star = (request.body \ "star").as[BigDecimal]
    postDB.markStar(postId, star)
    Ok("별점 부여됨")
  }

  /*
   * GET
   *
   * 클라이언트로부터 받는 것:
   * postId
   *
   * 댓글 목록 양식
   * [
   *    {
   *      "comment_id": 13,
   *      "text": "lorem ipsum",
   *      "like": 218,
   *      "unlike": 190,
   *      "image": "base64"
   *    },
   *    ...
   * ]
   *
   * 성공 시
   * 200 성공
   *
   */
  def getCommentList(postId: Int) = actionWithAuth.async {
    commentDB.findCommentsByPostId(postId).map { i =>
      var comments = new ListBuffer[JsObject]()
      i.foreach { column =>
        val commentId = column.comment_id
        val text = column.text
        val like = column.like
        val unlike = column.unlike
        var image = ""
        imageDB.findImageByCommentId(commentId).map { i =>
          i.foreach { column =>
            image = Base64.getEncoder.encodeToString(column.data.getBytes(1, Int(column.data.length)))
          }
        }
        val json = Json.obj(
          "comment_id" -> commentId,
          "text" -> text,
          "like" -> like,
          "unlike" -> unlike,
          "image" -> image
        )
        comments += json
      }
      Ok(Json.toJson(comments))
    }
  }

  /*
   * PUT
   *
   * 클라이언트로부터 받는 것:
   * commentId
   *
   * {
   *    "is_like": true // true면 좋아요, false면 싫어요
   * }
   *
   * 성공 시:
   * 200 OK
   */
  def putLikeOrUnlike(commentId: Int) = actionWithAuth(parse.json).async { request =>
    val isLike = (request.body \ "is_like").as[Boolean]
    commentDB.findCommentById(commentId).map { i =>
      i.foreach { column =>
        val like = column.like
        val unlike = column.unlike
        isLike match {
          case true => commentDB.increaseLike(commentId, like + 1)
          case false => commentDB.increaseUnlike(commentId, unlike + 1)
        }
      }
      Ok("좋아요/싫어요 부여됨")
    }
  }


}
