package org.codefirst.partakelist

import dispatch._
import dispatch.oauth._
import dispatch.oauth.OAuth._
import dispatch.twitter._

/** リストAPI */
case class TwitterList(consumer : Consumer, token: Token) extends Request(Twitter.host / "1" / "lists"){
  def create(name : String) =
    this / "create.json" << Map("name" -> name)  <@ (consumer, token)

  def show(name : String, screen_name : String) =
    this / "show.json" <<? Map("slug" -> name, "owner_screen_name" -> screen_name) <@ (consumer, token)
}

/** リストメンバーAPI */
case class TwitterMember(slug : String, owner : String, consumer : Consumer, token: Token) extends Request(Twitter.host / "1" / "lists" / "members"){
  def create(member : String) =
    this / "create.json" << Map("slug" -> slug, "owner_screen_name" -> owner, "screen_name" -> member) <@ (consumer, token)
}
