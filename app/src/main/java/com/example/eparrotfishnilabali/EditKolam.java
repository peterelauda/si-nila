package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EditKolam extends AppCompatActivity {

    // Widgets
    private ImageView suntingGambarKolam, gambarKolam, kembali;
    private EditText tambah_namaKolam, tambah_urlDatabase;
    private Button edit_kolam;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private CollectionReference collectionReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    // Data Kolam
    ActivityResultLauncher<String> mTakePhoto;
    Uri imageUri;
    private String kolamId, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_kolam);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Inisialisasi Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        if (currentUser != null) {
            String userID = currentUser.getUid();
            collectionReference = db.collection("Pengguna").document(userID).collection("Kolam");
        }

        // Inisialisasi widgets
        suntingGambarKolam = findViewById(R.id.imgPost);
        tambah_namaKolam = findViewById(R.id.editTextNamaKolam);
        tambah_urlDatabase = findViewById(R.id.editTextUrlDatabase);
        gambarKolam = findViewById(R.id.imgPostView);
        edit_kolam = findViewById(R.id.buttonBaruKolam);
        progressBar = findViewById(R.id.progressBar);
        kembali = findViewById(R.id.imageView1);

        kembali.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditKolam.this, KolamList.class);
                startActivity(i);
            }
        });

        // Mendapatkan data kolam dari intent
        Intent intent = getIntent();
        kolamId = intent.getStringExtra("kolam_id");
        imageUrl = intent.getStringExtra("image_url");
        String namaKolam = intent.getStringExtra("nama_kolam");
        String urlDatabase = intent.getStringExtra("url_database");

        // Menampilkan data kolam di EditText
        tambah_namaKolam.setText(namaKolam);
        tambah_urlDatabase.setText(urlDatabase);

        progressBar.setVisibility(View.INVISIBLE);

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .into(gambarKolam);
        }

        // Membuka Galeri untuk Mengedit Gambar
        mTakePhoto = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        // Showing the image
                        gambarKolam.setImageURI(result);

                        // Get the image URI
                        imageUri = result;
                    }
                }
        );
        // Tombol untuk Mengedit Gambar
        suntingGambarKolam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTakePhoto.launch("image/*");
            }
        });

        // Menyimpan data kolam yang diubah dan meneruskannya ke Firestore
        edit_kolam.setOnClickListener(view -> perbaharuiKolam());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            imageUri = data.getData();
            gambarKolam.setImageURI(imageUri); // Menampilkan gambar yang dipilih
        }
    }

    // Mengupdate data kolam ke Firestore
    private void perbaharuiKolam() {
        String namaKolamBaru = tambah_namaKolam.getText().toString().trim();
        String urlDatabaseBaru = tambah_urlDatabase.getText().toString().trim();

        if (TextUtils.isEmpty(namaKolamBaru) || TextUtils.isEmpty(urlDatabaseBaru)) {
            Toast.makeText(this, "Nama Kolam dan URL Database harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(namaKolamBaru) && !TextUtils.isEmpty(urlDatabaseBaru) && imageUri !=null) {
            // Update gambar jika ada gambar baru yang dipilih
            final StorageReference filePath = storageReference
                    .child("kolam_image/")
                    .child("my_image_" + Timestamp.now().getSeconds() + ".jpg");
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap = correctImageOrientation(bitmap, imageUri);
                Bitmap resizedBitmap = resizeToSquare(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();


                filePath.putBytes(data).addOnSuccessListener(taskSnapshot ->
                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                    // Dapatkan URL gambar yang baru dan lanjutkan update
                    updateFirestoreKolam(namaKolamBaru, urlDatabaseBaru, imageUrl);
                }));
            } catch (IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(EditKolam.this, "Gagal menambahkan gambar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Jika tidak ada gambar baru, hanya update data teks
            updateFirestoreKolam(namaKolamBaru, urlDatabaseBaru, imageUrl);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Mengupdate data kolam ke Firestore
    private void updateFirestoreKolam(String namaKolam, String urlDatabase, String newImageUrl) {
        // Membuat map data yang akan diupdate
        Map<String, Object> kolamUpdate = new HashMap<>();
        kolamUpdate.put("nama_kolam", namaKolam);
        kolamUpdate.put("url_database", urlDatabase);
        kolamUpdate.put("image_url", newImageUrl);

        collectionReference.document(kolamId).update(kolamUpdate)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditKolam.this, "Kolam berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish(); // Menutup activity setelah update berhasil
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(EditKolam.this, "Gagal memperbarui kolam", Toast.LENGTH_SHORT).show();
                });
    }

    // Memeriksa dan memperbaiki orientasi gambar
    private Bitmap correctImageOrientation(Bitmap bitmap, Uri uri) throws IOException {
        ExifInterface exif = new ExifInterface(getContentResolver().openInputStream(uri));
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        Matrix matrix = new Matrix();

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                break;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    // Mengubah ukuran gambar menjadi persegi
    private Bitmap resizeToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(width, height);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, (width - newWidth) / 2, (height - newWidth) / 2, newWidth, newWidth);
        return Bitmap.createScaledBitmap(resizedBitmap, 500, 500, true); // Atur ukuran sesuai keinginan
    }

}