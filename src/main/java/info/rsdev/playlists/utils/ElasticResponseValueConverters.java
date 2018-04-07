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

public interface ElasticResponseValueConverters {
	
	static short toShort(Object value) {
		if (value instanceof Integer) {
			int asInt = ((Integer)value).intValue();
			if ((asInt < Short.MIN_VALUE) || (asInt > Short.MAX_VALUE)) {
				throw new RuntimeException(String.format("Number too big to fit in a short: %d", asInt));
			}
			return (short)asInt;
		}
		
		throw new IllegalArgumentException(String.format("Integer value was expected: %d", value));
	}

	static byte toByte(Object value) {
		if (value instanceof Integer) {
			int asInt = ((Integer)value).intValue();
			if ((asInt < Byte.MIN_VALUE) || (asInt > Byte.MAX_VALUE)) {
				throw new RuntimeException(String.format("Number too big to fit in a byte: %d", asInt));
			}
			return (byte)asInt;
		}
		
		throw new IllegalArgumentException(String.format("Integer value was expected: %s", value));
	}

}
