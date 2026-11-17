package com.example.quanlythuchi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ThuChiDb extends SQLiteOpenHelper {

    public ThuChiDb(Context context) {
        super(
                context.getApplicationContext(),
                "quanlythuchi.db",
                null,
                1
        );
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE giao_dich (" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "tieu_de TEXT NOT NULL," +
                        "so_tien INTEGER NOT NULL " +
                        "CHECK(so_tien > 0 AND so_tien <= 999999999999)," +
                        "loai INTEGER NOT NULL CHECK(loai IN (0,1))," +
                        "danh_muc TEXT NOT NULL," +
                        "thoi_gian INTEGER NOT NULL," +
                        "ghi_chu TEXT NOT NULL DEFAULT '')"
        );

        db.execSQL(
                "CREATE INDEX idx_giao_dich_time " +
                        "ON giao_dich(thoi_gian)"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        throw new IllegalStateException(
                "Cần bổ sung migration trước khi tăng version."
        );
    }

    public List<GiaoDich> layDanhSach(
            Integer loai,
            Long id,
            Long tu,
            Long den
    ) {
        List<String> args = new ArrayList<>();

        StringBuilder where = new StringBuilder("1=1");

        if (loai != null) {
            where.append(" AND loai=?");
            args.add(String.valueOf(loai));
        }

        if (id != null) {
            where.append(" AND _id=?");
            args.add(String.valueOf(id));
        }

        if (tu != null && den != null) {
            where.append(" AND thoi_gian>=? AND thoi_gian<?");

            args.add(String.valueOf(tu));
            args.add(String.valueOf(den));
        }

        List<GiaoDich> result = new ArrayList<>();

        try (Cursor c = getReadableDatabase().query(
                "giao_dich",
                null,
                where.toString(),
                args.toArray(new String[0]),
                null,
                null,
                "thoi_gian DESC, _id DESC"
        )) {
            while (c.moveToNext()) {
                result.add(new GiaoDich(
                        c.getLong(
                                c.getColumnIndexOrThrow("_id")
                        ),
                        c.getString(
                                c.getColumnIndexOrThrow("tieu_de")
                        ),
                        c.getLong(
                                c.getColumnIndexOrThrow("so_tien")
                        ),
                        c.getInt(
                                c.getColumnIndexOrThrow("loai")
                        ),
                        c.getString(
                                c.getColumnIndexOrThrow("danh_muc")
                        ),
                        c.getLong(
                                c.getColumnIndexOrThrow("thoi_gian")
                        ),
                        c.getString(
                                c.getColumnIndexOrThrow("ghi_chu")
                        )
                ));
            }
        }

        return result;
    }

    public List<GiaoDich> tatCa() {
        return layDanhSach(
                null,
                null,
                null,
                null
        );
    }

    public boolean luu(GiaoDich gd) {
        ContentValues values = new ContentValues();

        values.put("tieu_de", gd.tieuDe);
        values.put("so_tien", gd.soTien);
        values.put("loai", gd.loai);
        values.put("danh_muc", gd.danhMuc);
        values.put("thoi_gian", gd.thoiGian);
        values.put("ghi_chu", gd.ghiChu);

        SQLiteDatabase db = getWritableDatabase();

        if (gd.id == 0) {
            return db.insertOrThrow(
                    "giao_dich",
                    null,
                    values
            ) > 0;
        }

        return db.update(
                "giao_dich",
                values,
                "_id=?",
                new String[]{String.valueOf(gd.id)}
        ) == 1;
    }

    public boolean xoa(long id) {
        return getWritableDatabase().delete(
                "giao_dich",
                "_id=?",
                new String[]{String.valueOf(id)}
        ) == 1;
    }
}