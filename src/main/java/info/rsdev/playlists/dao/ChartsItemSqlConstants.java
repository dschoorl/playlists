package info.rsdev.playlists.dao;

public final class ChartsItemSqlConstants {

	private ChartsItemSqlConstants() {
		// do not instantiate this bag of constants
	}

	/*
	 * For now, just like in the NoSql setup, do not apply data normalization. We
	 * may do that later.
	 */
	public static final String CHART_ITEM_TABLE = "chart_items";

	public static final String CHART_NAME_COLUMN = "chart_id";
	public static final String YEAR_COLUMN = "year";
	public static final String WEEK_COLUMN = "week_number";
	public static final String POSITION_COLUMN = "position";
	public static final String IS_NEW_IN_CHART = "is_newcommer";
	public static final String ARTIST_COLUMN = "artist";
	public static final String TITLE_COLUMN = "title";
	
	public static final String GET_NEW_RELEASES_QUERY = String.format("select %s, %s from %s where %s = ? and %s = true group by %s, %s",
	        ARTIST_COLUMN, TITLE_COLUMN, CHART_ITEM_TABLE, YEAR_COLUMN, IS_NEW_IN_CHART, ARTIST_COLUMN, TITLE_COLUMN);
	
	public static final String GET_HIGHEST_YEAR = String.format("select max(%s) from %s where %s = ?", 
	        YEAR_COLUMN, CHART_ITEM_TABLE, CHART_NAME_COLUMN);
	
    public static final String GET_HIGHEST_WEEK = String.format("select max(%s) from %s where %s = ? and %s = ?", 
            WEEK_COLUMN, CHART_ITEM_TABLE, CHART_NAME_COLUMN, YEAR_COLUMN);
    
    public static final String INSERT_CHART_ITEM = String.format("insert into %s (%s, %s, %s, %s, %s, %s, %s) values (?, ?, ?, ?, ?, ?, ?)",
            CHART_ITEM_TABLE, CHART_NAME_COLUMN, YEAR_COLUMN, WEEK_COLUMN, POSITION_COLUMN, IS_NEW_IN_CHART, ARTIST_COLUMN, TITLE_COLUMN);
	
}
