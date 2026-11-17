package com.example.quanlythuchi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public DbHelper(Context context) {
        super(
                context.getApplicationContext(),
                "project02_todo.db",
                null,
                1
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE todos (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "title TEXT NOT NULL," +
                        "content TEXT NOT NULL," +
                        "date TEXT NOT NULL," +
                        "type TEXT NOT NULL," +
                        "done INTEGER NOT NULL DEFAULT 0 CHECK(done IN (0,1)))"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        throw new IllegalStateException(
                "Chưa có migration cho phiên bản dữ liệu này."
        );
    }
}