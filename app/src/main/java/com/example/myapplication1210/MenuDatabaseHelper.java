package com.example.myapplication1210;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 以「重建表」的方式來實現 order_time DEFAULT (datetime('now','localtime'))
 */
public class MenuDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "menu.db";
    private static final int DATABASE_VERSION = 2; // 升級版號會自動觸發 onUpgrade

    public MenuDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // 第一次安裝 (沒有舊 DB) → 建表
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "price REAL," +
                "quantity INTEGER," +
                "order_time TEXT DEFAULT (datetime('now','localtime'))" +
                ")";
        db.execSQL(createTable);
    }

    // 舊版本升級 → 重建表
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 1) 改名
            db.execSQL("ALTER TABLE menu_items RENAME TO menu_items_old");
            // 2) 建新表
            String createTable = "CREATE TABLE IF NOT EXISTS menu_items (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "price REAL," +
                    "quantity INTEGER," +
                    "order_time TEXT DEFAULT (datetime('now','localtime'))" +
                    ")";
            db.execSQL(createTable);
            // 3) 搬移舊資料
            db.execSQL("INSERT INTO menu_items (name, price, quantity) " +
                    "SELECT name, price, quantity FROM menu_items_old");
            // 4) 刪除舊表
            db.execSQL("DROP TABLE menu_items_old");
        }
    }
}
