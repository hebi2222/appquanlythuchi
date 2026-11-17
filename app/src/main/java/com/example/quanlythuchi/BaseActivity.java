package com.example.quanlythuchi;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public abstract class BaseActivity extends AppCompatActivity {

    protected ThuChiDb db;

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();

    private int readVersion;
    private boolean writing;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(R.layout.activity_base);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View root = findViewById(R.id.root);

        ViewCompat.setOnApplyWindowInsetsListener(
                root,
                (view, insets) -> {
                    Insets padding = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                                    | WindowInsetsCompat.Type.ime()
                    );

                    view.setPadding(
                            padding.left,
                            padding.top,
                            padding.right,
                            padding.bottom
                    );

                    return insets;
                }
        );

        ViewCompat.requestApplyInsets(root);

        WindowCompat.getInsetsController(getWindow(), root)
                .setAppearanceLightStatusBars(true);

        WindowCompat.getInsetsController(getWindow(), root)
                .setAppearanceLightNavigationBars(true);

        db = new ThuChiDb(this);
    }

    protected void screen(
            int layout,
            String title,
            boolean back
    ) {
        FrameLayout host = findViewById(R.id.content);

        getLayoutInflater().inflate(
                layout,
                host,
                true
        );

        setTitle(title);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(back);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    protected abstract void reload();

    @Override
    protected void onResume() {
        super.onResume();
        reload();
    }

    protected <T> void async(
            Callable<T> task,
            Consumer<T> done,
            Runnable failed
    ) {
        worker.execute(() -> {
            try {
                T result = task.call();

                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        done.accept(result);
                    }
                });

            } catch (Exception error) {
                Log.e(
                        "QuanLyThuChi",
                        "Không hoàn thành thao tác",
                        error
                );

                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }

                    if (failed != null) {
                        failed.run();
                    }

                    toast(
                            "Không hoàn thành thao tác. Vui lòng thử lại."
                    );
                });
            }
        });
    }

    protected <T> void loadLatest(
            Callable<T> task,
            Consumer<T> done
    ) {
        int version = ++readVersion;

        async(
                task,
                result -> {
                    if (version == readVersion) {
                        done.accept(result);
                    }
                },
                null
        );
    }

    protected GiaoDichAdapter adapter() {
        return new GiaoDichAdapter(
                new GiaoDichAdapter.Listener() {
                    @Override
                    public void sua(GiaoDich gd) {
                        editor(gd);
                    }

                    @Override
                    public void xoa(GiaoDich gd) {
                        confirmDelete(gd);
                    }
                }
        );
    }

    protected void editor(GiaoDich old) {
        if (writing) {
            return;
        }

        View form = getLayoutInflater().inflate(
                R.layout.dialog_giao_dich,
                null
        );

        EditText title = form.findViewById(R.id.edtTitle);
        EditText amount = form.findViewById(R.id.edtAmount);
        EditText note = form.findViewById(R.id.edtNote);

        RadioGroup type = form.findViewById(R.id.rgType);
        Spinner category = form.findViewById(R.id.spCategory);

        Button date = form.findViewById(R.id.btnDate);
        Button time = form.findViewById(R.id.btnTime);

        Calendar selected = Calendar.getInstance();

        if (old != null) {
            selected.setTimeInMillis(old.thoiGian);
        }

        selected.set(Calendar.SECOND, 0);
        selected.set(Calendar.MILLISECOND, 0);

        Runnable updateTime = () -> {
            date.setText(
                    new SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.US
                    ).format(selected.getTime())
            );

            time.setText(
                    new SimpleDateFormat(
                            "HH:mm",
                            Locale.US
                    ).format(selected.getTime())
            );
        };

        updateTime.run();

        type.setOnCheckedChangeListener(
                (group, checked) -> categories(
                        category,
                        checked == R.id.rbIncome,
                        null
                )
        );

        type.check(
                old != null && old.loai == GiaoDich.THU
                        ? R.id.rbIncome
                        : R.id.rbExpense
        );

        categories(
                category,
                type.getCheckedRadioButtonId() == R.id.rbIncome,
                old == null ? null : old.danhMuc
        );

        if (old != null) {
            title.setText(old.tieuDe);
            amount.setText(String.valueOf(old.soTien));
            note.setText(old.ghiChu);
        }

        date.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        selected.set(year, month, day);
                        updateTime.run();
                    },
                    selected.get(Calendar.YEAR),
                    selected.get(Calendar.MONTH),
                    selected.get(Calendar.DAY_OF_MONTH)
            );

            picker.show();
        });

        time.setOnClickListener(v -> {
            TimePickerDialog picker = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        selected.set(Calendar.HOUR_OF_DAY, hour);
                        selected.set(Calendar.MINUTE, minute);
                        updateTime.run();
                    },
                    selected.get(Calendar.HOUR_OF_DAY),
                    selected.get(Calendar.MINUTE),
                    true
            );

            picker.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(
                        old == null
                                ? "Thêm giao dịch"
                                : "Sửa " + old.ma()
                )
                .setView(form)
                .setNegativeButton("HỦY", null)
                .setPositiveButton("LƯU", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            Button save = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            save.setOnClickListener(v -> {
                if (writing) {
                    return;
                }

                String name = title.getText()
                        .toString()
                        .trim();

                if (name.isEmpty()) {
                    title.setError("Nhập tên giao dịch");
                    title.requestFocus();
                    return;
                }

                long money;

                try {
                    String raw = amount.getText()
                            .toString()
                            .trim();

                    if (!raw.matches("[0-9]{1,12}")) {
                        throw new NumberFormatException();
                    }

                    money = Long.parseLong(raw);

                    if (money <= 0) {
                        throw new NumberFormatException();
                    }

                } catch (NumberFormatException error) {
                    amount.setError(
                            "Nhập số tiền nguyên dương, tối đa 12 chữ số"
                    );

                    amount.requestFocus();
                    return;
                }

                GiaoDich gd = new GiaoDich(
                        old == null ? 0 : old.id,
                        name,
                        money,
                        type.getCheckedRadioButtonId() == R.id.rbIncome
                                ? GiaoDich.THU
                                : GiaoDich.CHI,
                        category.getSelectedItem().toString(),
                        selected.getTimeInMillis(),
                        note.getText().toString().trim()
                );

                mutate(
                        dialog,
                        () -> db.luu(gd),
                        "Đã lưu giao dịch"
                );
            });
        });

        dialog.show();
    }

    private void categories(
            Spinner spinner,
            boolean income,
            String selected
    ) {
        String[] values = income
                ? GiaoDich.NHOM_THU
                : GiaoDich.NHOM_CHI;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                values
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(adapter);

        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(selected)) {
                spinner.setSelection(i);
            }
        }
    }

    private void confirmDelete(GiaoDich gd) {
        if (writing) {
            return;
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Cảnh báo")
                .setMessage(
                        "Bạn có chắc chắn xóa giao dịch này không?\n\n"
                                + gd.ma()
                                + " · "
                                + gd.tieuDe
                                + "\n"
                                + GiaoDich.tien(gd.soTien)
                )
                .setNegativeButton("KHÔNG", null)
                .setPositiveButton("CÓ", null)
                .create();

        dialog.setOnShowListener(ignored -> {
            Button delete = dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            );

            delete.setOnClickListener(v -> mutate(
                    dialog,
                    () -> db.xoa(gd.id),
                    "Đã xóa giao dịch"
            ));
        });

        dialog.show();
    }

    private void lock(
            AlertDialog dialog,
            boolean locked
    ) {
        dialog.setCancelable(!locked);
        dialog.setCanceledOnTouchOutside(!locked);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(!locked);

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setEnabled(!locked);
    }

    private void mutate(
            AlertDialog dialog,
            Callable<Boolean> task,
            String message
    ) {
        if (writing) {
            return;
        }

        writing = true;
        ++readVersion;

        lock(dialog, true);

        async(
                () -> {
                    if (!task.call()) {
                        throw new IllegalStateException(
                                "Bản ghi không tồn tại"
                        );
                    }

                    return true;
                },
                success -> {
                    writing = false;

                    dialog.dismiss();

                    toast(message);
                    reload();
                },
                () -> {
                    writing = false;
                    lock(dialog, false);
                }
        );
    }

    protected void toast(String text) {
        Toast.makeText(
                this,
                text,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            worker.execute(db::close);
        }

        worker.shutdown();

        super.onDestroy();
    }
}