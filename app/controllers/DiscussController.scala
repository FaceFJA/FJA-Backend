package controllers

import java.io.{File, PrintWriter}
import java.util.{Base64, Date}

import javax.inject._
import model.{Post, PostAccess}
import models.{Comment, CommentAccess, Image, ImageAccess}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc._
import services.ActionWithAuth

import scala.collection.mutable.ListBuffer
import scala.io.Source


@Singleton
class DiscussController @Inject()(cc: ControllerComponents,
                                 auth: ActionWithAuth,
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
  def discussList(needCategory: String) = auth.async {
    postDB.selectDiscuss.map { i =>
      var list = new ListBuffer[Map[String, JsValue]]()
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
              val blobLength = j(0).data.length.asInstanceOf[Int]
              image = Source.fromFile(j(0).data).mkString
            }
          }
        }
        if (category.equals(needCategory)) {
          list += Map(
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
  def getDiscussPost(postId: Int) = auth.async {
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
            Source.fromFile(image.data).mkString
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
  def markStarToPost(postId: Int) = auth(parse.json).async { request =>
    val star = (request.body \ "star").as[Double]
    postDB.markStar(postId, star).map { result =>
      Ok("별점 부여됨")
    }
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
  def getCommentList(postId: Int) = auth.async {
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
            image = Source.fromFile(column.data).mkString
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
   * POST
   *
   * 클라이언트로부터 받는 것:
   * postId
   * {
   *    "text": <text>
   * }
   */
  def uploadComment(postId: Int) = auth(parse.json).async { request =>
    val uid = request.session.get("id").getOrElse("")
    val text = (request.body \ "text").as[String]
    commentDB.addComment(Comment(uid, 0, postId, 0, 0, text)).map { result =>
      Ok("댓글 작성됨")
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
  def putLikeOrUnlike(commentId: Int) = auth(parse.json).async { request =>
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

  /*
   * POST
   *
   * 클라이언트로부터 받는 것:
   * {
   *    "title": "edlfhaekh",
   *    "text": "kferiuhougeir",
   *    "category": "some category",
   *    "images": [
   *      <base64 images...>
   *    ]
   * }
   *
   * 성공 시
   * 201 작성됨
   */
  def uploadPost = auth(parse.json).async { request =>
    val title = (request.body \ "title").as[String]
    val text = (request.body \ "text").as[String]
    val uploadDate = new Date().getTime
    val validDate = uploadDate
    val star = 0.0
    val category = (request.body \ "category").as[String]
    val images = (request.body \ "images").as[Array[String]]
    val isAssess = 0
    val userId = request.session.get("id").getOrElse("")
    val post = Post(0, title, text, uploadDate.asInstanceOf[Long], validDate.asInstanceOf[Long], star, category, isAssess, userId)
    postDB.post(post).map { result =>
      postDB.size.map { size =>
        images.foreach { image =>
          val path = new File(".").getCanonicalPath + "/public/" + image.hashCode.toString
          val writer = new PrintWriter(new File(path))
          writer.write(image)
          imageDB.insertImage(Image(Some(size), None, path, 0)).map { result =>

          }
        }
      }
      Created("글 작성됨")
    }
  }
}
