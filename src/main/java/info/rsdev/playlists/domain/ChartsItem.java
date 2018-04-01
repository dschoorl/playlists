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
package info.rsdev.playlists.domain;

public class ChartsItem {

    public final String chartName;

    public final short year;

    public final byte weekNumber;

    public final byte position;
    
    public final boolean isNewRelease;

    public final String artist;

    public final String title;

    public ChartsItem(String chartName, short year, byte weekNumber, byte position, boolean isNewRelease, 
            String artist, String title) {
        this.chartName = chartName;
        this.year = year;
        this.weekNumber = weekNumber;
        this.position = position;
        this.isNewRelease = isNewRelease;
        this.artist = artist;
        this.title = title;
    }

    @Override public String toString() {
        return "ChartsItem{" +
                "chartName='" + chartName + '\'' +
                ", year=" + year +
                ", weekNumber=" + weekNumber +
                ", position=" + position +
                ", new=" + isNewRelease +
                ", artist='" + artist + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
