package com.example.eparrotfishnilabali;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class TambahKolam extends AppCompatActivity {

    // Menambahkan Widget
    ImageView tambah_gambarKolam;
    EditText tambah_namaKolam, tambah_urlDatabase;
    Button buat_kolam;
    ImageView gambarKolam, kembali;
    ProgressBar progressBar;

    // Mendapatkan userID
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = auth.getCurrentUser();
    String userID = currentUser.getUid();

    // Menginisialisasikan koleksi kolam
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collectionReference = db.collection("Pengguna").document(userID).collection("Kolam");

    // Firebase Storage
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    // Menggunakan Activity Result Launcher
    ActivityResultLauncher<String> mTakePhoto;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah_kolam);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi Widget dan Progressbar
        tambah_gambarKolam = findViewById(R.id.imgPost);
        tambah_namaKolam = findViewById(R.id.editTextNamaKolam);
        tambah_urlDatabase = findViewById(R.id.editTextUrlDatabase);
        buat_kolam = findViewById(R.id.buttonBuatKolam);
        gambarKolam = findViewById(R.id.imgPostView);
        progressBar = findViewById(R.id.progressBar);
        kembali = findViewById(R.id.imageView1);

        progressBar.setVisibility(View.INVISIBLE);

        // Memberi fungsi pada tombol
        if (currentUser != null) {
            kembali.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(TambahKolam.this, KolamList.class));
                }
            });
            buat_kolam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { simpanKolam(); }
            });
        } else {
            Intent i = new Intent(TambahKolam.this, MainActivity.class);
            startActivity(i);
            finish();
        }

        // Mengambil gambar dari galeri
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

        // Tombol untuk Tambah Kolam
        tambah_gambarKolam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTakePhoto.launch("image/*");
            }
        });
    }

    // Fungsi Menyimpan Kolam
    private void simpanKolam() {

        String namaKolam = tambah_namaKolam.getText().toString().trim();
        String urlDatabase = tambah_urlDatabase.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        if(!TextUtils.isEmpty(namaKolam) && !TextUtils.isEmpty(urlDatabase) && imageUri !=null) {
            final StorageReference filePath = storageReference
                    .child("kolam_image/")
                    .child("my_image_" + Timestamp.now().getSeconds() + ".jpg");

            try {
                // Mengubah gambar menjadi bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                bitmap = correctImageOrientation(bitmap, imageUri);
                Bitmap resizedBitmap = resizeToSquare(bitmap);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                filePath.putBytes(data).addOnSuccessListener(taskSnapshot ->
                        filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();

                            KolamModel kolamModel = new KolamModel();
                            kolamModel.setImage_url(imageUrl);
                            kolamModel.setNama_kolam(tambah_namaKolam.getText().toString());
                            kolamModel.setUrl_database(tambah_urlDatabase.getText().toString());

                            Map<String, Object> dataKolam = new HashMap<>();
                            dataKolam.put("image_url", imageUrl);
                            dataKolam.put("nama_kolam", tambah_namaKolam.getText().toString());
                            dataKolam.put("url_database", tambah_urlDatabase.getText().toString());

                            collectionReference.add(dataKolam)
                                    .addOnSuccessListener(documentReference -> {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Intent i = new Intent(TambahKolam.this, KolamList.class);
                                        startActivity(i);
                                        finish();
                                        Toast.makeText(TambahKolam.this, "Berhasil menambahkan kolam", Toast.LENGTH_SHORT).show();
                                        Log.d("Firestore", "Kolam ditambahkan dengan ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(TambahKolam.this, "Gagal menambahkan kolam", Toast.LENGTH_SHORT).show();
                                        Log.w("Firestore", "Gagal menambahkan kolam", e);
                                    });
                        })
                ).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(TambahKolam.this, "Gagal menambahkan kolam", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(TambahKolam.this, "Gagal menambahkan gambar", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Fungsi untuk memperbaiki orientasi gambar
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

    // Fungsi untuk mengubah ukuran gambar menjadi persegi
    private Bitmap resizeToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(width, height);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, (width - newWidth) / 2, (height - newWidth) / 2, newWidth, newWidth);
        return Bitmap.createScaledBitmap(resizedBitmap, 500, 500, true); // Atur ukuran sesuai keinginan
    }
}