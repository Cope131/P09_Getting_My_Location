package com.myapplicationdev.android.p09gettingmylocation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class RecordsActivity extends AppCompatActivity {

    private final String DEBUG_TAG = RecordsActivity.class.getSimpleName();
    private File recordFile;

    // List View Components
    private ListView lv;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> locations;

    // Views
    private TextView recordsTV;
    private Button refereshBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        initViews();
        initLVComp();
        getFilePath();
        read();
    }

    private void initViews() {
        recordsTV = findViewById(R.id.number_of_records_text_view);
        refereshBtn = findViewById(R.id.refresh_button);
        refereshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                read();
            }
        });
    }

    private void initLVComp() {
        lv = findViewById(R.id.list_view);
        locations = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(RecordsActivity.this, android.R.layout.simple_list_item_1, locations);
        lv.setAdapter(arrayAdapter);
    }

    private void getFilePath() {
        String fileName = getIntent().getStringExtra("fileName");
        String folderPath = getIntent().getStringExtra("folderPath");
        recordFile = new File(folderPath, fileName);
    }

    private void read() {
        if (recordFile != null && recordFile.exists()) {
            StringBuilder sb = new StringBuilder();
            try {
                FileReader reader = new FileReader(recordFile);
                BufferedReader br = new BufferedReader(reader);
                locations.clear();
                String line = br.readLine();
                while (line != null) {
                    sb.append(line + "\n");
                    if (line != null) {
                        locations.add(line);
                    }
                    line = br.readLine();
                }
                br.close();
                reader.close();
                Log.d(DEBUG_TAG, "records.txt Content: \n" + sb.toString());
                Log.d(DEBUG_TAG, locations.toString());

                arrayAdapter.notifyDataSetChanged();
                recordsTV.setText(locations.size() + "");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}