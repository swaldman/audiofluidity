package audiofluidity

import scala.collection.*
import scala.xml.Elem  

import Element.{Content, Itunes}
import Xmlable.given

object Decoration:

  private def enc[T : Xmlable]( opt : Option[T] ) = opt.map( _.toElem )

  case class Channel (
    itunesCategories   : immutable.Seq[Itunes.Category],
    itunesImage        : Itunes.Image,
    itunesExplicit     : Itunes.Explicit                 = Itunes.Explicit(false),
    mbItunesAuthor     : Option[Itunes.Author]           = None,
    mbItunesBlock      : Option[Itunes.Block.type]       = None,
    mbItunesComplete   : Option[Itunes.Complete.type]    = None,
    mbItunesKeywords   : Option[Itunes.Keywords]         = None,
    mbItunesNewFeedUrl : Option[Itunes.NewFeedUrl]       = None,
    mbItunesOwner      : Option[Itunes.Owner]            = None,
    mbItunesSubtitle   : Option[Itunes.Subtitle]         = None,
    mbItunesSummary    : Option[Itunes.Summary]          = None,
    mbItunesTitle      : Option[Itunes.Title]            = None,
  ):
      def includesItunesElements  : Boolean = true
      def includesContentElements : Boolean = false

      def decorate( channelElem : Elem ) : Elem = 
        val newElems =
          (itunesCategories.map( _.toElem ) :+ itunesImage.toElem :+ itunesExplicit.toElem) ++
           enc(mbItunesAuthor) ++ enc(mbItunesBlock) ++ enc(mbItunesComplete) ++ enc(mbItunesKeywords) ++ enc(mbItunesNewFeedUrl) ++ enc(mbItunesOwner) ++ enc(mbItunesSummary) ++ enc(mbItunesTitle)
        channelElem.copy( child = channelElem.child ++ newElems )
  end Channel

  case class Item (
    mbContentEncoded    : Option[Content.Encoded]    = None,
    mbItunesBlock       : Option[Itunes.Block.type]  = None,
    mbItunesDuration    : Option[Itunes.Duration]    = None,
    mbItunesEpisode     : Option[Itunes.Episode]     = None,
    mbItunesEpisodeType : Option[Itunes.EpisodeType] = None,
    mbItunesExplicit    : Option[Itunes.Explicit]    = None,
    mbItunesImage       : Option[Itunes.Image]       = None,
    mbItunesKeywords    : Option[Itunes.Keywords]    = None,
    mbItunesSeason      : Option[Itunes.Season]      = None,
    mbItunesSubtitle    : Option[Itunes.Subtitle]    = None,
    mbItunesSummary     : Option[Itunes.Summary]     = None,
    mbItunesTitle       : Option[Itunes.Title]       = None,
  ):
      def includesItunesElements  : Boolean = List(mbItunesBlock, mbItunesDuration, mbItunesEpisode, mbItunesEpisodeType, mbItunesExplicit, mbItunesImage, mbItunesKeywords, mbItunesSeason, mbItunesSubtitle, mbItunesSummary, mbItunesTitle).exists(_.nonEmpty)
      def includesContentElements : Boolean = mbContentEncoded.nonEmpty

      def decorate( itemElem : Elem ) : Elem =
        val newElems =
          immutable.Seq.empty[Elem] ++ enc(mbContentEncoded) ++ enc(mbItunesBlock) ++ enc(mbItunesDuration) ++ enc(mbItunesEpisode) ++ enc(mbItunesEpisodeType) ++ enc(mbItunesExplicit) ++
          enc(mbItunesImage) ++ enc(mbItunesKeywords) ++ enc(mbItunesSeason) ++ enc(mbItunesSubtitle) ++ enc(mbItunesSummary) ++ enc(mbItunesTitle)
        itemElem.copy( child = itemElem.child ++ newElems )              
  end Item

end Decoration
    
