package info.rsdev.playlists.dao;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.services.MusicChart;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class MySqlChartsItemDao implements ChartsItemDao {
    @Override
    public boolean setupStoreWhenNeeded() {
        return false;
    }

    @Override
    public void saveOrUpdate(ChartsItem chartsItem) {

    }

    @Override
    public Collection<Song> getReleases(short year) {
        return null;
    }

    @Override
    public short getHighestYearStored(MusicChart chart) {
        return 0;
    }

    @Override
    public byte getHighestWeekStored(MusicChart chart, short year) {
        return 0;
    }
}
