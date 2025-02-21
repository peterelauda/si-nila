package com.example.eparrotfishnilabali;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{

    // Variables
    private Context context;
    private List<KolamModel> kolamModelList;

    public MyAdapter(Context context, List<KolamModel> kolamModelList) {
        this.context = context;
        this.kolamModelList = kolamModelList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.activity_monitoring, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        KolamModel currentKolamModel = kolamModelList.get(position);

        holder.namaKolam.setText(currentKolamModel.getNama_kolam());

        // Tombol untuk melihat riwayat
        holder.buttonRiwayat.setOnClickListener(v -> {
            // Buka RiwayatActivity dan kirim kolamId
            Intent intent = new Intent(context, RiwayatList.class);
            intent.putExtra("idKolam", currentKolamModel.getIdKolam());
            intent.putExtra("namaKolam", currentKolamModel.getNama_kolam());
            context.startActivity(intent);
        });

        // Tombol untuk melihat grafik
        holder.grafikBtn.setOnClickListener(v -> {
            // Buka RiwayatActivity dan kirim kolamId
            Intent intent = new Intent(context, GrafikActivity.class);
            intent.putExtra("idKolam", currentKolamModel.getIdKolam());
            intent.putExtra("namaKolam", currentKolamModel.getNama_kolam());
            context.startActivity(intent);
        });

        String imageUrl = currentKolamModel.getImage_url();

        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .into(holder.gambarKolam);

        // Format tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID"));
        String currentDate = dateFormat.format(new Date());
        holder.tanggal.setText(currentDate);

        // Format jam dengan zona waktu GMT+8
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String currentTime = timeFormat.format(new Date());
        holder.waktu.setText(currentTime);

        // Menambahkan fungsi klik untuk tombol sunting dan hapus
        holder.btnSunting.setOnClickListener(v -> suntingKolam(position));
        holder.btnHapus.setOnClickListener(v -> hapusKolam(position));

        // Inisialisasi Realtime Database dan referensi
        FirebaseDatabase database = FirebaseDatabase.getInstance(currentKolamModel.getUrl_database());
        DatabaseReference kolamRef = database.getReference("Kolam").child(currentKolamModel.getNama_kolam());

        // Mengambil data dari Realtime Database
        kolamRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Mengambil data dari Realtime Database
                    Float ph_air = dataSnapshot.child("ph_air").getValue(Float.class);
                    String status_kolam = dataSnapshot.child("status_kolam").getValue(String.class);
                    Integer kekeruhan_air = dataSnapshot.child("kekeruhan_air").getValue(Integer.class);
                    Integer ketinggian_air = dataSnapshot.child("ketinggian_air").getValue(Integer.class);

                    Boolean switch1 = dataSnapshot.child("switch1").getValue(Boolean.class);
                    Boolean switch2 = dataSnapshot.child("switch2").getValue(Boolean.class);
                    Boolean switch3 = dataSnapshot.child("switch3").getValue(Boolean.class);
                    Boolean switch4 = dataSnapshot.child("switch4").getValue(Boolean.class);
                    Boolean switch5 = dataSnapshot.child("switch5").getValue(Boolean.class);

                    holder.phAir.setText((ph_air != null ? ph_air : "N/A").toString());
                    holder.statusKolam.setText(status_kolam != null ? status_kolam : "N/A");
                    holder.kekeruhanAir.setText((kekeruhan_air != null ? kekeruhan_air : "N/A").toString());
                    holder.ketinggianAir.setText((ketinggian_air != null ? ketinggian_air : "N/A").toString());

                    holder.switchPertama.setChecked(switch1 != null ? switch1 : false);
                    holder.switchKedua.setChecked(switch2 != null ? switch2 : false);
                    holder.switchKetiga.setChecked(switch3 != null ? switch3 : false);
                    holder.switchKeempat.setChecked(switch4 != null ? switch4 : false);
                    holder.switchKelima.setChecked(switch5 != null ? switch5 : false);

                    // Update Model
                    currentKolamModel.setPh_air(ph_air);
                    currentKolamModel.setStatus_kolam(status_kolam);
                    currentKolamModel.setKekeruhan_air(kekeruhan_air);
                    currentKolamModel.setKetinggian_air(ketinggian_air);

                    currentKolamModel.setSwitch1(switch1);
                    currentKolamModel.setSwitch2(switch2);
                    currentKolamModel.setSwitch3(switch3);
                    currentKolamModel.setSwitch4(switch4);
                    currentKolamModel.setSwitch5(switch5);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MyAdapter", "Error reading data", databaseError.toException());
            }
        });

        // Listener untuk memperbarui Switch di Firebase saat statusnya berubah
        holder.switchPertama.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kolamRef.child("switch1").setValue(isChecked);
            currentKolamModel.setSwitch1(isChecked); // Update model
        });

        holder.switchKedua.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kolamRef.child("switch2").setValue(isChecked);
            currentKolamModel.setSwitch2(isChecked); // Update model
        });

        holder.switchKetiga.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kolamRef.child("switch3").setValue(isChecked);
            currentKolamModel.setSwitch3(isChecked); // Update model
        });

        holder.switchKeempat.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kolamRef.child("switch4").setValue(isChecked);
            currentKolamModel.setSwitch4(isChecked); // Update model
        });

        holder.switchKelima.setOnCheckedChangeListener((buttonView, isChecked) -> {
            kolamRef.child("switch5").setValue(isChecked);
            currentKolamModel.setSwitch5(isChecked); // Update model
        });
    }

    @Override
    public int getItemCount() {
        return kolamModelList.size();
    }

    // View Holder
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView namaKolam;

        // TextView dan Switch untuk Realtime Database
        public TextView kekeruhanAir, ketinggianAir, phAir, statusKolam;
        public Switch switchPertama, switchKedua, switchKetiga, switchKeempat, switchKelima;

        // Menambahkan tombol sunting dan hapus
        public ImageView gambarKolam, btnSunting, btnHapus;

        // Menambahkan time dan date
        public TextView waktu, tanggal;

        // Menambahkan tombol riwayat
        public ImageView buttonRiwayat, grafikBtn;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            namaKolam = itemView.findViewById(R.id.eparrotfish_kolam_list);
            gambarKolam = itemView.findViewById(R.id.eparrotfish_gambar_list);
            // Menambahkan tombol sunting dan hapus
            btnSunting = itemView.findViewById(R.id.sunting_btn);
            btnHapus = itemView.findViewById(R.id.hapus_btn);

            // Menambahkan TextView dan Switch untuk Realtime Database
            kekeruhanAir = itemView.findViewById(R.id.eparrotfish_kekeruhan_list);
            ketinggianAir = itemView.findViewById(R.id.eparrotfish_tinggi_list);
            phAir = itemView.findViewById(R.id.eparrotfish_ph_list);
            statusKolam = itemView.findViewById(R.id.eparrotfish_status_list);

            switchPertama = itemView.findViewById(R.id.switch1);
            switchKedua = itemView.findViewById(R.id.switch2);
            switchKetiga = itemView.findViewById(R.id.switch3);
            switchKeempat = itemView.findViewById(R.id.switch4);
            switchKelima = itemView.findViewById(R.id.switch5);

            // Menambahkan time dan date
            waktu = itemView.findViewById(R.id.eparrotfish_time);
            tanggal = itemView.findViewById(R.id.eparrotfish_date);

            // Menambahkan tombol riwayat
            buttonRiwayat = itemView.findViewById(R.id.riwayat_btn);

            // Menambahkan tombol grafik
            grafikBtn = itemView.findViewById(R.id.grafik_btn);
        }
    }

    private void suntingKolam(int position) {
        // Handle edit logic
        KolamModel currentKolamModel = kolamModelList.get(position);
        // Create an Intent to start the editing activity
        Intent intent = new Intent(context, EditKolam.class);
        intent.putExtra("kolam_id", currentKolamModel.getIdKolam());
        intent.putExtra("image_url", currentKolamModel.getImage_url());
        intent.putExtra("nama_kolam", currentKolamModel.getNama_kolam());
        intent.putExtra("url_database", currentKolamModel.getUrl_database());
        context.startActivity(intent);
    }

    private void hapusKolam(int position) {
        // Handle delete logic
        KolamModel currentKolamModel = kolamModelList.get(position);
        String kolamId = currentKolamModel.getIdKolam();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Assuming collectionReference is available in the adapter
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Pengguna").document(userID).collection("Kolam").document(kolamId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    kolamModelList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Kolam berhasil dihapus", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Kesalahan saat menghapus kolam", Toast.LENGTH_SHORT).show());
    }
}