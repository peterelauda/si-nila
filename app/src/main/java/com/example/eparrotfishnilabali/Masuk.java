package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Masuk extends AppCompatActivity {

    private EditText tulis_email, tulis_password;
    Button tombolMasuk;
    ImageView lihat_pass3;

    // Firebase Auth
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_masuk);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView tombolTutup = findViewById(R.id.imageView1);
        TextView tombolDaftar = findViewById(R.id.textView9);

        tombolMasuk = findViewById(R.id.button4);
        tulis_email = findViewById(R.id.editTextText);
        tulis_password = findViewById(R.id.editTextTextPassword);
        lihat_pass3 = findViewById(R.id.imageView4);

        lihat_pass3.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;

            @Override
            public void onClick(View view) {

                if (isPasswordVisible) {
                    // Jika password terlihat, ubah menjadi tersembunyi
                    tulis_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    lihat_pass3.setImageResource(R.drawable.visible);
                } else {
                    // Jika password tersembunyi, ubah menjadi terlihat
                    tulis_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    lihat_pass3.setImageResource(R.drawable.invisible);
                }
                // Memindahkan kursor ke akhir teks setelah perubahan
                tulis_password.setSelection(tulis_password.getText().length());
                isPasswordVisible = !isPasswordVisible;

            }
        });

        // Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();
        tombolMasuk.setOnClickListener(v->{
            logEmailPassUser(
                    tulis_email.getText().toString().trim(),
                    tulis_password.getText().toString().trim()
            );
        });
        
        // Menambah Fungsi pada Tombol
        tombolTutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk memulai Activity baru
                Intent intent = new Intent(Masuk.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tombolDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk memulai Activity baru
                Intent intent = new Intent(Masuk.this, Daftar.class);
                startActivity(intent);
            }
        });
    }

    // Fungsi untuk Masuk
    private void logEmailPassUser(
            String email, String password
    ){
        // Mengecek kolom kosong
        if (!TextUtils.isEmpty(email)
            && !TextUtils.isEmpty(password)
        ){
            firebaseAuth.signInWithEmailAndPassword(
                    email,
                    password
            ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();

                    Intent i = new Intent(Masuk.this, KolamList.class);
                    startActivity(i);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(Masuk.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}