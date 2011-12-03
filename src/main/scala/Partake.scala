package org.codefirst.partakelist
import scala.xml.Node
import scala.io.Source
import java.net.URL
import java.io.StringReader
import scala.xml.parsing.NoBindingFactoryAdapter
import nu.validator.htmlparser.sax.HtmlParser
import nu.validator.htmlparser.common.XmlViolationPolicy
import org.xml.sax.InputSource

object Html {
  def parse(str: String): Node = {
    val hp = new HtmlParser
    hp.setNamePolicy(XmlViolationPolicy.ALLOW)

    val saxer = new NoBindingFactoryAdapter
    hp.setContentHandler(saxer)
    hp.parse(new InputSource(new StringReader(str)))
    saxer.rootElem
  }

  def load(url : String) : Node =
    parse(Source.fromURL(url, "UTF-8").mkString)
}

/** Partakeの情報をスクレイピングする */
case class Partake(url : String) {
  private val html =
    Html.load(url)

  private def guard(b : Boolean) =
    if(b) List(()) else List()

  /** イベントタイトル */
  lazy val title =
    (html \\ "h1").firstOption.map(_.text.trim).getOrElse("untitled")

  /** イベント出席者(除く:補欠) */
  lazy val users =
    for { div <- html \\ "div"
          _   <- guard { (div \ "@class").toString == "event-participants rad" }
          ul  <- (div \ "ul").firstOption.toList
          a   <- (ul \\ "a")
          _   <- guard { (a \ "@href").toString.contains("users") }
       } yield a.text.trim
}
