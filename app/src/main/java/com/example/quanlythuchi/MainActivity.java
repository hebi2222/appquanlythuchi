package com.example.quanlythuchi;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends BaseActivity {

    private GiaoDichAdapter adapter;

    private TextView balance;
    private TextView income;
    private TextView expense;
    private TextView count;
    private TextView empty;

    private final ActivityResultLauncher<String> exportFile =
            registerForActivityResult(
                    new ActivityResultContracts.CreateDocument(
                            "application/json"
                    ),
                    uri -> {
                        if (uri == null) {
                            return;
                        }

                        async(
                                () -> {
                                    JSONArray rows = new JSONArray();

                                    for (GiaoDich gd : db.tatCa()) {
                                        JSONObject row = new JSONObject();

                                        row.put("id", gd.id);
                                        row.put("ma", gd.ma());
                                        row.put("tieu_de", gd.tieuDe);
                                        row.put("so_tien_vnd", gd.soTien);

                                        row.put(
                                                "loai",
                                                gd.loai == GiaoDich.THU
                                                        ? "THU"
                                                        : "CHI"
                                        );

                                        row.put("danh_muc", gd.danhMuc);

                                        row.put(
                                                "thoi_gian_epoch_ms",
                                                gd.thoiGian
                                        );

                                        row.put("ghi_chu", gd.ghiChu);

                                        rows.put(row);
                                    }

                                    JSONObject data = new JSONObject();

                                    data.put("schema_version", 1);
                                    data.put("giao_dich", rows);

                                    try (OutputStream out =
                                                 getContentResolver()
                                                         .openOutputStream(
                                                                 uri,
                                                                 "wt"
                                                         )) {
                                        if (out == null) {
                                            throw new IllegalStateException(
                                                    "Không mở được file"
                                            );
                                        }

                                        out.write(
                                                data.toString(2).getBytes(
                                                        StandardCharsets.UTF_8
                                                )
                                        );
                                    }

                                    return rows.length();
                                },
                                size -> toast(
                                        "Đã xuất "
                                                + size
                                                + " giao dịch ra JSON"
                                ),
                                null
                        );
                    }
            );

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        screen(
                R.layout.activity_main,
                "Sổ thu chi",
                false
        );

        balance = findViewById(R.id.tvBalance);
        income = findViewById(R.id.tvIncome);
        expense = findViewById(R.id.tvExpense);
        count = findViewById(R.id.tvCount);
        empty = findViewById(R.id.tvEmpty);

        adapter = adapter();

        RecyclerView list = findViewById(R.id.rvTransactions);

        list.setLayoutManager(
                new LinearLayoutManager(this)
        );

        list.setAdapter(adapter);

        findViewById(R.id.btnAdd).setOnClickListener(
                v -> editor(null)
        );

        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    SearchActivity.class
            );

            startActivity(intent);
        });

        findViewById(R.id.btnStats).setOnClickListener(v -> {
            Intent intent = new Intent(
                    this,
                    StatsActivity.class
            );

            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        confirmExit();
                    }
                }
        );
    }

    @Override
    protected void reload() {
        loadLatest(
                db::tatCa,
                this::showData
        );
    }

    private void showData(List<GiaoDich> data) {
        adapter.setData(data);

        BigInteger thu = GiaoDich.tong(
                data,
                GiaoDich.THU
        );

        BigInteger chi = GiaoDich.tong(
                data,
                GiaoDich.CHI
        );

        balance.setText(
                GiaoDich.tien(thu.subtract(chi))
        );

        income.setText(
                GiaoDich.tien(thu)
        );

        expense.setText(
                GiaoDich.tien(chi)
        );

        count.setText(
                "Lịch sử giao dịch · " + data.size()
        );

        empty.setVisibility(
                data.isEmpty() ? View.VISIBLE : View.GONE
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(
                0,
                1,
                0,
                "Xuất dữ liệu JSON"
        );

        menu.add(
                0,
                2,
                1,
                "Thoát chương trình"
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            exportFile.launch(
                    "quanlythuchi-data.json"
            );

            return true;
        }

        if (item.getItemId() == 2) {
            confirmExit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmExit() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát ứng dụng?")
                .setMessage(
                        "Các giao dịch đã lưu vẫn được giữ lại."
                )
                .setNegativeButton("Ở LẠI", null)
                .setPositiveButton(
                        "THOÁT",
                        (dialog, which) -> finishAffinity()
                )
                .show();
    }
}