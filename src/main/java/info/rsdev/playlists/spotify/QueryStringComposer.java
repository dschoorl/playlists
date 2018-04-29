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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import info.rsdev.playlists.domain.Song;

public class QueryStringComposer {
	
	private static final Set<String> ARTIST_NOISE_WORDS = new HashSet<>();
	private static final Set<String> TITLE_NOISE_WORDS = new HashSet<>();
	private static final Map<String, String> ARTIST_ALIASSES = new HashMap<>();
	static {
		//all entries must be lower case
		ARTIST_NOISE_WORDS.addAll(Arrays.asList("feat", "feat.", "featuring", "ft.", "ft", "with", "and", "x", "+", "vs", "vs."));
		TITLE_NOISE_WORDS.addAll(Arrays.asList("the", "a", "de", "-"));
		
		ARTIST_ALIASSES.put("ATC", "A Touch Of Class");
	}
	
	public static String makeQueryString(Song song) throws UnsupportedEncodingException {
		String title = chooseOneWhenThereIsDoubleASide(song.title);
		Set<String> titleWords = splitToLowercaseWords(title);
		titleWords.removeAll(TITLE_NOISE_WORDS);
		
		Set<String> artistWords = splitToLowercaseWords(song.artist);
		artistWords.removeAll(ARTIST_NOISE_WORDS);
		artistWords = artistWords.stream().map(QueryStringComposer::replaceAliasses).collect(Collectors.toSet());
		
		StringBuilder query = new StringBuilder();
		appendField(query, "artist", artistWords);
		if (!artistWords.isEmpty()) {
			query.append(" ");
		}
		appendField(query, "title", titleWords);
		return query.toString();
	}
	
	private static String chooseOneWhenThereIsDoubleASide(String title) {
		if (title.contains("/")) {
			return title.substring(0, title.indexOf("/"));
		}
		return title;
	}

	private static String replaceAliasses(String word) {
		if (ARTIST_ALIASSES.containsKey(word)) {
			return ARTIST_ALIASSES.get(word);
		}
		return word;
	}
	
	private static StringBuilder appendField(StringBuilder query, String fieldName, Set<String> words) throws UnsupportedEncodingException {
		if (!words.isEmpty()) {
			query.append(fieldName).append(":");
			query.append(words.stream().collect(Collectors.joining(" ")));
		}
		return query;
	}

	private static Set<String> splitToLowercaseWords(String field) {
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