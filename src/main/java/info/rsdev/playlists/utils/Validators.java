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
package info.rsdev.playlists.utils;

import java.util.regex.Pattern;

public interface Validators {

    Pattern POSTCODE_NL_PATTERN = Pattern.compile("\\d{4}[A-Z]{2}");

    static String hasExactLength(String value, int length) {
        if (value != null) {
            if (value.length() != length) {
                throw new RuntimeException(String.format("Expected value to have length of %d characters: %s", length, value));
            }
        }
        return value;
    }

    static String notNull(String value) {
        if (value == null) {
            throw new RuntimeException("Field should not be null");
        }
        return value;
    }

    static String hasPostcodeNLFormat(String value) {
        if (value == null) {
            return null;
        }
        if (!POSTCODE_NL_PATTERN.matcher(value).matches()) {
            throw new RuntimeException(String.format("Not a valid Dutch zipcode format: %s", value));
        }
        return value;
    }
}
