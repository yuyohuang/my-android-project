package com.example.myapplication1210;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * MainActivity 範例：
 *   - 「手動日期」按鈕改成：依照使用者輸入的日期 (yyyy-MM-dd)，
 *     再把菜單上勾選的飲料，逐一插入到資料庫 (order_time=自訂日期)。
 *
 *   - 其餘功能：新增、刪除、顯示、查當日、三日、七日、自訂區間、一次顯示三區間等，
 *     與原本程式相同。
 */
public class MainActivity extends AppCompatActivity {
    int v;
    // ======= 15個 Spinner (飲品) =======
    private Spinner spinner1, spinner2, spinner3, spinner4, spinner5,
            spinner6, spinner7, spinner8, spinner9, spinner10,
            spinner11, spinner12, spinner13, spinner14, spinner15;

    // ======= 功能按鈕 =======
    private Button buttonAdd, buttonDelete, buttonShow; // 新增 / 刪除全部 / 顯示全部
    private Button buttonQuery, button5, button6;       // 查當日 / 查三日 / 查七日
    private TextView textView21;                       // 顯示查詢結果用

    // ======= 自訂區間 (start/end) =======
    private EditText editTextStartDate, editTextEndDate;
    private Button buttonCustomRange;

    // ======= 一次顯示三區間 (當日/三日/七日) =======
    private Button buttonMultiStats;

    // ======= 自訂日期 (核心改動：手動輸入日期 + 將Spinner選擇寫入DB) =======
    private EditText editTextCustomDate;   // 使用者輸入 yyyy-MM-dd
    private Button buttonCustomDate;       // 按下後，依勾選的飲品 + 自訂日期插入資料庫

    // 資料庫
    private MenuDatabaseHelper dbHelper;

    /**
     * 飲料名稱 & 價格：index 0~14 分別對應 15 個 Spinner
     */
    private final String[] drinkNames = {
            "芝芝金萱三Q", "芝芝翡翠綠茶", "芝芝蜜桃紅茶", "芝芝錫蘭奶茶", "芝芝阿華田",
            "芝芝金萱雙Q", "百香雙Q果", "百香綠茶", "百香多多", "翡翠柳橙",
            "金桔檸檬", "檸檬綠茶", "檸檬紅茶", "蜂蜜檸檬", "檸檬多多"
    };

    private final double[] drinkPrices = {
            65.0, 50.0, 60.0, 65.0, 80.0,
            60.0, 55.0, 55.0, 60.0, 60.0,
            45.0, 45.0, 45.0, 55.0, 60.0
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // 連結 XML

        // 初始化資料庫輔助類
        dbHelper = new MenuDatabaseHelper(this);

        // ===== 綁定 UI =====
        // 飲品選擇
        spinner1  = findViewById(R.id.spinner3);
        spinner2  = findViewById(R.id.spinner4);
        spinner3  = findViewById(R.id.spinner5);
        spinner4  = findViewById(R.id.spinner6);
        spinner5  = findViewById(R.id.spinner7);
        spinner6  = findViewById(R.id.spinner8);
        spinner7  = findViewById(R.id.spinner9);
        spinner8  = findViewById(R.id.spinner10);
        spinner9  = findViewById(R.id.spinner11);
        spinner10 = findViewById(R.id.spinner12);
        spinner11 = findViewById(R.id.spinner13);
        spinner12 = findViewById(R.id.spinner14);
        spinner13 = findViewById(R.id.spinner15);
        spinner14 = findViewById(R.id.spinner16);
        spinner15 = findViewById(R.id.spinner17);

        // 功能按鈕
        buttonAdd    = findViewById(R.id.buttonAdd);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonShow   = findViewById(R.id.buttonShow);

        // 查當日、三日、七日
        buttonQuery = findViewById(R.id.buttonQuery);
        button5     = findViewById(R.id.button5);
        button6     = findViewById(R.id.button6);

        textView21 = findViewById(R.id.textView21);

        // 自訂區間
        editTextStartDate = findViewById(R.id.editTextStartDate);
        editTextEndDate   = findViewById(R.id.editTextEndDate);
        buttonCustomRange = findViewById(R.id.buttonCustomRange);

        // 多區間統計
        buttonMultiStats  = findViewById(R.id.buttonMultiStats);

        // === 自訂日期 (輸入 yyyy-MM-dd + 手動日期按鈕) ===
        editTextCustomDate = findViewById(R.id.editTextCustomDate);
        buttonCustomDate   = findViewById(R.id.buttonCustomDate);

        // ====== 設定各按鈕 onClickListener ======

        // 「新增」按鈕 -> 新增餐點 (原本)
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addMenuItem();
                } catch (Exception e) {
                    Log.e("MainActivity", "新增餐點失敗", e);
                    Toast.makeText(MainActivity.this, "新增餐點失敗: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        // 「刪除全部」按鈕
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllMenuItems();
            }
        });

        // 「顯示全部」按鈕
        buttonShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenuItems();
            }
        });

        // 「查當日」
        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSalesSummaryForToday();
            }
        });

        // 「查三日」
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSalesSummaryForLast3Days();
            }
        });

        // 「查七日」
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSalesSummaryForLast7Days();
            }
        });

        // 「自訂區間」
        buttonCustomRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String startDate = editTextStartDate.getText().toString().trim();
                String endDate   = editTextEndDate.getText().toString().trim();
                if (startDate.isEmpty() || endDate.isEmpty()) {
                    Toast.makeText(MainActivity.this, "請輸入起始日期與結束日期", Toast.LENGTH_SHORT).show();
                    return;
                }
                showSalesSummaryInRange(startDate, endDate);
            }
        });

        // 「多區間統計」
        buttonMultiStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAllStatsInOneDialog();
            }
        });

        // ★「手動日期」按鈕 -> 依勾選飲品 + 自訂日期
        buttonCustomDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertCustomDateMenuItem();
            }
        });
    }

    /**
     * (1) 手動日期功能：
     *     將使用者輸入的日期 (yyyy-MM-dd)，
     *     與 15 個 Spinner 選擇的「飲料 + 份數」結合，批量插入到 DB。
     */
    private void insertCustomDateMenuItem() {
        // 1. 讀取使用者輸入之日期
        String dateInput = editTextCustomDate.getText().toString().trim();
        if (dateInput.isEmpty()) {
            Toast.makeText(this, "請輸入日期 (yyyy-MM-dd)", Toast.LENGTH_SHORT).show();
            return;
        }
        // 簡易檢查格式
        if (!dateInput.matches("\\d{4}-\\d{2}-\\d{2}")) {
            Toast.makeText(this, "日期格式需為 yyyy-MM-dd，例如 2025-01-05", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 逐一檢查 15 個 Spinner
        //    若該 Spinner 有 >0 份，就建立一筆 order_time = dateInput
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Spinner[] spinners = {
                spinner1, spinner2, spinner3, spinner4, spinner5,
                spinner6, spinner7, spinner8, spinner9, spinner10,
                spinner11, spinner12, spinner13, spinner14, spinner15
        };

        int totalQuantity = 0;  // 紀錄本次總份數

        for (int i = 0; i < spinners.length; i++) {
            // 取出該 Spinner 的文字，如 "0份", "1份", ...
            String selectedItem = (String) spinners[i].getSelectedItem();
            if (selectedItem != null && selectedItem.endsWith("份")) {
                try {
                    int quantity = Integer.parseInt(selectedItem.replace("份", "").trim());
                    if (quantity > 0) {
                        // 這個飲料勾選了「quantity」份
                        String drinkName = drinkNames[i];
                        double price     = drinkPrices[i];
                        totalQuantity   += quantity;

                        // 建立要插入的 ContentValues
                        ContentValues values = new ContentValues();
                        values.put("name", drinkName);
                        values.put("price", price);
                        values.put("quantity", quantity);

                        // 這裡就用 dateInput 當 order_time
                        values.put("order_time", dateInput);

                        db.insert("menu_items", null, values);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "解析 Spinner 時發生異常", e);
                    Toast.makeText(this, "無法解析選項: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }
        }

        // 檢查本次是否真的有插入
        if (totalQuantity > 0) {
            Toast.makeText(this, "已依自訂日期: " + dateInput
                    + "\n新增總份數: " + totalQuantity, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "請至少選擇一個餐點 (並輸入有效日期)", Toast.LENGTH_SHORT).show();
        }
    }

    // ======== 以下維持原功能 (新增、刪除、顯示、查當日、三日、七日、區間、一次顯示多區間) ========

    /**
     * (2) 新增餐點(原本)
     *     - 預設 order_time = 今日
     */
    private void addMenuItem() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Spinner[] spinners = {
                spinner1, spinner2, spinner3, spinner4, spinner5,
                spinner6, spinner7, spinner8, spinner9, spinner10,
                spinner11, spinner12, spinner13, spinner14, spinner15
        };

        int totalQuantity = 0;
        for (int i = 0; i < spinners.length; i++) {
            String selectedItem = (String) spinners[i].getSelectedItem();
            if (selectedItem != null && selectedItem.endsWith("份")) {
                try {
                    int quantity = Integer.parseInt(selectedItem.replace("份", "").trim());
                    if (quantity > 0) {
                        String drinkName = drinkNames[i];
                        double price     = drinkPrices[i];
                        totalQuantity   += quantity;

                        ContentValues values = new ContentValues();
                        values.put("name", drinkName);
                        values.put("price", price);
                        values.put("quantity", quantity);

                        // 原先寫法：order_time = 今日日期
                        String now = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                        values.put("order_time", now);

                        db.insert("menu_items", null, values);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "解析 Spinner 時發生異常", e);
                    Toast.makeText(this, "無法解析選項: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (totalQuantity > 0) {
            Toast.makeText(this, "餐點已新增！總份數: " + totalQuantity, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "請至少選擇一個餐點", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * (3) 刪除全部
     */
    private void deleteAllMenuItems() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("DELETE FROM menu_items");
        Toast.makeText(this, "所有餐點已清空！", Toast.LENGTH_SHORT).show();
    }

    /**
     * (4) 顯示全部
     *     - 包含 order_time
     *     - 計算總金額
     */
    private void showMenuItems() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, price, quantity, order_time FROM menu_items", null);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder sb = new StringBuilder();
            double totalPrice = 0.0;

            do {
                String name  = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                String otime = cursor.getString(cursor.getColumnIndexOrThrow("order_time"));

                double itemTotal = price * quantity;
                totalPrice += itemTotal;

                sb.append("餐點名稱: ").append(name)
                        .append(", 價格: $").append(price)
                        .append(", 數量: ").append(quantity)
                        .append(", 小計: $").append(itemTotal)
                        .append(", 時間: ").append(otime)
                        .append("\n");

            } while (cursor.moveToNext());

            cursor.close();
            sb.append("\n總金額: $").append(totalPrice);

            new AlertDialog.Builder(this)
                    .setTitle("餐點清單")
                    .setMessage(sb.toString())
                    .setPositiveButton("確定", null)
                    .show();
        } else {
            Toast.makeText(this, "目前沒有餐點資料", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * (5) 查當日
     */
    private void showSalesSummaryForToday() {
        String sql = "SELECT SUM(quantity) AS total_qty, SUM(price * quantity) AS total_revenue "
                + "FROM menu_items "
                + "WHERE date(order_time) = date('now','localtime')";
        showSalesDialogFromSQL("當日營業資訊", sql);
    }

    /**
     * (6) 查最近三日
     */
    private void showSalesSummaryForLast3Days() {
        String sql = "SELECT SUM(quantity) AS total_qty, SUM(price * quantity) AS total_revenue "
                + "FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-2 day')";
        showSalesDialogFromSQL("最近 3 天營業資訊", sql);
    }

    /**
     * (7) 查最近七日
     */
    private void showSalesSummaryForLast7Days() {
        String sql = "SELECT SUM(quantity) AS total_qty, SUM(price * quantity) AS total_revenue "
                + "FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-6 day')";
        showSalesDialogFromSQL("最近 7 天營業資訊", sql);
    }

    /**
     * (8) 一次顯示：當日 / 三日 / 七日
     */
    private void showAllStatsInOneDialog() {
        int todayQty        = getQtyForDays(1);
        double todayRevenue = getRevenueForDays(1);

        int threeDayQty        = getQtyForDays(3);
        double threeDayRevenue = getRevenueForDays(3);

        int sevenDayQty        = getQtyForDays(7);
        double sevenDayRevenue = getRevenueForDays(7);

        String message =
                "【當日】\n"
                        + "總品項數量: " + todayQty + "\n"
                        + "總營業額: $" + todayRevenue + "\n\n"
                        + "【三日】\n"
                        + "總品項數量: " + threeDayQty + "\n"
                        + "總營業額: $" + threeDayRevenue + "\n\n"
                        + "【七日】\n"
                        + "總品項數量: " + sevenDayQty + "\n"
                        + "總營業額: $" + sevenDayRevenue;

        new AlertDialog.Builder(this)
                .setTitle("多區間營業資訊")
                .setMessage(message)
                .setPositiveButton("確定", null)
                .show();
    }

    /**
     * (9) 計算「最近 days 天」的總品項數量
     */
    private int getQtyForDays(int days) {
        int totalQty = 0;
        String sql = "SELECT SUM(quantity) AS total_qty FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-" + (days - 1) + " day')";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        if (c != null) {
            if (c.moveToFirst()) {
                totalQty = c.getInt(c.getColumnIndexOrThrow("total_qty"));
            }
            c.close();
        }
        return totalQty;
    }

    /**
     * (10) 計算「最近 days 天」的營業額
     */
    private double getRevenueForDays(int days) {
        double totalRev = 0.0;
        String sql = "SELECT SUM(price * quantity) AS total_revenue FROM menu_items "
                + "WHERE date(order_time) >= date('now','localtime','-" + (days - 1) + " day')";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(sql, null);
        if (c != null) {
            if (c.moveToFirst()) {
                totalRev = c.getDouble(c.getColumnIndexOrThrow("total_revenue"));
            }
            c.close();
        }
        return totalRev;
    }

    /**
     * (11) 通用 SQL 查詢對話框
     */
    private void showSalesDialogFromSQL(String title, String sql) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        int totalQty = 0;
        double totalRevenue = 0.0;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                totalQty     = cursor.getInt(cursor.getColumnIndexOrThrow("total_qty"));
                totalRevenue = cursor.getDouble(cursor.getColumnIndexOrThrow("total_revenue"));
            }
            cursor.close();
        }

        String msg = "總品項數量: " + totalQty + "\n"
                + "總營業額: $" + totalRevenue;

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("確定", null)
                .show();
    }

    /**
     * (12) 區間查詢
     */
    private void showSalesSummaryInRange(String startDate, String endDate) {
        String sql = "SELECT SUM(quantity) AS total_qty, SUM(price * quantity) AS total_revenue "
                + "FROM menu_items "
                + "WHERE date(order_time) BETWEEN date('" + startDate + "') AND date('" + endDate + "')";

        showSalesDialogFromSQL("區間營業資訊\n(" + startDate + " ~ " + endDate + ")", sql);
    }
}
