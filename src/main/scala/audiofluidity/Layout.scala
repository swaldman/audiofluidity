package audiofluidity

import audiofluidity.Episode

import java.nio.file.Path

object Layout:
  class Basic(
    indexHtmlName   : String = "index.html",
    episodesDirName : String = "episodes",
    rssFeedFileName : String = "feed.rss"
  ) extends Layout:
    def destEpisodeAudioFileName(podcast : Podcast, episode : Episode, extension : String) : String =
      s"${podcast.shortOpaqueName}-audio-episode-${episode.uid}.${extension}"
    def destEpisodeImageFileName(podcast : Podcast, episode : Episode, extension : String) : String =
      s"${podcast.shortOpaqueName}-coverart-episode-${episode.uid}.${extension}"
    def mainHtmlPath(podcast : Podcast) : Path = Path.of(indexHtmlName)
    def mainImagePath(podcast : Podcast) : Path =
      val extension = mainImageFileExtension(podcast)
      Path.of(s"${podcast.shortOpaqueName}-coverart.${extension}")
    def rssFeedPath(podcast : Podcast) : Path = Path.of(rssFeedFileName)
    def episodeRoot(podcast : Podcast, episode : Episode) : Path = Path.of(episodesDirName,s"episode-${episode.uid}")
    def episodeAudioPath(podcast : Podcast, episode : Episode) : Path =
      val extension = audioFileExtension(episode)
      val fileName = destEpisodeAudioFileName(podcast, episode, extension)
      episodeRoot(podcast,episode).resolve(fileName)
    def episodeHtmlPath(podcast : Podcast, episode : Episode)  : Path =
      episodeRoot(podcast,episode).resolve(indexHtmlName)
    def mbEpisodeImagePath(podcast : Podcast, episode : Episode) : Option[Path] = // only if there is an episode image
      mbEpisodeImageFileExtension(episode).map { extension =>
        val fileName = destEpisodeImageFileName(podcast, episode, extension)
        episodeRoot(podcast,episode).resolve(fileName)
      }

trait Layout:
  def destEpisodeAudioFileName(podcast : Podcast, episode : Episode, extension : String) : String
  def destEpisodeImageFileName(podcast : Podcast, episode : Episode, extension : String) : String

  // paths below are all relative to podcast root
  def mainHtmlPath(podcast : Podcast)                         : Path
  def mainImagePath(podcast : Podcast)                        : Path
  def rssFeedPath(podcast : Podcast)                          : Path
  def episodeRoot(podcast : Podcast, episode : Episode)       : Path
  def episodeAudioPath(podcast : Podcast, episode : Episode)  : Path
  def episodeHtmlPath(podcast : Podcast, episode : Episode)   : Path

  def mbEpisodeImagePath(podcast : Podcast, episode : Episode) : Option[Path] // only if there is an episode image

  def mainUrl( podcast : Podcast )                          : String = podcast.mainUrl // html file should be directory index
  def mainImageUrl(podcast : Podcast)                      : String = pathcat(podcast.mainUrl,mainImagePath(podcast).toString)
  def rssFeedPathUrl(podcast : Podcast)                     : String = pathcat(podcast.mainUrl,rssFeedPath(podcast).toString)
  def episodeAudioUrl(podcast : Podcast, episode : Episode) : String = pathcat(podcast.mainUrl,episodeAudioPath(podcast,episode).toString)
  def episodeUrl( podcast : Podcast, episode : Episode)     : String = pathcat(podcast.mainUrl,episodeRoot(podcast,episode).toString) // episode html file should be a directory index

  def mbEpisodeImageUrl(podcast : Podcast, episode : Episode) : Option[String] = mbEpisodeImagePath(podcast,episode).map(ip => pathcat(podcast.mainUrl,ip.toString))
