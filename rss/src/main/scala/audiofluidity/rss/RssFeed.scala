package audiofluidity.rss

import java.time.ZonedDateTime
import scala.collection.*
import scala.xml.{Elem,PrettyPrinter}
import Element.{Channel, Item}
import Xmlable.given

object RssFeed:
  private val EmptyChannelRssElem =
    Element.Rss(channel = Channel(Element.Title(""),Element.Link(""),Element.Description(""), items=Nil)).toElem

  def create[T : Itemable] (
    title              : String,
    linkUrl            : String,
    description        : String,
    itemSources        : immutable.Seq[T],
    language           : Option[LanguageCode]            = None,
    copyright          : Option[String]                  = None,
    managingEditor     : Option[String]                  = None,
    webmaster          : Option[String]                  = None,
    pubDate            : Option[ZonedDateTime]           = None,
    lastBuildDate      : Option[ZonedDateTime]           = None,
    categories         : immutable.Seq[Element.Category] = Nil,
    generator          : Option[String]                  = None,
    cloud              : Option[Element.Cloud]           = None,
    ttlMinutes         : Option[Int]                     = None,
    image              : Option[Element.Image]           = None,
    rating             : Option[String]                  = None,
    textInput          : Option[Element.TextInput]       = None,
    skipHours          : Option[Element.SkipHours]       = None,
    skipDays           : Option[Element.SkipDays]        = None,
    channelDecorations : immutable.Seq[Elem]             = Nil,
    namespaces         : List[Namespace]                 = Nil
  ) : RssFeed =
    val (items, itemsD) =
      val (itemsReversed, itemsDReversed ) =
        itemSources.foldLeft( Tuple2(Nil : List[Item], Nil : List[immutable.Seq[Elem]]) ){ (accum, next) =>
          ( next.toItem :: accum(0), next.itemDecorations :: accum(1) )
        }
      (itemsReversed.reverse, itemsDReversed.reverse)

    // where we had convenience circumventions, we have to build our elements
    val elemTitle = Element.Title(title)
    val elemLink  = Element.Link(linkUrl)
    val elemDesc  = Element.Description(description)

    val mbLanguage       = language.map( Element.Language.apply )
    val mbCopyright      = copyright.map( Element.Copyright.apply )
    val mbManagingEditor = managingEditor.map( Element.ManagingEditor.apply )
    val mbWebmaster      = webmaster.map( Element.WebMaster.apply )
    val mbPubDate        = pubDate.map( Element.PubDate.apply )
    val mbLastBuildDate  = lastBuildDate.map( Element.LastBuildDate.apply )
    val mbGenerator      = generator.map( Element.Generator.apply )
    val mbTtl            = ttlMinutes.map( Element.Ttl.apply )
    val mbRating         = rating.map( Element.Rating.apply )

    val channel = Element.Channel(
      title = elemTitle,
      link = elemLink,
      description = elemDesc,
      language = mbLanguage,
      copyright = mbCopyright,
      managingEditor = mbManagingEditor,
      webMaster = mbWebmaster,
      pubDate = mbPubDate,
      lastBuildDate = mbLastBuildDate,
      categories = categories,
      generator = mbGenerator,
      docs = Some(Element.Docs()), // use the default docs URL pretty much always
      cloud = cloud,
      ttl = mbTtl,
      image = image,
      rating = mbRating,
      textInput = textInput,
      skipHours = skipHours,
      skipDays = skipDays,
      items = items
    )

    RssFeed( channel, channelDecorations = channelDecorations.toList, itemDecorations = itemsD, namespaces = namespaces )


case class RssFeed( channel : Channel, channelDecorations : immutable.Seq[Elem], itemDecorations : immutable.Seq[immutable.Seq[Elem]], namespaces : List[Namespace] ):

  // we use a val rather than a def or lazy val so we effectively validate itemDecoration length on construction
  // it's rare one would build an RssFeed without intending to generate the Xml
  val rssElem : Elem =
    val undecoratedChannel = channel.toElem
    val (undecoratedItems, otherChannelChildren) =
      // i wish there were some hybrid of collect and partition...
      val (undecoratedItemNodes, otherChildren) = undecoratedChannel.child.partition {
        // for some reason, destructuring matches aren't working
        //case Elem(null, "item", _, _, _, _) => true
        case elem : Elem if elem.prefix == null && elem.label == "item" => true
        case _                                                          => false
      }
      (undecoratedItemNodes.map(_.asInstanceOf[Elem]), otherChildren)

    val decoratedItems =
      val mustDecorateItems = itemDecorations.nonEmpty
      if mustDecorateItems && (undecoratedItems.length != itemDecorations.length) then
        throw new IllegalArgumentException (
          s"itemDecorations must be the same length as the number of items, or else length 0. (items: ${undecoratedItems.size}, itemDecorations: ${itemDecorations.size}"
        )
      else
        undecoratedItems.zip(itemDecorations).map( tup => tup(0).copy(child = tup(0).child ++ tup(1)) )
    val decoratedChannelElem = undecoratedChannel.copy(child = otherChannelChildren ++ channelDecorations ++ decoratedItems)

    val decoratedRssChildren =
      RssFeed.EmptyChannelRssElem.child.map {
        // for some reason, destructuring matches aren't working
        //case Elem(null, "channel", _, _, _, _) => decoratedChannelElem
        case elem : Elem if elem.prefix == null && elem.label == "channel" => decoratedChannelElem
        case other => other
      }
    RssFeed.EmptyChannelRssElem.copy(scope=Namespace.toBinding(namespaces),child=decoratedRssChildren)

  lazy val asXmlText : String =
    val pp = new PrettyPrinter(80,2)
    val noXmlDeclarationPretty = pp.format(rssElem)
    s"<?xml version='1.0' encoding='UTF-8'?>\n\n${noXmlDeclarationPretty}"

  lazy val bytes : immutable.Seq[Byte] =
    immutable.ArraySeq.ofByte(asXmlText.getBytes(scala.io.Codec.UTF8.charSet))
