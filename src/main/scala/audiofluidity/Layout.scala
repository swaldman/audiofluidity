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
    def destEpisodeCoverImageFileName(podcast : Podcast, episode : Episode, extension : String) : String =
      s"${podcast.shortOpaqueName}-coverart-episode-${episode.uid}.${extension}"
    def mainHtmlPath(podcast : Podcast) : Path = Path.of(indexHtmlName)
    def mainCoverImagePath(podcast : Podcast) : Path =
      val extension = mainImageFileExtension(podcast)
      Path.of(s"${podcast.shortOpaqueName}-coverart.${extension}")
    def rssFeedPath(podcast : Podcast) : Path = Path.of(rssFeedFileName)
    def episodeRoot(podcast : Podcast, episode : Episode) : Path = Path.of(episodesDirName,s"episode-${episode.uid}")

    // paths below are relative to episode root
    def episodeAudioPath(podcast : Podcast, episode : Episode) : Path =
      val extension = audioFileExtension(episode)
      Path.of( destEpisodeAudioFileName(podcast, episode, extension) )
    def episodeHtmlPath(podcast : Podcast, episode : Episode)  : Path =
      Path.of( indexHtmlName )
    def episodeBacklinkToRoot(podcast : Podcast, episode : Episode) : Path =
      Path.of("..","..")
    def mbEpisodeCoverImagePath(podcast : Podcast, episode : Episode) : Option[Path] = // only if there is an episode image
      mbEpisodeImageFileExtension(episode).map { extension =>
        Path.of( destEpisodeCoverImageFileName(podcast, episode, extension) )
      }

trait Layout:
  def destEpisodeAudioFileName(podcast : Podcast, episode : Episode, extension : String) : String
  def destEpisodeCoverImageFileName(podcast : Podcast, episode : Episode, extension : String) : String

  // paths below are all relative to podcast root
  def mainHtmlPath(podcast : Podcast)                         : Path
  def mainCoverImagePath(podcast : Podcast)                        : Path
  def rssFeedPath(podcast : Podcast)                          : Path
  def episodeRoot(podcast : Podcast, episode : Episode)       : Path

  // paths below are relative to episode root
  def episodeAudioPath(podcast : Podcast, episode : Episode)  : Path
  def episodeHtmlPath(podcast : Podcast, episode : Episode)   : Path
  def episodeBacklinkToRoot(podcast : Podcast, episode : Episode) : Path
  def mbEpisodeCoverImagePath(podcast : Podcast, episode : Episode) : Option[Path] // only if there is an episode image

