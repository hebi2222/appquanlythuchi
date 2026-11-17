package com.example.quanlythuchi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class ToDoDAO {

    private final DbHelper helper;

    public ToDoDAO(Context context) {
        helper = new DbHelper(context);
    }

    public List<ToDo> getAll() {
        List<ToDo> result = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.query(
                "todos",
                null,
                null,
                null,
                null,
                null,
                "_id DESC"
        )) {
            while (cursor.moveToNext()) {
                result.add(new ToDo(
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow("_id")
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("title")
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("content")
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("date")
                        ),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow("type")
                        ),
                        cursor.getInt(
                                cursor.getColumnIndexOrThrow("done")
                        ) == 1
                ));
            }
        }

        return result;
    }

    private ContentValues toValues(ToDo todo) {
        ContentValues values = new ContentValues();

        values.put("title", todo.title);
        values.put("content", todo.content);
        values.put("date", todo.date);
        values.put("type", todo.type);

        return values;
    }

    public boolean insert(ToDo todo) {
        ContentValues values = toValues(todo);
        values.put("done", todo.done ? 1 : 0);

        return helper.getWritableDatabase().insertOrThrow(
                "todos",
                null,
                values
        ) > 0;
    }

    public boolean update(ToDo todo) {
        return helper.getWritableDatabase().update(
                "todos",
                toValues(todo),
                "_id = ?",
                new String[]{String.valueOf(todo.id)}
        ) == 1;
    }

    public boolean updateStatus(long id, boolean done) {
        ContentValues values = new ContentValues();
        values.put("done", done ? 1 : 0);

        return helper.getWritableDatabase().update(
                "todos",
                values,
                "_id = ?",
                new String[]{String.valueOf(id)}
        ) == 1;
    }

    public boolean delete(long id) {
        return helper.getWritableDatabase().delete(
                "todos",
                "_id = ?",
                new String[]{String.valueOf(id)}
        ) == 1;
    }

    public void close() {
        helper.close();
    }
}