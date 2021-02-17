package ru.mofrison.MobileMonitoring.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DataBase {

    private static final String DB_NAME = "MCData";
    private static final int DB_VERSION = 1;
    private static final String DB_TABLE = "Sensors";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_VALUE = "value";
    public static final String COLUMN_UM = "unit_of_measure";
    public static final String COLUMN_TIME = "time";

    private static final String DB_CREATE =
            "create table " + DB_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_NAME + " text, " +
                    COLUMN_VALUE + " text, " +
                    COLUMN_TIME + " int" +
                    ");";

    private static final String ORDER = COLUMN_TIME + " DESC";

    private final Context mCtx;


    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    public DataBase(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    public String getName(long id) {
        Cursor cursor = mDB.query(DB_TABLE, null, COLUMN_ID + " = ?", new String[] {String.valueOf(id)}, null, null,null);
        int columnIndex = cursor.getColumnIndex(COLUMN_NAME);
        if(cursor.moveToFirst()){
            return new String(cursor.getString(columnIndex));
        }
        return "";
    }
    // получить все данные из таблицы DB_TABLE
    public Cursor getAllData() {
        return mDB.query(DB_TABLE, null, null, null, null, null, ORDER);
    }

    public String[] getAllNames() {

        Cursor cursor = mDB.query(DB_TABLE, new String[] { COLUMN_NAME }, null, null, null, null, null);
        int columnIndex = cursor.getColumnIndex(COLUMN_NAME);

        String[] names = new String[cursor.getCount()];
        int i = 0;
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
          names[i++] = cursor.getString(columnIndex);
        }
        return names;
    }

    // добавить запись в DB_TABLE
    public void addRec(String name, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_VALUE, value);
        cv.put(COLUMN_TIME, System.currentTimeMillis());
        mDB.insert(DB_TABLE, null, cv);
    }

    // Обновить запись в DB_TABLE

    public  int update(String name, String value) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_VALUE, value);
//        cv.put(COLUMN_TIME, System.currentTimeMillis());
        int updCount = mDB.update(DB_TABLE, cv, COLUMN_NAME + " = ?",
                new String[] { name });
        return updCount;
    }

    // удалить запись из DB_TABLE по id
    public void delRec(long id) {
        mDB.delete(DB_TABLE, COLUMN_ID + " = " + id, null);
    }

    // удалить запись из DB_TABLE по полю name
    public void delRec(String name) {
        mDB.delete(DB_TABLE, COLUMN_NAME + " = ?", new String[] { name });
    }

    // удалить запись из DB_TABLE по полю name
    public void delAll() {
        mDB.delete(DB_TABLE, null, null);
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DB_CREATE);

//            ContentValues cv = new ContentValues();
//            for (int i = 1; i < 5; i++) {
//                cv.put(COLUMN_NAME, "sometext " + i);
//                cv.put(COLUMN_VALUE, i*i);
//                cv.put(COLUMN_TIME, System.currentTimeMillis());
//                db.insert(DB_TABLE, null, cv);
//            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
