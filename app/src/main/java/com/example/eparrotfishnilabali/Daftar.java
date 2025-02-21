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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Daftar extends AppCompatActivity {

    // Widgets
    EditText buat_nama, buat_alamat, buat_email, buat_username, buat_password, buat_password1;
    ImageView lihat_pass1, lihat_pass2;
    Button buat_akun;

    // Firebase Auth
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    // UserID
    String userID;

    // Firebase Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daftar);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi Widgets
        buat_akun = findViewById(R.id.button4);
        lihat_pass1 = findViewById(R.id.imageView4);
        lihat_pass2 = findViewById(R.id.imageView5);
        buat_password = findViewById(R.id.editTextTextPassword);
        buat_password1 = findViewById(R.id.editTextTextPassword1);
        buat_nama = findViewById(R.id.editTextText1);
        buat_alamat = findViewById(R.id.editTextText2);
        buat_email = findViewById(R.id.editTextText3);
        buat_username = findViewById(R.id.editTextText4);

        //Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                // Check user already logged in?
                if (currentUser != null){
                    // User already logged in
                } else {
                    // User signed out
                }
            }
        };

        // Fungsi untuk Tombol Lihat Password
        lihat_pass1.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;
            @Override
            public void onClick(View view) {
                if (isPasswordVisible) {
                    // Jika password terlihat, ubah menjadi tersembunyi
                    buat_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    lihat_pass1.setImageResource(R.drawable.visible);
                } else {
                    // Jika password tersembunyi, ubah menjadi terlihat
                    buat_password.setInputType(InputType.TYPE_CLASS_TEXT);
                    lihat_pass1.setImageResource(R.drawable.invisible);
                }
                // Memindahkan kursor ke akhir teks setelah perubahan
                buat_password.setSelection(buat_password.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });
        lihat_pass2.setOnClickListener(new View.OnClickListener() {
            boolean isPasswordVisible = false;
            @Override
            public void onClick(View view) {

                if (isPasswordVisible) {
                    // Jika password terlihat, ubah menjadi tersembunyi
                    buat_password1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    lihat_pass2.setImageResource(R.drawable.visible);
                } else {
                    // Jika password tersembunyi, ubah menjadi terlihat
                    buat_password1.setInputType(InputType.TYPE_CLASS_TEXT);
                    lihat_pass2.setImageResource(R.drawable.invisible);
                }
                // Memindahkan kursor ke akhir teks setelah perubahan
                buat_password1.setSelection(buat_password1.getText().length());
                isPasswordVisible = !isPasswordVisible;
            }
        });

        // Fungsi untuk Tombol Buat Akun
        buat_akun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(buat_nama.getText().toString())
                    && !TextUtils.isEmpty(buat_alamat.getText().toString())
                        && !TextUtils.isEmpty(buat_email.getText().toString())
                            && !TextUtils.isEmpty(buat_username.getText().toString())
                                && !TextUtils.isEmpty(buat_password.getText().toString())){

                    String nama = buat_nama.getText().toString().trim();
                    String alamat = buat_alamat.getText().toString().trim();
                    String email = buat_email.getText().toString().trim();
                    String username = buat_username.getText().toString().trim();
                    String password = buat_password.getText().toString().trim();
                    String konfirmasi = buat_password1.getText().toString().trim();

                    CreateUserAccount(nama, alamat, email, username, password, konfirmasi);
                } else {
                    Toast.makeText(Daftar.this, "Kolom kosong tidak diperbolehkan", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView tombolTutup = findViewById(R.id.imageView1);
        TextView tombolMasuk = findViewById(R.id.textView9);

        tombolTutup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk memulai Activity baru
                Intent intent = new Intent(Daftar.this, MainActivity.class);
                startActivity(intent);
            }
        });

        tombolMasuk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Membuat Intent untuk memulai Activity baru
                Intent intent = new Intent(Daftar.this, Masuk.class);
                startActivity(intent);
            }
        });
    }

    // Fungsi untuk Membuat Akun
    private void CreateUserAccount (
            String nama,
            String alamat,
            String email,
            String username,
            String password,
            String konfirmasi
    ){
        if (!TextUtils.isEmpty(nama)
                && !TextUtils.isEmpty(alamat)
                    && !TextUtils.isEmpty(email)
                        && !TextUtils.isEmpty(username)
                            && !TextUtils.isEmpty(password)
                                && password.equals(konfirmasi)
        ){
            firebaseAuth.createUserWithEmailAndPassword(
                    email, password
            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        // User is created successfully
                        Toast.makeText(Daftar.this, "Akun telah berhasil dibuat", Toast.LENGTH_SHORT).show();
                        userID = firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("nama", buat_nama.getText().toString());
                        user.put("alamat", buat_alamat.getText().toString());
                        user.put("email", buat_email.getText().toString());
                        user.put("username", buat_username.getText().toString());
                        FirebaseFirestore.getInstance().collection("Pengguna").document(userID)
                                .set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        startActivity(new Intent(Daftar.this, MainActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Daftar.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseAuthException) {
                            String errorCode = ((FirebaseAuthException) e).getErrorCode();
                            Toast.makeText(Daftar.this, "Error: " + errorCode, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Daftar.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "Maaf, password dan konfirmasi password tidak sama", Toast.LENGTH_SHORT).show();
        }
    }
}