package info.rsdev.playlists.dao;

import static info.rsdev.playlists.dao.ChartsItemSqlConstants.ARTIST_COLUMN;
import static info.rsdev.playlists.dao.ChartsItemSqlConstants.TITLE_COLUMN;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import info.rsdev.playlists.domain.Song;

public class SongMapper implements RowMapper<Song> {

    @Override
    public Song mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Song(rs.getString(ARTIST_COLUMN), rs.getString(TITLE_COLUMN));
    }

}
