import javax.servlet.http._
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.layout.DefaultLayoutStrategy
import java.io.{File => JFile}

class MainPlan extends unfiltered.filter.Plan {
  implicit lazy val engine = {
    val path = config.getServletContext.getRealPath("/")
    val engine = new TemplateEngine(List(new JFile(path + "/WEB-INF/templates")))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, path + "/WEB-INF/templates/layouts/default.scaml")
    engine
  }

  def intent = {
    case req@Path(_) =>
      println( )
      Ok ~> Scalate(req, "index.scaml")(engine)
  }
}
