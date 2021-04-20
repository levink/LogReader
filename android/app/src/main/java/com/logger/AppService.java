package com.logger;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppService extends JobIntentService {

    public static final String URL_CHECK_BROADCAST = "app-service-url-check";
    public static final String FILE_PARSE_BROADCAST = "app-service-file-parse";
    private static final int JOB_ID = 1000;

    public static class Tag {
        public static final String WORK = "work";
        public static final String URL = "url";
        public static final String MASK = "url";
        public static final String IS_AVAILABLE = "is_available";
        public static final String PROGRESS = "progress";
    }

    private enum Work {
        Unknown,
        CheckUrl,
        ParseFile;

        static Work parse(int value) {
            if (value < 0 || value >= values().length) {
                return Unknown;
            }
            return values()[value];
        }
        static Work parse(Intent intent) {
            int value = intent.getIntExtra(Tag.WORK, Unknown.ordinal());
            return parse(value);
        }
    }

    public static void checkUrl(Context context, String url) {
        Intent work = new Intent();
        work.putExtra(Tag.WORK, Work.CheckUrl.ordinal());
        work.putExtra(Tag.URL, url);
        enqueueWork(context, AppService.class, JOB_ID, work);
    }
    static void parseFile(Context context, String url, String mask) {
        Intent work = new Intent();
        work.putExtra(Tag.WORK, Work.ParseFile.ordinal());
        work.putExtra(Tag.URL, url);
        work.putExtra(Tag.MASK, mask);
        enqueueWork(context, AppService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Work work = Work.parse(intent);
        switch (work) {
            case CheckUrl: checkUrl(intent); break;
            case ParseFile: parseFile(intent); break;
        }
    }

    private void send(Intent intent){
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void checkUrl(Intent intent) {
        String url = intent.getStringExtra(Tag.URL);
        boolean available = checkUrl(url);
        Intent result = new Intent(URL_CHECK_BROADCAST);
        result.putExtra(Tag.URL, url);
        result.putExtra(Tag.IS_AVAILABLE, available);
        send(result);
    }
    private boolean checkUrl(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                return false;
            }

            int length = connection.getContentLength();
            return length > 0;

        } catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (connection != null)
                connection.disconnect();
        }
        return false;
    }

    @FunctionalInterface
    interface ProgressListener{
        void onProgress(int progress);
    }
    private void parseFile(Intent intent) {
        String url = intent.getStringExtra(Tag.URL);
        String mask = intent.getStringExtra(Tag.MASK);

        parseFile(url, mask, progress -> {
            Intent progressIntent = new Intent(FILE_PARSE_BROADCAST);
            progressIntent.putExtra(Tag.PROGRESS, progress);
            send(progressIntent);
        });

        Intent result = new Intent(FILE_PARSE_BROADCAST);
        send(result);
    }
    private void parseFile(String url, String mask, ProgressListener listener) {
        listener.onProgress(10);
        listener.onProgress(50);
        listener.onProgress(100);
    }
}