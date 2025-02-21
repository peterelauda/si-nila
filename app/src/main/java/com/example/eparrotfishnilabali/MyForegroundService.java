package com.example.eparrotfishnilabali;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Inisialisasi apa yang ingin dilakukan saat Service dimulai
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SI-NILA Beroperasi")
                .setContentText("Aplikasi sedang berjalan dan memproses data")
                .setSmallIcon(R.drawable.ic_notification)
                .build();

        startForeground(1, notification);

        // Buat WorkRequest untuk menjalankan KolamDataWorker setiap 30 menit
        PeriodicWorkRequest kolamDataRequest =
                new PeriodicWorkRequest.Builder(KolamDataWorker.class, 30, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueue(kolamDataRequest);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}