package com.logger.classes.logs;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

    private final static long DESIRED_LOG_FILE_LENGTH = 1024 * 1024 * 10;
    private final static String FILE_NAME = "results";

    private final BufferedWriter writer;
    public LogWriter(Context context) {
        File file = openOrCreate(context.getApplicationContext());
        writer = createWriter(file);
    }

    private File openOrCreate(Context context) {
        File dir = context.getFilesDir();
        File file = new File(dir, FILE_NAME);

        boolean exists = file.exists();
        boolean tooBig = file.length() > DESIRED_LOG_FILE_LENGTH;
        if (exists && tooBig) {
            String otherName = LogWriter.FILE_NAME + System.currentTimeMillis();
            file.renameTo(new File(dir, otherName));
            file = new File(dir, LogWriter.FILE_NAME);
        }
        return file;
    }
    private BufferedWriter createWriter(File file){
        try {
            return new BufferedWriter(new FileWriter(file, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void write(String message) {
        try {
            if (writer != null) {
                writer.write(message);
                writer.newLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
