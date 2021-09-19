package audiofluidity

import scala.collection.*

import Element.{Content, Itunes}

object Decoration:
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
    end Item
end Decoration
    
