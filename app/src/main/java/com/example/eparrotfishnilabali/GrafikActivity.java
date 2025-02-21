package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class GrafikActivity extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private String kolamId;
    private String namaKolam;
    private ImageView kembali;
    private TextView nama_kolam;

    private LineChart phChart;  // Grafik untuk pH
    private LineChart kekeruhanChart;  // Grafik untuk kekeruhan
    private Spinner spinnerTimePeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grafik);

        firestore = FirebaseFirestore.getInstance();
        kolamId = getIntent().getStringExtra("idKolam");
        namaKolam = getIntent().getStringExtra("namaKolam");

        kembali = findViewById(R.id.imageView11);
        kembali.setOnClickListener(view -> startActivity(new Intent(GrafikActivity.this, KolamList.class)));

        nama_kolam = findViewById(R.id.eparrotfish_nama_kolam);
        nama_kolam.setText(namaKolam);

        // Inisialisasi LineChart untuk pH dan Kekeruhan
        phChart = findViewById(R.id.phChart);
        kekeruhanChart = findViewById(R.id.kekeruhanChart);
        spinnerTimePeriod = findViewById(R.id.spinnerTimePeriod);

        // Atur Fungsi Spinner
        spinnerTimePeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedPeriod = parentView.getItemAtPosition(position).toString();
                loadGraphData(selectedPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Tidak ada yang dipilih
            }
        });
    }

    // Fungsi untuk Menampilkan Grafik
    private void loadGraphData(String period) {
        CollectionReference riwayatRef = firestore.collection("Pengguna")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Kolam")
                .document(kolamId)
                .collection("Riwayat");

        Query query = riwayatRef.orderBy("timestamp", Query.Direction.ASCENDING);  // Pastikan query diurutkan berdasarkan timestamp

        switch (period) {
            case "Sehari":
                query = riwayatRef.whereGreaterThan("timestamp", getStartOfDay()).orderBy("timestamp", Query.Direction.ASCENDING);
                break;
            case "Sebulan":
                query = riwayatRef.whereGreaterThan("timestamp", getStartOfMonth()).orderBy("timestamp", Query.Direction.ASCENDING);
                break;
            case "Setahun":
                query = riwayatRef.whereGreaterThan("timestamp", getStartOfYear()).orderBy("timestamp", Query.Direction.ASCENDING);
                break;
        }

        // Deklarasi format tanggal dan bulan dengan zona waktu GMT+8
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd MMM", new Locale("id", "ID"));
        dayFormat.setTimeZone(TimeZone.getTimeZone("GMT+8")); // Set timezone ke GMT+8

        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yyyy", new Locale("id", "ID"));
        monthFormat.setTimeZone(TimeZone.getTimeZone("GMT+8")); // Set timezone ke GMT+8

        // Format waktu dalam jam dengan zona waktu GMT+8
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", new Locale("id", "ID"));
        hourFormat.setTimeZone(TimeZone.getTimeZone("GMT+8")); // Set timezone ke GMT+8

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                Log.d("GrafikActivity", "Tidak ada data untuk ditampilkan.");
                return;
            }

            ArrayList<Entry> phEntries = new ArrayList<>();
            ArrayList<Entry> kekeruhanEntries = new ArrayList<>();
            ArrayList<String> xAxisLabels = new ArrayList<>();  // Menyimpan label untuk sumbu X

            // Gunakan LinkedHashMap untuk mempertahankan urutan penambahan data berdasarkan tanggal/bulan
            LinkedHashMap<String, List<Double>> phMap = new LinkedHashMap<>();
            LinkedHashMap<String, List<Double>> kekeruhanMap = new LinkedHashMap<>();

            // Kelompokkan data berdasarkan periode (per hari atau per bulan)
            for (DocumentSnapshot document : queryDocumentSnapshots) {
                Double ph = document.getDouble("ph_air");
                Double kekeruhan = document.getDouble("kekeruhan_air");
                Timestamp timestamp = document.getTimestamp("timestamp");

                if (ph != null && kekeruhan != null && timestamp != null) {
                    String formattedDate;
                    switch (period) {
                        case "Sehari":
                            // Dalam sehari, gunakan waktu sebagai label
                            formattedDate = hourFormat.format(timestamp.toDate()); // Gunakan format jam dengan GMT+8
                            break;

                        case "Sebulan":
                            // Dalam sebulan, gunakan format tanggal (dd MMM)
                            formattedDate = dayFormat.format(timestamp.toDate());
                            break;

                        case "Setahun":
                            // Dalam setahun, gunakan format bulan (MMM yyyy)
                            formattedDate = monthFormat.format(timestamp.toDate());
                            break;

                        default:
                            formattedDate = "";
                            break;
                    }

                    // Tambahkan nilai ke LinkedHashMap yang menjaga urutan
                    if (!phMap.containsKey(formattedDate)) {
                        phMap.put(formattedDate, new ArrayList<>());
                    }
                    phMap.get(formattedDate).add(ph);

                    if (!kekeruhanMap.containsKey(formattedDate)) {
                        kekeruhanMap.put(formattedDate, new ArrayList<>());
                    }
                    kekeruhanMap.get(formattedDate).add(kekeruhan);
                }
            }

            // Hitung rata-rata berdasarkan periode (harian atau bulanan)
            int index = 0;
            for (String date : phMap.keySet()) {
                // Hitung rata-rata pH dan kekeruhan untuk periode tersebut
                List<Double> phList = phMap.get(date);
                List<Double> kekeruhanList = kekeruhanMap.get(date);

                double phAverage = phList.stream().mapToDouble(val -> val).average().orElse(0.0);
                double kekeruhanAverage = kekeruhanList.stream().mapToDouble(val -> val).average().orElse(0.0);

                // Tambahkan rata-rata ke grafik
                phEntries.add(new Entry(index, (float) phAverage));
                kekeruhanEntries.add(new Entry(index, (float) kekeruhanAverage));

                // Tambahkan label ke sumbu X (tanggal atau bulan)
                xAxisLabels.add(date);

                index++;
            }

            // Atur DataSet untuk pH
            LineDataSet phDataSet = new LineDataSet(phEntries, "pH Air");
            phDataSet.setColor(Color.BLUE);
            phDataSet.setLineWidth(2f);

            // Atur DataSet untuk Kekeruhan
            LineDataSet kekeruhanDataSet = new LineDataSet(kekeruhanEntries, "Kekeruhan Air");
            kekeruhanDataSet.setColor(Color.RED);
            kekeruhanDataSet.setLineWidth(2f);

            // Set data ke grafik pH
            LineData phLineData = new LineData(phDataSet);
            phChart.setData(phLineData);

            // Set data ke grafik Kekeruhan
            LineData kekeruhanLineData = new LineData(kekeruhanDataSet);
            kekeruhanChart.setData(kekeruhanLineData);

            // Atur format X-axis untuk grafik pH
            XAxis phXAxis = phChart.getXAxis();
            phXAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
            phXAxis.setLabelRotationAngle(-45);  // Memutar label agar tidak tumpang tindih
            phXAxis.setGranularity(1f);  // Menampilkan setiap label
            phXAxis.setGranularityEnabled(true);
            phChart.invalidate();  // Refresh grafik pH

            // Atur format X-axis untuk grafik Kekeruhan
            XAxis kekeruhanXAxis = kekeruhanChart.getXAxis();
            kekeruhanXAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
            kekeruhanXAxis.setLabelRotationAngle(-45);
            kekeruhanXAxis.setGranularity(1f);
            kekeruhanXAxis.setGranularityEnabled(true);
            kekeruhanChart.invalidate();  // Refresh grafik kekeruhan
        }).addOnFailureListener(e -> Log.e("GrafikActivity", "Error getting data", e));
    }

    // Fungsi untuk mendapatkan awal hari
    private Date getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Fungsi untuk mendapatkan awal bulan
    private Date getStartOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Fungsi untuk mendapatkan awal tahun
    private Date getStartOfYear() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

}

