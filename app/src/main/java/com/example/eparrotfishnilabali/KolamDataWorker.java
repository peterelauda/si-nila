package com.example.eparrotfishnilabali;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class KolamDataWorker extends Worker {

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String userID;

    public KolamDataWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            userID = firebaseAuth.getCurrentUser().getUid();
        } else {
            Log.e("KolamDataWorker", "User tidak terautentikasi");
        }
    }

    @androidx.annotation.NonNull
    @Override
    public Result doWork() {
        // Cek waktu terakhir kali data dikirim
        long lastSyncTime = getLastSyncTime();
        long currentTime = System.currentTimeMillis();
        long syncInterval = 30 * 60 * 1000; // 30 menit dalam milidetik

        // Jika waktu sejak sinkronisasi terakhir kurang dari 30 menit, jangan kirim data
        if (currentTime - lastSyncTime < syncInterval) {
            Log.d("KolamDataWorker", "Sinkronisasi sudah dilakukan dalam 30 menit terakhir, abaikan.");
            return Result.success();
        }

        // Lanjutkan jika belum sinkronisasi
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            firestore.collection("Pengguna")
                    .document(userID)
                    .collection("Kolam")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot kolamDoc : queryDocumentSnapshots.getDocuments()) {
                            String kolamId = kolamDoc.getId();
                            String namaKolam = kolamDoc.getString("nama_kolam");
                            String urlDatabase = kolamDoc.getString("url_database");

                            // Inisialisasi realtimeDatabase untuk kolam ini
                            FirebaseDatabase kolamDatabase = FirebaseDatabase.getInstance(urlDatabase);
                            DatabaseReference kolamRef = kolamDatabase.getReference("Kolam").child(namaKolam);

                            // Ambil data kolam dari realtime Database
                            captureKolamData(kolamRef, kolamId, namaKolam);
                        }
                    });
        }, 30000); // Delay 30 detik

        // Update waktu sinkronisasi terakhir
        saveLastSyncTime(currentTime);

        return Result.success();
    }

    // Metode untuk mengambil waktu terakhir kali data dikirim
    private long getLastSyncTime() {
        // Mengambil waktu sinkronisasi terakhir dari SharedPreferences
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("KolamDataPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getLong("last_sync_time", 0);
    }

    // Metode untuk menyimpan waktu terakhir kali data dikirim
    private void saveLastSyncTime(long time) {
        // Menyimpan waktu sinkronisasi terakhir ke SharedPreferences
        Context context = getApplicationContext();
        SharedPreferences sharedPreferences = context.getSharedPreferences("KolamDataPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("last_sync_time", time);
        editor.apply();
    }

    // Metode untuk mengambil data kolam dari realtime Database
    private void captureKolamData(DatabaseReference kolamRef, String kolamId, String namaKolam) {
        // Menangkap data kolam sekali
        kolamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    KolamModel kolam = dataSnapshot.getValue(KolamModel.class);
                    if (kolam != null) {
                        // Simpan data kolam ke Firestore
                        saveToFirestore(kolam, kolamId, namaKolam);
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError databaseError) {
                Log.e("KolamDataWorker", "Error membaca data kolam", databaseError.toException());
            }
        });
    }

    // Metode untuk menyimpan data ke Firestore
    private void saveToFirestore(KolamModel kolam, String kolamId, String namaKolam) {
        DocumentReference riwayatDoc = firestore.collection("Pengguna")
                .document(userID)
                .collection("Kolam")
                .document(kolamId)
                .collection("Riwayat")
                .document();

        Map<String, Object> riwayatData = new HashMap<>();
        riwayatData.put("ph_air", kolam.getPh_air());
        riwayatData.put("kekeruhan_air", kolam.getKekeruhan_air());
        riwayatData.put("ketinggian_air", kolam.getKetinggian_air());
        riwayatData.put("statusKolam", kolam.getStatus_kolam());
        riwayatData.put("timestamp", FieldValue.serverTimestamp());
        riwayatData.put("switch1", kolam.getSwitch1());
        riwayatData.put("switch2", kolam.getSwitch2());
        riwayatData.put("switch3", kolam.getSwitch3());
        riwayatData.put("switch4", kolam.getSwitch4());
        riwayatData.put("switch5", kolam.getSwitch5());

        riwayatDoc.set(riwayatData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("KolamDataWorker", "Data riwayat disimpan");
                    sendNotification(namaKolam, kolam.getStatus_kolam());
                })
                .addOnFailureListener(e -> Log.e("KolamDataWorker", "Error menyimpan riwayat", e));
    }

    // Mengirim Notifikasi berdasarkan kondisi statusKolam
    private void sendNotification(String namaKolam, String statusKolam) {
        Context context = getApplicationContext();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Buat notification channel jika API 26 atau lebih tinggi
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "kolam_status_channel",
                    "Kolam Status Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel for Kolam Status notifications");
            notificationManager.createNotificationChannel(channel);
        }

        // Hanya kirim notifikasi jika status kolam memenuhi kondisi tertentu
        if (statusKolam.equals("pH terlalu asam") ||
                statusKolam.equals("pH terlalu basa") ||
                statusKolam.equals("Kolam sangat keruh")) {

            // Buat notifikasi dengan informasi nama kolam dan status kolam
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "kolam_status_channel")
                    .setSmallIcon(R.drawable.ic_notification) // Ikon notifikasi
                    .setContentTitle("Nama Kolam: " + namaKolam)
                    .setContentText("Status Kolam: " + statusKolam)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            // Tampilkan notifikasi
            notificationManager.notify(2, builder.build());
        }
    }

}