package com.unpi.madres.view;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import com.unpi.madres.R;

public class FamilyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button btnFamilySearch = findViewById(R.id.btnSearchFamily);
        Button btnFamilyList = findViewById(R.id.btnListFamily);

        btnFamilySearch.setOnClickListener(v ->
                startActivity(new Intent(FamilyActivity.this, SearchFamilyActivity.class)));

        btnFamilyList.setOnClickListener(v ->
                startActivity(new Intent(FamilyActivity.this, FamilyCircleActivity.class)));
    }
}
