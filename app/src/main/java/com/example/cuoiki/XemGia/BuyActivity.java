package com.example.cuoiki.XemGia;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cuoiki.TienIch.SQLiteHelper;
import com.example.cuoiki.R;

import java.text.DecimalFormat;

public class BuyActivity extends AppCompatActivity {

    private SQLiteHelper dbHelper;
    private TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        dbHelper = new SQLiteHelper(this);
        txtResult = findViewById(R.id.txtResult);

        displayProductPrices();
    }

    private void displayProductPrices() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name, price, init, quantity FROM product", null);

        StringBuilder builder = new StringBuilder();
        DecimalFormat df = new DecimalFormat("#,### VNĐ");
        double grandTotal = 0;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                String init = cursor.getString(cursor.getColumnIndexOrThrow("init"));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
                double total = price * quantity;
                grandTotal += total;

                //  Format sản phẩm, tách rõ từng khối
                builder.append("<div style='margin-bottom:12px;'>")
                        .append("<b><font color='#FFD700'>Tên sản phẩm:</font></b> <font color='#FFFFFF'>")
                        .append(name).append("</font><br>")
                        .append("<font color='#32CD32'>Đơn vị:</font> ").append(init).append("<br>")
                        .append("<font color='#00BFFF'>Số lượng:</font> ").append(quantity).append("<br>")
                        .append("<font color='#FF69B4'>Đơn giá:</font> ").append(df.format(price)).append("<br>")
                        .append("<font color='#FFA500'>Tổng:</font> ").append(df.format(total))
                        .append("</div>")
                        .append("<hr color='#777777'/>"); // dòng ngăn cách mỏng
            } while (cursor.moveToNext());
            cursor.close();
        }

        //  Dòng tổng tách riêng, chữ to rõ
        builder.append("<br><b><font color='#00FF7F' size='6'>TỔNG TẤT CẢ:</font></b><br>")
                .append("<font color='#FFFFFF' size='5'><b>")
                .append(df.format(grandTotal))
                .append("</b></font>");

        txtResult.setText(Html.fromHtml(builder.toString(), Html.FROM_HTML_MODE_LEGACY));
        db.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) dbHelper.close();
    }
}
