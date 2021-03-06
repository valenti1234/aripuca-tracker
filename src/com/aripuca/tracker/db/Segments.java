package com.aripuca.tracker.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aripuca.tracker.utils.Utils;

public class Segments {

	public static final String TABLE_NAME = "segments";

	public static final String TABLE_CREATE =
			"CREATE TABLE " + TABLE_NAME +
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT," +
					"track_id INTEGER NOT NULL," +
					"segment_index INTEGER," +
					"distance REAL," +
					"total_time INTEGER," +
					"moving_time INTEGER," +
					"max_speed REAL," +
					"max_elevation REAL," +
					"min_elevation REAL," +
					"elevation_gain REAL," +
					"elevation_loss REAL," +
					"start_time INTEGER NOT NULL," +
					"finish_time INTEGER)";

	public static ArrayList<Segment> getAll(SQLiteDatabase db, long trackId) {

		ArrayList<Segment> segments = new ArrayList<Segment>();

		//String sql = "SELECT * FROM " + TABLE_NAME + " WHERE track_id = " + trackId;

		String sql = "SELECT s.*, COUNT(s.segment_index) AS points_count " +
				"FROM segments AS s " +
				"LEFT JOIN track_points AS tp " +
				"ON s.track_id = tp.track_id AND s.segment_index = tp.segment_index " +
				"WHERE s. track_id=" + trackId + " " +
				"GROUP BY s.segment_index";

		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();

		while (cursor.isAfterLast() == false) {

			Segment wp = new Segment(cursor);

			segments.add(wp);

			cursor.moveToNext();
		}

		cursor.close();

		return segments;
	}

	public static int getCount(SQLiteDatabase db, long trackId) {

		String sql = "SELECT COUNT(*) AS count FROM " + TABLE_NAME + " WHERE track_id = " + trackId;

		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		int count = cursor.getInt(cursor.getColumnIndex("count"));
		cursor.close();

		return count;
	}

	/**
	 * Returns number of started segments. If track recording is interrupted for
	 * some reason, last segment will not be inserted in segments table, but
	 * track_points table will have all the points from last segment recorded
	 * with last segment_index
	 * 
	 * @param db
	 * @param trackId
	 * @return
	 */
	public static int getStartedCount(SQLiteDatabase db, long trackId) {

		String sql = "SELECT COUNT(DISTINCT segment_index) AS count FROM track_points WHERE track_id=" + trackId;

		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();
		int count = cursor.getInt(cursor.getColumnIndex("count"));
		cursor.close();

		return count;
	}

	public static Segment get(SQLiteDatabase db, long trackId, int segmentIndex) {

		String sql = "SELECT s.*, COUNT(tp._id) AS points_count " +
				"FROM segments AS s " +
				"LEFT JOIN track_points AS tp " +
				"ON s.track_id = tp.track_id AND s.segment_index = tp.segment_index " +
				"WHERE s.track_id=" + trackId + " AND s.segment_index=" + segmentIndex;

		Cursor cursor = db.rawQuery(sql, null);
		cursor.moveToFirst();

		Segment segment = new Segment(cursor);

		cursor.close();

		return segment;
	}

	/**
	 * Insert segment record
	 * 
	 * @param db
	 * @param segment
	 * @return
	 */
	public static long insert(SQLiteDatabase db, Segment segment) {

		ContentValues values = new ContentValues();

		values.put("track_id", segment.getTrackId());
		values.put("segment_index", segment.getSegmentIndex());

		values.put("distance", Utils.formatNumber(segment.getDistance(), 1));

		values.put("total_time", segment.getTotalTime());
		values.put("moving_time", segment.getMovingTime());

		values.put("max_speed", Utils.formatNumber(segment.getMaxSpeed(), 2));

		values.put("max_elevation", Utils.formatNumber(segment.getMaxElevation(), 1));
		values.put("min_elevation", Utils.formatNumber(segment.getMinElevation(), 1));

		values.put("elevation_gain", segment.getElevationGain());
		values.put("elevation_loss", segment.getElevationLoss());

		values.put("start_time", segment.getStartTime());
		values.put("finish_time", segment.getFinishTime());

		return db.insertOrThrow("segments", null, values);

	}

	/**
	 * Update segment record
	 * 
	 * @param db
	 * @param segment
	 * @return
	 */
	public static int update(SQLiteDatabase db, Segment segment) {

		ContentValues values = new ContentValues();

		values.put("distance", Utils.formatNumber(segment.getDistance(), 1));

		values.put("total_time", segment.getTotalTime());
		values.put("moving_time", segment.getMovingTime());

		values.put("max_speed", Utils.formatNumber(segment.getMaxSpeed(), 2));

		values.put("max_elevation", Utils.formatNumber(segment.getMaxElevation(), 1));
		values.put("min_elevation", Utils.formatNumber(segment.getMinElevation(), 1));

		values.put("elevation_gain", segment.getElevationGain());
		values.put("elevation_loss", segment.getElevationLoss());

		values.put("finish_time", segment.getFinishTime());

		//		return db.insertOrThrow("segments", null, values);
		return db.update("segments", values, "_id=?", new String[] { String.valueOf(segment.getId()) });

	}

}
