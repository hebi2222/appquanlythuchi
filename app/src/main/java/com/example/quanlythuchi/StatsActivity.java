package com.example.quanlythuchi;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsActivity extends BaseActivity {

    private Spinner month;
    private EditText year;

    private TextView period;
    private TextView income;
    private TextView expense;
    private TextView balance;

    private LinearLayout groups;

    private int appliedMonth;
    private int appliedYear;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        screen(
                R.layout.activity_stats,
                "Thống kê thu chi",
                true
        );

        month = findViewById(R.id.spMonth);
        year = findViewById(R.id.edtYear);

        period = findViewById(R.id.tvPeriod);
        income = findViewById(R.id.tvIncome);
        expense = findViewById(R.id.tvExpense);
        balance = findViewById(R.id.tvBalance);

        groups = findViewById(R.id.categoryRows);

        List<String> months = new ArrayList<>();

        months.add("Cả năm");

        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }

        ArrayAdapter<String> choices = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                months
        );

        choices.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        month.setAdapter(choices);

        Calendar now = Calendar.getInstance();

        appliedMonth = state == null
                ? now.get(Calendar.MONTH) + 1
                : state.getInt("month");

        appliedYear = state == null
                ? now.get(Calendar.YEAR)
                : state.getInt("year");

        month.setSelection(appliedMonth);
        year.setText(String.valueOf(appliedYear));

        findViewById(R.id.btnApply).setOnClickListener(v -> {
            try {
                int value = Integer.parseInt(
                        year.getText().toString().trim()
                );

                if (value < 1900 || value > 9999) {
                    throw new NumberFormatException();
                }

                appliedYear = value;
                appliedMonth = month.getSelectedItemPosition();

                reload();

            } catch (NumberFormatException error) {
                year.setError(
                        "Nhập năm từ 1900 đến 9999"
                );
            }
        });
    }

    @Override
    protected void reload() {
        Calendar start = Calendar.getInstance();

        start.clear();

        start.set(
                appliedYear,
                appliedMonth == 0 ? 0 : appliedMonth - 1,
                1
        );

        Calendar end = (Calendar) start.clone();

        end.add(
                appliedMonth == 0
                        ? Calendar.YEAR
                        : Calendar.MONTH,
                1
        );

        long from = start.getTimeInMillis();
        long to = end.getTimeInMillis();

        String label = appliedMonth == 0
                ? "Năm " + appliedYear
                : "Tháng " + appliedMonth + "/" + appliedYear;

        loadLatest(
                () -> db.layDanhSach(
                        null,
                        null,
                        from,
                        to
                ),
                data -> {
                    BigInteger thu = GiaoDich.tong(
                            data,
                            GiaoDich.THU
                    );

                    BigInteger chi = GiaoDich.tong(
                            data,
                            GiaoDich.CHI
                    );

                    period.setText(
                            label
                                    + " · "
                                    + data.size()
                                    + " giao dịch"
                    );

                    income.setText(
                            GiaoDich.tien(thu)
                    );

                    expense.setText(
                            GiaoDich.tien(chi)
                    );

                    balance.setText(
                            "Chênh lệch: "
                                    + GiaoDich.tien(thu.subtract(chi))
                    );

                    showGroups(
                            data,
                            thu.add(chi)
                    );
                }
        );
    }

    private void showGroups(
            List<GiaoDich> data,
            BigInteger total
    ) {
        groups.removeAllViews();

        Map<String, BigInteger> sums = new HashMap<>();

        for (GiaoDich gd : data) {
            String key = (
                    gd.loai == GiaoDich.THU
                            ? "Thu · "
                            : "Chi · "
            ) + gd.danhMuc;

            BigInteger current = sums.containsKey(key)
                    ? sums.get(key)
                    : BigInteger.ZERO;

            sums.put(
                    key,
                    current.add(
                            BigInteger.valueOf(gd.soTien)
                    )
            );
        }

        if (sums.isEmpty()) {
            TextView text = new TextView(this);

            text.setText(
                    "Chưa có giao dịch trong khoảng thời gian này."
            );

            text.setPadding(
                    0,
                    dp(16),
                    0,
                    dp(16)
            );

            groups.addView(text);

            return;
        }

        List<Map.Entry<String, BigInteger>> entries =
                new ArrayList<>(sums.entrySet());

        entries.sort(
                (a, b) -> b.getValue().compareTo(a.getValue())
        );

        for (Map.Entry<String, BigInteger> entry : entries) {
            int color = Color.parseColor(
                    entry.getKey().startsWith("Thu")
                            ? "#0F766E"
                            : "#C65035"
            );

            TextView label = new TextView(this);

            label.setText(
                    entry.getKey()
                            + "\n"
                            + GiaoDich.tien(entry.getValue())
            );

            label.setTextColor(color);
            label.setTextSize(15);

            label.setPadding(
                    0,
                    dp(16),
                    0,
                    dp(8)
            );

            groups.addView(label);

            ProgressBar bar = new ProgressBar(
                    this,
                    null,
                    android.R.attr.progressBarStyleHorizontal
            );

            bar.setMax(1000);

            bar.setProgress(
                    entry.getValue()
                            .multiply(BigInteger.valueOf(1000))
                            .divide(total)
                            .intValue()
            );

            bar.setProgressTintList(
                    ColorStateList.valueOf(color)
            );

            bar.setProgressBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#E2E8ED")
                    )
            );

            groups.addView(
                    bar,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(8)
                    )
            );
        }
    }

    private int dp(int value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putInt(
                "month",
                appliedMonth
        );

        state.putInt(
                "year",
                appliedYear
        );

        super.onSaveInstanceState(state);
    }
}