package com.novoda.downloadmanager.demo.simple;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.facebook.stetho.Stetho;
import com.novoda.downloadmanager.AllBatchStatusesCallback;
import com.novoda.downloadmanager.Batch;
import com.novoda.downloadmanager.DownloadBatchId;
import com.novoda.downloadmanager.DownloadBatchIdCreator;
import com.novoda.downloadmanager.DownloadBatchStatus;
import com.novoda.downloadmanager.DownloadManagerBuilder;
import com.novoda.downloadmanager.LiteDownloadManagerCommands;
import com.novoda.downloadmanager.demo.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String BIG_FILE = "http://ipv4.download.thinkbroadband.com/200MB.zip";
    //    private static final String PENGUINS_IMAGE = "http://i.imgur.com/Y7pMO5Kb.jpg";
    private static final DownloadBatchId BEARD_ID = DownloadBatchIdCreator.createFrom("beard_id");

    private LiteDownloadManagerCommands downloadManagerCommands;
    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);
        setContentView(R.layout.activity_main);
        emptyView = findViewById(R.id.main_no_downloads_view);
        recyclerView = findViewById(R.id.main_downloads_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Handler handler = new Handler(Looper.getMainLooper());
        downloadManagerCommands = DownloadManagerBuilder
                .newInstance(this, handler, R.mipmap.ic_launcher)
                .build();

        setupDownloadingExample();
        setupQueryingExample();
    }

    private void logV1Database() {
        if (checkV1DatabaseExists()) {
            File dbFile = this.getDatabasePath("downloads.db");

            SQLiteDatabase database = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, 0);

            Cursor cursor = database.rawQuery("SELECT * FROM Downloads", null);
            cursor.moveToFirst();

            Log.d("MainActivity", cursor.getString(cursor.getColumnIndex("_data")));

            cursor.close();
            database.close();

            FileOutputStream file = openFileOutput(filePath.path(), Context.MODE_APPEND);
            
        } else {
            Log.d("MainActivity", "downloads.db doesn't exist!");
        }
    }

    private boolean checkV1DatabaseExists() {
        File dbFile = this.getDatabasePath("downloads.db");
        return dbFile.exists();
    }

    private void setupDownloadingExample() {
        findViewById(R.id.main_download_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        final Batch batch = new Batch.Builder(BEARD_ID, "Family of Penguins")
                                .addFile(BIG_FILE)
                                .build();
                        downloadManagerCommands.download(batch);
                    }
                });
    }

    private void setupQueryingExample() {
        queryForDownloads();
        findViewById(R.id.main_refresh_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View v) {
                        queryForDownloads();
                        logV1Database();
                    }
                }
        );
    }

    private void queryForDownloads() {
        downloadManagerCommands.getAllDownloadBatchStatuses(new AllBatchStatusesCallback() {
            @Override
            public void onReceived(List<DownloadBatchStatus> downloadBatchStatuses) {
                List<BeardDownload> beardDownloads = new ArrayList<>(downloadBatchStatuses.size());
                for (DownloadBatchStatus downloadBatchStatus : downloadBatchStatuses) {
                    BeardDownload beardDownload = new BeardDownload(downloadBatchStatus.getDownloadBatchTitle(), downloadBatchStatus.status());
                    beardDownloads.add(beardDownload);
                }
                onQueryResult(beardDownloads);
            }
        });
    }

    public void onQueryResult(List<BeardDownload> beardDownloads) {
        recyclerView.setAdapter(new BeardDownloadAdapter(beardDownloads));
        emptyView.setVisibility(beardDownloads.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
