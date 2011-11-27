import javax.servlet.http._
import unfiltered.request._
import unfiltered.response._

class MainPlan extends unfiltered.filter.Plan {
  def intent = {
    case Path(_) => Ok ~> ResponseString("hello")
  }
}
