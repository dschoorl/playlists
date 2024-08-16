import { Track } from '@spotify/web-api-ts-sdk';
import { Song } from '../app/main/main.model';
import stringComparison from 'string-comparison';
import { removeAnythingAfterDash } from './QueryStringComposer';

/**
 * Based on the scores from {scoreMatch} decide whether we assert that
 * the two song representations deal about the same song
 * @param chartItem
 * @param track
 * @returns true if we think it concerns the same song, false otherwise
 */
export function matchesTrack(chartItem: Song, track: Track) {
  const score = scoreMatch(chartItem, track);
  return score[0] >= 75 && score[1] >= 75;
}

/**
 * Given a song from the charts-database and a track from Spotify,
 * express how likely it is that they represent the same song in real life
 *
 * @param chartItem The song from the charts database
 * @param track The Track information from Spotify
 * @returns an array with a similarity score for title and for the artist, each score
 * being between 0 and 100. The higher the number, the more convinced the software is
 * that it is the same.
 */
export function scoreMatch(
  chartItem: Song,
  track: Track
): [titleScore: number, artistScore: number] {
  const titleMatchCertainty = titleMatches(
    lowerCaseNoDiacritics(chartItem.title),
    lowerCaseNoDiacritics(track.name)
  );
  const artistMatchCertainty = artistMatches(
    lowerCaseNoDiacritics(chartItem.artist),
    track.artists.map((a) => lowerCaseNoDiacritics(a.name))
  );

  return [titleMatchCertainty, artistMatchCertainty];
}

/**
 * Normalize a string value by removing diacritics and putting it in all lowercase,
 * so it may be easier comparing values from different sources (e.g. title or artist name).
 */
export function lowerCaseNoDiacritics(value: string): string {
  return value
    .toLowerCase()
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '');
}

function titleMatches(chartsTitle: string, spotifyTitle: string) {
  if (chartsTitle === spotifyTitle) {
    return 100;
  }

  chartsTitle = removeAnythingAfterDash(chartsTitle);
  spotifyTitle = removeAnythingAfterDash(spotifyTitle);
  if (chartsTitle.length !== spotifyTitle.length) {
    // check if beginning matches
    const maxCompareLength = Math.min(chartsTitle.length, spotifyTitle.length);
    const relevantStartChartsTitle = chartsTitle.substring(0, maxCompareLength);
    const relevantStartSpotifyTitle = spotifyTitle.substring(
      0,
      maxCompareLength
    );
    if (relevantStartChartsTitle === relevantStartSpotifyTitle) {
      return 80;
    }

    // check if ending matches
    const relevantEndChartsTitle = chartsTitle.substring(
      chartsTitle.length - maxCompareLength
    );
    const relevantEndSpotifyTitle = spotifyTitle.substring(
      spotifyTitle.length - maxCompareLength
    );
    if (relevantEndChartsTitle === relevantEndSpotifyTitle) {
      return 80;
    }
  }

  return (
    stringComparison.jaroWinkler.similarity(chartsTitle, spotifyTitle) * 100
  );
}

function artistMatches(chartsArtist: string, spotifyArtist: string[]) {
  const numberOfArtists = spotifyArtist.length;
  if (numberOfArtists === 1) {
    if (
      removeArtistNoiseWords(spotifyArtist[0]) ===
      removeArtistNoiseWords(chartsArtist)
    ) {
      return 100;
    } else {
      return (
        stringComparison.jaroWinkler.similarity(
          removeArtistNoiseWords(chartsArtist),
          removeArtistNoiseWords(spotifyArtist[0])
        ) * 100
      );
    }
  }

  let matchedArtists = 0;
  chartsArtist = removeArtistNoiseWords(chartsArtist);
  for (const artist of spotifyArtist) {
    if (chartsArtist.includes(removeArtistNoiseWords(artist))) {
      matchedArtists++;
    }
  }
  if (matchedArtists === numberOfArtists) {
    return 100;
  } else {
    if (matchedArtists >= 1) {
      return 75;
    }
  }
  return 0;
}

export function removeArtistNoiseWords(artist: string) {
  return artist.replace(/the /g, '');
}
