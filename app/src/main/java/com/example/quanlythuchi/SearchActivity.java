package com.example.quanlythuchi;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class SearchActivity extends BaseActivity {

    private EditText code;
    private Spinner type;

    private TextView result;
    private TextView empty;

    private GiaoDichAdapter adapter;

    private Integer appliedType;
    private Long appliedId;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        screen(
                R.layout.activity_search,
                "Tìm giao dịch",
                true
        );

        code = findViewById(R.id.edtCode);
        type = findViewById(R.id.spType);
        result = findViewById(R.id.tvResult);
        empty = findViewById(R.id.tvEmpty);

        ArrayAdapter<String> types = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{
                        "Tất cả",
                        "Khoản thu",
                        "Khoản chi"
                }
        );

        types.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        type.setAdapter(types);

        adapter = adapter();

        RecyclerView list = findViewById(R.id.rvTransactions);

        list.setLayoutManager(
                new LinearLayoutManager(this)
        );

        list.setAdapter(adapter);

        if (state != null) {
            if (state.containsKey("filterType")) {
                appliedType = state.getInt("filterType");
            }

            if (state.containsKey("filterId")) {
                appliedId = state.getLong("filterId");
            }
        }

        findViewById(R.id.btnFind).setOnClickListener(
                v -> applySearch()
        );
    }

    private void applySearch() {
        String raw = code.getText()
                .toString()
                .trim()
                .toUpperCase(Locale.ROOT);

        Long id = null;

        if (!raw.isEmpty()) {
            try {
                if (!raw.matches("(?:GD)?[0-9]{1,19}")) {
                    throw new NumberFormatException();
                }

                id = Long.parseLong(
                        raw.startsWith("GD")
                                ? raw.substring(2)
                                : raw
                );

                if (id <= 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException error) {
                code.setError(
                        "Nhập mã như GD000001 hoặc 1"
                );

                return;
            }
        }

        int selection = type.getSelectedItemPosition();

        appliedType = selection == 0
                ? null
                : (selection == 1 ? GiaoDich.THU : GiaoDich.CHI);

        appliedId = id;

        reload();
    }

    @Override
    protected void reload() {
        Integer filterType = appliedType;
        Long filterId = appliedId;

        loadLatest(
                () -> db.layDanhSach(
                        filterType,
                        filterId,
                        null,
                        null
                ),
                data -> {
                    adapter.setData(data);

                    result.setText(
                            "Tìm thấy "
                                    + data.size()
                                    + " giao dịch"
                    );

                    empty.setVisibility(
                            data.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );
                }
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        if (appliedType != null) {
            state.putInt(
                    "filterType",
                    appliedType
            );
        }

        if (appliedId != null) {
            state.putLong(
                    "filterId",
                    appliedId
            );
        }

        super.onSaveInstanceState(state);
    }
}