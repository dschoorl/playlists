import { Song } from '../app/main/main.model';
import { lowerCaseNoDiacritics, removeArtistNoiseWords } from './SongMatcher';

/* List of words in a title NOT to use as search word. Only words of tree or more
 * characters are relevant, because shorter words are not used anyways
 */
const TITLE_NOISE_WORDS = ['the', 'radio', 'edit', 'mix', 'single'];

export function queryStringComposer(song: Song, year: number) {
  //normalize artist, split into parts
  const titleSearchWords = getTitleSearchWords(song.title);
  //extract main searchwords from title
  const artistSearchWords = getArtistSearchWords(song.artist);

  let queryString =
    'track:' +
    titleSearchWords.join(' ') +
    ' artist:' +
    artistSearchWords.join(' ');
  // if (year) {
  //   queryString += ` year:${year - 1}-${year + 1}`;
  // }
  console.log('Search for: ' + queryString);
  return queryString;
}

export function removeAnythingAfterDash(value: string) {
  const dashPosition = value.lastIndexOf('-');
  if (dashPosition > -1) {
    value = value.substring(0, dashPosition);
  }
  return value;
}

function getTitleSearchWords(title: string) {
  // strip 'comments' to the title, e.g. that it is from a movie etc.
  // assumed is that these comments are added after the name of the song,
  // and that they are preceeded with a dash.
  let normalizedTitle = lowerCaseNoDiacritics(title);
  normalizedTitle = removeAnythingAfterDash(normalizedTitle);

  const searchWords: string[] = [];
  const titleWords = normalizedTitle.split(' ');
  for (let word of titleWords) {
    if (word) {
      word = word.trim();
      //assume that too short words have no value in search
      if (word.length > 2 && !TITLE_NOISE_WORDS.includes(word)) {
        searchWords.push(word);
      }
    }
  }
  console.log(`Search words title '${title}' -> '${searchWords.join(' ')}'`);
  return searchWords;
}

function getArtistSearchWords(artists: string): string[] {
  let normalizedArtists = lowerCaseNoDiacritics(artists);

  // strip 'comments' to the title, e.g. that it is from a movie etc.
  // assumed is that these comments are added after the name of the song,
  // and that they are preceeded with a dash.
  normalizedArtists = removeAnythingAfterDash(normalizedArtists);
  normalizedArtists = removeArtistNoiseWords(normalizedArtists);
  normalizedArtists = normalizedArtists.replace(
    / featuring | feat. | feat | ft. | ft | with | x | mmv | m.m.v. | vs | vs. | + | and /g,
    '&'
  );

  const disectedArtists: string[] = [];
  // when there are more than 2 artists, it is not uncommon to use comma to seperate
  // the first artist name and ampersands to separate all remaining artist names
  const commaPosition = normalizedArtists.indexOf(',');
  if (commaPosition > -1) {
    disectedArtists.push(normalizedArtists.substring(0, commaPosition).trim());
    normalizedArtists = normalizedArtists.substring(commaPosition + 1);
  }

  const otherArtists = normalizedArtists.split('&');
  for (let artist of otherArtists) {
    disectedArtists.push(artist.trim());
  }

  console.log(
    `Search words artists '${artists}' -> '${disectedArtists.join(' ')}'`
  );
  return disectedArtists;
}
