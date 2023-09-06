package info.rsdev.playlists.dao;

import static info.rsdev.playlists.dao.ChartsItemSqlConstants.GET_HIGHEST_WEEK;
import static info.rsdev.playlists.dao.ChartsItemSqlConstants.GET_HIGHEST_YEAR;
import static info.rsdev.playlists.dao.ChartsItemSqlConstants.GET_NEW_RELEASES_QUERY;
import static info.rsdev.playlists.dao.ChartsItemSqlConstants.INSERT_CHART_ITEM;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import info.rsdev.playlists.domain.ChartsItem;
import info.rsdev.playlists.domain.Song;
import info.rsdev.playlists.services.MusicChart;

@Component
@Profile("!ElasticSearch")
public class MySqlChartsItemDao implements ChartsItemDao {

    private static final SongMapper SONG_MAPPER = new SongMapper();

    private final JdbcTemplate jdbcTemplate;

    public MySqlChartsItemDao(DataSource datasource) {
        jdbcTemplate = new JdbcTemplate(datasource);
    }

    @Override
    public void insert(ChartsItem entry) {
        Song song = entry.song();
        jdbcTemplate.update(INSERT_CHART_ITEM, entry.chartName(), entry.year(), entry.weekNumber(), entry.position(),
                entry.isNewRelease(), song.artist(), song.title());
    }

    @Override
    public void insert(List<ChartsItem> chartItems) {
        this.jdbcTemplate.batchUpdate(INSERT_CHART_ITEM,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ChartsItem chartItem = chartItems.get(i);
                        ps.setString(1, chartItem.chartName());
                        ps.setShort(2, chartItem.year());
                        ps.setByte(3, chartItem.weekNumber());
                        ps.setShort(4, chartItem.position());
                        ps.setBoolean(5, chartItem.isNewRelease());
                        ps.setString(6, chartItem.song().artist());
                        ps.setString(7, chartItem.song().title());
                    }
                    public int getBatchSize() {
                        return chartItems.size();
                    }
                });
    }

    @Override
    public Collection<Song> getReleases(short year) {
        return jdbcTemplate.query(GET_NEW_RELEASES_QUERY, new Object[] { Short.valueOf(year) }, new int[] { Types.SMALLINT }, SONG_MAPPER);
    }

    @Override
    public short getHighestYearStored(MusicChart chart) {
        Short highestYear = jdbcTemplate.queryForObject(GET_HIGHEST_YEAR, new Object[] {chart.chartName()}, new int[] {Types.VARCHAR}, Short.class);
        if (highestYear != null) {
            return highestYear;
        }
        return -1;
    }

    @Override
    public byte getHighestWeekStored(MusicChart chart, short year) {
        Byte highestWeek = jdbcTemplate.queryForObject(GET_HIGHEST_WEEK, new Object[] {chart.chartName(), year}, new int[] {Types.VARCHAR, Types.SMALLINT}, Byte.class);
        if (highestWeek != null) {
            return highestWeek;
        }
        return -1;
    }

}
