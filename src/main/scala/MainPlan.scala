package org.codefirst.partakelist
import javax.servlet.http._
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.{File => JFile}

import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import dispatch.twitter._

case class TwitterList(consumer : Consumer, token: Token) extends Request(Twitter.host / "1" / "lists"){
  def create(name : String) =
    this / "create.json" << Map("name" -> name)  <@ (consumer, token)

  def show(name : String, screen_name : String) =
    this / "show.json" <<? Map("slug" -> name, "owner_screen_name" -> screen_name) <@ (consumer, token)

  def createAll(name : String, screen_name : String, members : Seq[String]) =
    this / "members" / "create_all.json" << Map("slug" -> name, "owner_screen_name" -> screen_name, "screen_name" -> members.mkString(",")) <@ (consumer, token)

  def create(name : String, screen_name : String, member : String) =
    this / "members" / "create.json" << Map("slug" -> name, "owner_screen_name" -> screen_name, "screen_name" -> member) <@ (consumer, token)
}

class MainPlan extends unfiltered.filter.Plan {
  implicit lazy val engine = {
    val path = config.getServletContext.getRealPath("/")
    val engine = new TemplateEngine(List(new JFile(path + "/WEB-INF/templates")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, path + "/WEB-INF/templates/layouts/default.scaml")
    engine
  }

  private val CONSUMER = Consumer("6bBnS4NHAfDp2g8EYYekfA", "q8JvPVxKD1mjddya1a9lZQ28lIsIL7NwbXEbxrnd8")
  private val http = new Http

  def twitter[A](request : HttpRequest[A], url : String) = {
    request match {
      case RemoteAddr(host) =>
        val scheme = if(request.isSecure) "https" else "http"
        val redirectUrl : String= scheme + "://" + request.headers("host").next + "/callback?url=" + url
        val reqToken = http(Auth.request_token(CONSUMER, redirectUrl))
        Redirect(Auth.authorize_url(reqToken).to_uri.toString)
      case _ =>
        BadRequest
    }
  }

  def ensureList(token : Token, screenName : String, name : String) {
    try {
      http(TwitterList(CONSUMER, token).show(name, screenName) >|)
    } catch { case StatusCode(404,_) =>
      http(TwitterList(CONSUMER, token).create(name) >|)
    }
  }

  def callback( params : Map[String, Seq[String]]) = {
    val url = params("url").first
    val (title, users) = Partake.load(url)
    val ret = for {
      oauthToken    <- params("oauth_token").firstOption
      oauthVerifier <- params("oauth_verifier").firstOption
      val oauthTokenObj = new Token(oauthToken, oauthVerifier)
      val (accessToken: Token, userId: String, screenName: String) =
        http(Auth.access_token(CONSUMER, oauthTokenObj, oauthVerifier))
      val _ = ensureList(accessToken, screenName, title)
      val _ = users.foreach { user =>
        http(TwitterList(CONSUMER, accessToken).create(title, screenName, user) >|) }
    } yield ResponseString("ok")

    ret getOrElse {
      InternalServerError
    }
  }

  def intent = { case request =>
    request match {
      case Path(Seg("static" :: _)) | Path("/favicon.ico") =>
        Pass
      case Path("/") =>
        Ok ~> HtmlContent ~> Scalate(request, "index.scaml", "title" -> "AttendList")
      case Path(Seg("callback" :: _)) & Params(params) =>
        callback(params)
      case Path("/create") & Params(params) =>
        twitter(request, params("url").firstOption.getOrElse(""))
    }}}

