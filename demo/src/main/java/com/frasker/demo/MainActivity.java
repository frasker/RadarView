package com.frasker.demo;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.frasker.radarview.RadarData;
import com.frasker.radarview.RadarView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RadarView radarView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        radarView = findViewById(R.id.radarView);
        button = findViewById(R.id.btn);
        List<RadarData> radarDatas = new ArrayList<>();
        radarDatas.add(new RadarData("社区活跃度", 0.8));
        radarDatas.add(new RadarData("社区影响力", 0.6));
        radarDatas.add(new RadarData("社区共享度", 0.4));
        radarDatas.add(new RadarData("社区积极性", 0.9));
        radarDatas.add(new RadarData("社区贡献度", 0.2));
        radarView.setDataList(radarDatas);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                radarView.playAnimation();
            }
        });


        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("push://com.baidu.android.push/notification?msg=haha"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String intentUri = intent.toUri(Intent.URI_INTENT_SCHEME);
        Log.i("intentUri", intentUri);
    }
}
