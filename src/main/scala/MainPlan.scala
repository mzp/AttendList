import javax.servlet.http._
import unfiltered.request._
import unfiltered.response._
import unfiltered.scalate._
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.servlet.ServletTemplateEngine
import java.io.{File => JFile}

class MainPlan extends unfiltered.filter.Plan {
  //implicit val engine =
  def engine = {
    val path = config.getServletContext.getRealPath("/")
    println(path + "/WEB-INF/tempalates")
    new TemplateEngine(List(new JFile(path + "/WEB-INF/templates")))
  }

  def intent = {
    case req@Path(_) =>
      println( )
      Ok ~> Scalate(req, "index.scaml")(engine)
  }
}
