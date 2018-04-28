/*
 * Copyright 2018 Red Star Development.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.rsdev.playlists.spotify;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import info.rsdev.playlists.domain.Song;

public class QueryStringComposer {
	
	private static final Set<String> ARTIST_NOISE_WORDS = new HashSet<>();
	private static final Set<String> TITLE_NOISE_WORDS = new HashSet<>();
	static {
		//all entries must be lower case
		ARTIST_NOISE_WORDS.addAll(Arrays.asList("feat", "feat.", "featuring", "ft.", "ft", "with", "and", "x", "+"));
		TITLE_NOISE_WORDS.addAll(Arrays.asList("the", "a", "de"));
	}
	
	public static String makeQueryString(Song song) throws UnsupportedEncodingException {
		Set<String> titleWords = splitToWords(song.title);
		titleWords.removeAll(TITLE_NOISE_WORDS);
		Set<String> artistWords = splitToWords(song.artist);
		artistWords.removeAll(ARTIST_NOISE_WORDS);
		
		StringBuilder query = new StringBuilder();
		appendField(query, "artist", artistWords);
		if (!artistWords.isEmpty()) {
			query.append(" ");
		}
		appendField(query, "title", titleWords);
		return query.toString();
	}
	
	private static StringBuilder appendField(StringBuilder query, String fieldName, Set<String> words) throws UnsupportedEncodingException {
		if (!words.isEmpty()) {
			query.append(fieldName).append(":");
			query.append(words.stream().collect(Collectors.joining(" ")));
		}
		return query;
	}

	private static Set<String> splitToWords(String field) {
		String[] words = field.toLowerCase().split("[\\s\\(\\)\\,\\&]");
		Set<String> result = new TreeSet<>();	//apply alphabetic ordering to make unit testing easier
		for (String word : words) {
			if ((word != null) && !word.isEmpty()) {
				result.add(word);
			}
		}
		return result;
	}

}
