/*
package audiofluidity

import java.time.{Instant,ZonedDateTime}
import scala.collection.*

object RssFeed:
  val Version = "2.0"

  enum ValidDay:
    case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday

  case class Author(email : String)
  case class Category(domain : String, text : String)
  case class Channel(
    title          : Title,
    link           : Link,
    description    : Description,
    language       : Option[Language] = None,
    copyright      : Option[Copyright] = None,
    managingEditor : Option[ManagingEditor] = None,
    webMaster      : Option[Webmaster] = None,
    pubDate        : Option[PubDate] = None,
    lastBuildDate  : Option[LastBuildDate] = None,
    categories     : immutable.Seq[Category] = immutable.Seq.empty,
    generator      : Option[Generator] = None,
    docs           : Option[Docs] = Some(Docs()), // use default docs URL
    cloud          : Option[Cloud] = None,
    ttl            : Option[Ttl] = None,
    image          : Option[Image] = None,
    rating         : Option[Rating] = None,
    textInput      : Option[TextInput] = None,
    skipHours      : Option[SkipHours] = None,
    skipDays       : Option[SkipDays] = None,
    items          : immutable.Seq[Item]
  )
  case class Cloud(domain : String, port : Int, path : String, registerProcedure : String, protocol : String)
  case class Comments(url : String)
  case class Copyright(notice : String)
  case class Day(day : ValidDay)
  case class Description(text : String)
  case class Docs(url : String = "https://cyber.harvard.edu/rss/rss.html")
  case class Enclosure(url : String, length : Long, `type` : String)
  case class Generator(description : String)
  case class Guid(isPermalink : Boolean, id : String)
  case class Hour(hour : Int) // shuld be 1 to 24
  case class Height(pixels : Int)
  case class Image(url : Url, title : Title, link : Link, width : Option[Width], height : Option[Height], description : Option[Description])
  case class Item(
    title       : Title,
    link        : Link,
    description : Description,
    author      : Author,
    categories  : immutable.Seq[Category],
    comments    : Option[Comments],
    enclosure   : Option[Enclosure],
    guid        : Option[Guid],
    pubDate     : Option[PubDate],
    source      : Option[Source]
  )
  case class Language(code : LanguageCode)
  case class LastBuildDate(date : Instant)
  case class Link(location : String)
  case class ManagingEditor(email : String)
  case class Name(text : String)
  case class PubDate(date : Instant)
  case class Rating(contents : String) // this seems widely unutilized, not sure what the contents might look like exactly
  case class SkipDays(days : immutable.Seq[Day])
  case class SkipHours(hours : immutable.Seq[Hour])
  case class Source(url : String, title : String)
  case class TextInput(title : Title, description : Description, name : Name, link : Link) // mostly unutilized
  case class Title(text : String)
  case class Ttl(minutes : Int)
  case class Url(location : String)
  case class Webmaster(email : String)
  case class Width(pixels : Int)

case class RssFeed( channel : RssFeed.Channel )

*/
