package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class RiwayatList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RiwayatAdapter riwayatAdapter;
    private List<RiwayatModel> riwayatList;
    private FirebaseFirestore firestore;
    private String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String kolamId;
    private ImageView kembali;

    // Nama Kolam
    TextView namaKolam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_riwayat_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ambil kolamId dari Intent
        Intent intent = getIntent();
        kolamId = intent.getStringExtra("idKolam");

        // Memanggil Riwayat Recycleview
        recyclerView = findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        riwayatList = new ArrayList<>();
        riwayatAdapter = new RiwayatAdapter(this, riwayatList);
        recyclerView.setAdapter(riwayatAdapter);

        // Menampilkan nama kolam
        namaKolam = findViewById(R.id.eparrotfish_nama_kolam);
        namaKolam.setText(intent.getStringExtra("namaKolam"));

        firestore = FirebaseFirestore.getInstance();

        // Tombol Kembali
        kembali = findViewById(R.id.imageView11);
        kembali.setOnClickListener(v -> {
            Intent i = new Intent(RiwayatList.this, KolamList.class);
            startActivity(i);
        });

        // Pop-up Menu
        ImageButton menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(v -> showPopupMenu(v));

        // Muat Riwayat dari Firestore
        loadRiwayatFromFirestore();
    }

    // Show PopupMenu
    private void showPopupMenu(View view) {

        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popupMenu.getMenu());

        // Handle item clicks
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.menu_refresh) {
                // Handle Refresh
                riwayatList.clear();
                riwayatAdapter.notifyDataSetChanged();
                loadRiwayatFromFirestore();
                Toast.makeText(RiwayatList.this, "Riwayat disegarkan", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_clear) {
                // Handle Clear History
                clearRiwayat();
                Toast.makeText(RiwayatList.this, "Riwayat dibersihkan", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    // Clear Riwayat from Firestore
    private void clearRiwayat() {
        firestore.collection("Pengguna")
                .document(userId)
                .collection("Kolam")
                .document(kolamId)
                .collection("Riwayat")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        firestore.collection("Pengguna")
                                .document(userId)
                                .collection("Kolam")
                                .document(kolamId)
                                .collection("Riwayat")
                                .document(document.getId())
                                .delete();
                    }
                    // Clear data in RecyclerView after deletion
                    riwayatList.clear();
                    riwayatAdapter.notifyDataSetChanged();
                });
    }

    // Menampilkan Riwayat dari Firestore
    private void loadRiwayatFromFirestore() {
        firestore.collection("Pengguna")
                .document(userId)
                .collection("Kolam")
                .document(kolamId)
                .collection("Riwayat")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        RiwayatModel riwayat = document.toObject(RiwayatModel.class);
                        riwayatList.add(riwayat);
                    }
                    riwayatAdapter.notifyDataSetChanged();
                });
    }
}