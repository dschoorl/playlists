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
package info.rsdev.playlists.services;

/**
 * An enumeration of music charts that is understood by this software
 */
public enum MusicChart { 

    TOP40("Top 40", (short)1965, (byte)1),

    TIPPARADE("Tipparade", (short)1967, (byte)28);

    private String name;
    
    private short yearStarted;
    
    private byte weekStarted;

    MusicChart(String name, short yearStarted, byte weekStarted) {
        this.name = name;
        this.yearStarted = yearStarted;
        this.weekStarted = weekStarted;
    }
    
    public String getName() {
    	return name;
    }

    public short getYearStarted() {
    	return yearStarted;
    }
    
    public byte getWeekStarted() {
    	return weekStarted;
    }
    
}