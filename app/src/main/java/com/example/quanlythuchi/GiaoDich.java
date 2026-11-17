package com.example.quanlythuchi;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GiaoDich {

    public static final int CHI = 0;
    public static final int THU = 1;

    public static final String[] NHOM_THU = {
            "Lương",
            "Thưởng",
            "Kinh doanh",
            "Thu nhập khác"
    };

    public static final String[] NHOM_CHI = {
            "Ăn uống",
            "Đi lại",
            "Mua sắm",
            "Giải trí",
            "Hóa đơn",
            "Sức khỏe",
            "Học tập",
            "Chi khác"
    };

    public final long id;
    public final long soTien;
    public final long thoiGian;

    public final int loai;

    public final String tieuDe;
    public final String danhMuc;
    public final String ghiChu;

    public GiaoDich(
            long id,
            String tieuDe,
            long soTien,
            int loai,
            String danhMuc,
            long thoiGian,
            String ghiChu
    ) {
        this.id = id;
        this.tieuDe = tieuDe;
        this.soTien = soTien;
        this.loai = loai;
        this.danhMuc = danhMuc;
        this.thoiGian = thoiGian;
        this.ghiChu = ghiChu;
    }

    public String ma() {
        return String.format(
                Locale.US,
                "GD%06d",
                id
        );
    }

    public static String tien(Number value) {
        return NumberFormat.getIntegerInstance(
                new Locale("vi", "VN")
        ).format(value) + " ₫";
    }

    public static String ngay(long time) {
        return new SimpleDateFormat(
                "dd/MM/yyyy · HH:mm",
                Locale.US
        ).format(new Date(time));
    }

    public static BigInteger tong(List<GiaoDich> data, int loai) {
        BigInteger total = BigInteger.ZERO;

        for (GiaoDich item : data) {
            if (item.loai == loai) {
                total = total.add(
                        BigInteger.valueOf(item.soTien)
                );
            }
        }

        return total;
    }
}