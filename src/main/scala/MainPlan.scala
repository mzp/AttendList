package org.codefirst.partakelist
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.{File => JFile}

import dispatch._
import dispatch.oauth._
import dispatch.twitter._

object AttendList {
  private val consumer =
    Consumer("6bBnS4NHAfDp2g8EYYekfA", "q8JvPVxKD1mjddya1a9lZQ28lIsIL7NwbXEbxrnd8")

  private val http =
    new Http

  private def getURL[A](request : HttpRequest[A]) : Option[String] =
    request match {
      case RemoteAddr(host) =>
        val scheme = if(request.isSecure) "https" else "http"
        Some(scheme + "://" + request.headers("host").next)
      case _ =>
        None
    }

  def auth[A](request : HttpRequest[A], url : String) = {
    val ret = for {
      base <- getURL(request)
      val redirectUrl = base + "/callback?url=" + url
      val reqToken = http(Auth.request_token(consumer, redirectUrl))
    } yield Redirect(Auth.authorize_url(reqToken).to_uri.toString)

    ret getOrElse {
      InternalServerError
    }
  }

  /** リストを作る(失敗しても泣かない) */
  private def ensureList(token : Token, screenName : String, name : String) {
    val list =
      TwitterList(consumer, token)
    try {
      http(list.show(name, screenName) >|)
    } catch { case StatusCode(404,_) =>
      http(list.create(name) >|)
    }
  }

  private def getAccessToken(params : Map[String, Seq[String]]) =
    for {
      oauthToken    <- params("oauth_token").firstOption
      oauthVerifier <- params("oauth_verifier").firstOption
    } yield http(Auth.access_token(consumer,
                                   new Token(oauthToken, oauthVerifier),
                                   oauthVerifier))

  def createList(params : Map[String, Seq[String]]) = {
    val ret = for {
      // partakeの情報を取得
      url <- params("url").firstOption
      val event = Partake(url)
      // twitter認証を取得
      (token,_,screenName) <- getAccessToken(params)
      // リストの作成
      val () = ensureList(token, screenName, event.title)
      // ユーザの追加
      val () = event.users.foreach { user =>
        http(TwitterMember(event.title, screenName, consumer, token).create(user) >|) }
    } yield ResponseString("ok")

    ret getOrElse {
      InternalServerError
    }
  }
}

class MainPlan extends unfiltered.filter.Plan {
  implicit lazy val engine = {
    // Scalateのテンプレートを/WEB-INF/tempalte/*からロードするようにする。
    val path = config.getServletContext.getRealPath("/")
    val engine = new TemplateEngine(List(new JFile(path + "/WEB-INF/templates")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, path + "/WEB-INF/templates/layouts/default.scaml")
    engine
  }

  def intent = { case request =>
    request match {
      case Path(Seg("static" :: _)) | Path("/favicon.ico") =>
        Pass
      case Path("/") =>
        Ok ~> HtmlContent ~> Scalate(request, "index.scaml", "title" -> "AttendList")
      case Path(Seg("callback" :: _)) & Params(params) =>
        AttendList.createList(params)
      case Path("/create") & Params(params) =>
        params("url") firstOption match {
          case Some(url) =>
            AttendList.auth(request, url)
          case None =>
            Redirect("/")
        }
    } }
}
