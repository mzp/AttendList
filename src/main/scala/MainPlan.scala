import javax.servlet.http._
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.{File => JFile}

import dispatch._
import dispatch.oauth._
import dispatch.twitter._

class MainPlan extends unfiltered.filter.Plan {
  implicit lazy val engine = {
    val path = config.getServletContext.getRealPath("/")
    val engine = new TemplateEngine(List(new JFile(path + "/WEB-INF/templates")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, path + "/WEB-INF/templates/layouts/default.scaml")
    engine
  }

  def twitter = {
    val CONSUMER = Consumer("6bBnS4NHAfDp2g8EYYekfA", "q8JvPVxKD1mjddya1a9lZQ28lIsIL7NwbXEbxrnd8")
    val http = new Http
    val redirectUrl = "http://example.com/service/callback"
    val reqToken = http(Auth.request_token(CONSUMER, redirectUrl))
    val url = Auth.authorize_url(reqToken).to_uri
    Redirect(url.toString())
  }

  def intent = { case request =>
    request match {
      case Path(Seg("static" :: _)) | Path("/favicon.ico") =>
        Pass
      case Path("/") =>
        Ok ~> Scalate(request, "index.scaml", "title" -> "PartakeList")
      case Path("/create") =>
        twitter
    }}}

