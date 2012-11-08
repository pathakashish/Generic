package com.aviras.generic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseAdapter";
	public static final String DB_NAME = "data.sqlite";
	private static final int DB_VERSION = 3;

	private static String DB_PATH = null;
	private static DatabaseHelper dbHelper;
	private static SQLiteDatabase db = null;

	public static DatabaseHelper init(Context context) {
		if (dbHelper == null) {
			Context applicationContext = context.getApplicationContext();
			dbHelper = new DatabaseHelper(applicationContext, DB_VERSION);
			db = dbHelper.getWritableDatabase();
		}
		return dbHelper;
	}

	public static SQLiteDatabase getSqliteDatabase() {
		if (db == null) {
			throw new RuntimeException(
					"DatabaseHelper.init() must be called before calling this method.");
		}
		return db;
	}

	private DatabaseHelper(Context context, int databaseVersion) {
		super(context, DB_NAME, null, databaseVersion);
		DB_PATH = "/data/data/" + context.getPackageName().replace("/", "")
				+ "/databases/";
		dbHelper = this;
		if (!databaseExists()) {
			try {
				copyDataBase(context);
			} catch (IOException e) {
				Log.e(TAG, "Error while copying database to device: " + e);
			}
		} else {
			Log.i(TAG, "Database already copied.");
		}
	}

	public void copyDatabaseToSdCard() throws IOException {
		if (Log.isInDebugMode()) {
			InputStream input = null;
			FileOutputStream output = null;

			byte[] buffer;
			try {
				File databaseFile = new File(
						Environment.getExternalStorageDirectory(),
						MyApplication.getInstance()
								.getString(R.string.app_name) + ".sqlite");
				if (databaseFile.exists()) {
					databaseFile.delete();
				}
				databaseFile.createNewFile();
				output = new FileOutputStream(databaseFile);
				input = new FileInputStream(new File(DB_PATH + DB_NAME));
				buffer = new byte[4096];
				int bytesReadCount;
				while ((bytesReadCount = input.read(buffer)) != -1) {
					output.write(buffer, 0, bytesReadCount);
				}
				output.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					input.close();
				}
				if (output != null) {
					output.close();
				}
			}
		}
	}

	/**
	 * Check if the database already copied to the device.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean databaseExists() {
		String dbFilePath = DB_PATH + DB_NAME;
		File dbFile = new File(dbFilePath);
		return dbFile.exists();
	}

	/**
	 * Copies your database FROM your local raw-folder to the just created empty
	 * database in the system folder, FROM where it can be accessed and handled.
	 * This is done by transfering bytestream.
	 * */
	private void copyDataBase(Context applicationContext) throws IOException {
		InputStream input = null;
		FileOutputStream output = null;
		int c;
		byte[] tmp;
		try {
			File dbPath = new File(DB_PATH);
			dbPath.mkdirs();
			File databaseFile = new File(DB_PATH, DB_NAME);
			databaseFile.createNewFile();
			output = new FileOutputStream(DB_PATH + DB_NAME);
			int i = 0;

			input = applicationContext.getResources().openRawResource(
					R.raw.data);
			tmp = new byte[1024];
			while ((c = input.read(tmp)) != -1) {
				i++;
				output.write(tmp, 0, c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
				output.close();
			}
		}
	}

	public void deleteDatabase() {
		String outFileName = DB_PATH + DB_NAME;
		File databaseFile = new File(outFileName);
		try {
			databaseFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Cursor executeSelectQuery(SQLiteDatabase db, String query) {
		Log.v(TAG, query);
		Cursor cur = null;
		cur = db.rawQuery(query, new String[] {});
		return cur;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVerions, int newVersion) {

	}

	public static void explainCursor(Cursor cur) {
		Log.i(TAG, "Columns count: " + cur.getColumnCount());
		for (int i = 0; i < cur.getColumnCount(); i++) {
			Log.i(TAG, "col[" + i + "]: " + cur.getColumnName(i));
		}
	}
}
