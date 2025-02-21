package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, KolamList.class));
        } else {
            // Menambah Fungsi untuk Tombol
            Button tombolInstruksi = findViewById(R.id.button);
            Button tombolDaftar = findViewById(R.id.button1);
            Button tombolMasuk = findViewById(R.id.button2);

            tombolInstruksi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Membuat Intent untuk memulai Activity baru
                    Intent intent = new Intent(MainActivity.this, Instruksi.class);
                    startActivity(intent);
                }
            });

            tombolDaftar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Membuat Intent untuk memulai Activity baru
                    Intent intent = new Intent(MainActivity.this, Daftar.class);
                    startActivity(intent);
                }
            });

            tombolMasuk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Membuat Intent untuk memulai Activity baru
                    Intent intent = new Intent(MainActivity.this, Masuk.class);
                    startActivity(intent);
                }
            });
        }
    }

}