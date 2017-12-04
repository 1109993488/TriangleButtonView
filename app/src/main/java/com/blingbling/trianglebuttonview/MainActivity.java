package com.blingbling.trianglebuttonview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final String[] button = {"品牌", "价格", "车型", "排量", "其他"};
    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TriangleButtonView tbv = findViewById(R.id.tbv);

//        tbv.setButton(button);
        tbv.setOnItemClickListener(new TriangleButtonView.OnItemClickListener() {
            @Override
            public void onItemClick(TriangleButtonView view, int index) {
                tbv.setSelected(index);
                showToast("你点击了:" + button[index]);
            }
        });
    }

    private void showToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }
}
