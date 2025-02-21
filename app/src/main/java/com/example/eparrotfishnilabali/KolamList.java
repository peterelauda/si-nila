package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class KolamList extends AppCompatActivity {
    TextView nama, alamat;
    Button tombolKeluar;
    ImageButton refreshbtn;

    // Mendapatkan User
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;
    private String userID;

    // Firebase Firestore
    private FirebaseFirestore db;
    private CollectionReference collectionReference;

    // List Kolam
    private List<KolamModel> kolamList;

    // RecyclerView
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    // Realtime Database
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_kolam_list);

        // Inisialisasi Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        // Periksa apakah user sudah terautentikasi
        if (user != null) {
            userID = user.getUid();
        } else {
            // Tangani kasus ketika user tidak terautentikasi
            Log.d("KolamList", "User tidak terautentikasi");
            startActivity(new Intent(KolamList.this, MainActivity.class));
            finish();
            return; // Menghentikan eksekusi lebih lanjut jika user tidak ada
        }

        // Inisialisasi Firebase Firestore
        db = FirebaseFirestore.getInstance();
        collectionReference = db.collection("Pengguna").document(userID).collection("Kolam");

        // Widgets
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Segarkan atau Sinkronisasi
        refreshbtn = findViewById(R.id.refresh);
        refreshbtn.setOnClickListener(v -> onStart());

        // Keluar Akun
        tombolKeluar = findViewById(R.id.button5);
        tombolKeluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(KolamList.this, MainActivity.class));
                finish();
            }
        });

        // Inisialisasi List dan Adapter
        kolamList = new ArrayList<>();
        myAdapter = new MyAdapter(this, kolamList);
        recyclerView.setAdapter(myAdapter);

        // Pastikan kolamList sudah terisi sebelum mengaksesnya
        if (kolamList != null && kolamList.size() > 0) {
            databaseReference = FirebaseDatabase.getInstance(kolamList.get(0).getUrl_database()) // Pastikan URL valid
                    .getReference("Kolam")
                    .child(kolamList.get(0).getNama_kolam());

            // Memantau perubahan data di realtime database
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Kolam list harus di-clear sebelum mengisi data baru
                    kolamList.clear();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        KolamModel kolam = dataSnapshot.getValue(KolamModel.class);
                        // Pastikan kolam tidak null sebelum menambahkannya ke list
                        if (kolam != null) {
                            kolamList.add(kolam);
                        }
                    }

                    // Pastikan adapter diberitahu setelah perubahan data
                    myAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("KolamList", "Error fetching data", error.toException());
                }
            });
        } else {
            Log.e("KolamList", "kolamList is empty or null");
        }

        // Menampilkan Nama dan Alamat Pengguna
        db.collection("Pengguna").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String namaPengguna = document.getString("nama");
                        String alamatPengguna = document.getString("alamat");
                        nama = findViewById(R.id.eparrotfish_nama_list);
                        alamat = findViewById(R.id.eparrotfish_alamat_list);
                        nama.setText(namaPengguna);
                        alamat.setText(alamatPengguna);
                    } else {
                        Log.d("Firestore", "No such document");
                    }
                } else {
                    Log.d("Firestore", "Error getting document", task.getException());
                }
            }
        });

        // Tombol Floating Action Button
        FloatingActionButton fabButton = findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk memulai Activity baru
                Intent intent = new Intent(KolamList.this, TambahKolam.class);
                startActivity(intent);
            }
        });

        // Mulai foreground service
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Mengambil data dari Firestore dan menampilkan di RecyclerView
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                kolamList.clear();
                for (QueryDocumentSnapshot kolams : queryDocumentSnapshots) {
                    KolamModel kolam = kolams.toObject(KolamModel.class);
                    // Mengambil ID Kolam
                    kolam.setIdKolam(kolams.getId());
                    kolamList.add(kolam);
                }
                // Inisialisasi Adapter dan set ke RecyclerView
                myAdapter = new MyAdapter(KolamList.this, kolamList);
                recyclerView.setAdapter(myAdapter);
                myAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(KolamList.this, "Mohon maaf, terjadi kesalahan", Toast.LENGTH_SHORT).show();
            }
        });
    }
}