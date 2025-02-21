package com.example.eparrotfishnilabali;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class RiwayatAdapter extends RecyclerView.Adapter<RiwayatAdapter.ViewHolder> {

    private Context context;
    private List<RiwayatModel> riwayatList;

    public RiwayatAdapter(Context context, List<RiwayatModel> riwayatList) {
        this.context = context;
        this.riwayatList = riwayatList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_baris_riwayat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RiwayatAdapter.ViewHolder holder, int position) {
        RiwayatModel riwayat = riwayatList.get(position);
        holder.phAir.setText("pH: " + riwayat.getPh_air());
        holder.statusKolam.setText("Status: " + riwayat.getStatusKolam());
        holder.kekeruhanAir.setText("Kekeruhan: " + riwayat.getKekeruhan_air());
        holder.ketinggianAir.setText("Ketinggian: " + riwayat.getKetinggian_air());

        // Format timestamp
        if (riwayat.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));

            // Set GMT+8 TimeZone
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));

            // Format timestamp dari riwayat
            holder.timeStamp.setText(sdf.format(riwayat.getTimestamp().toDate()));

        }
        holder.switch1.setText("Switch 1: " + riwayat.getSwitch1());
        holder.switch2.setText("Switch 2: " + riwayat.getSwitch2());
        holder.switch3.setText("Switch 3: " + riwayat.getSwitch3());
        holder.switch4.setText("Switch 4: " + riwayat.getSwitch4());
        holder.switch5.setText("Switch 5: " + riwayat.getSwitch5());

    }

    @Override
    public int getItemCount() {

        return riwayatList.size();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView phAir, kekeruhanAir, ketinggianAir, statusKolam, timeStamp, switch1, switch2, switch3, switch4, switch5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            phAir = itemView.findViewById(R.id.ph_riwayat);
            kekeruhanAir = itemView.findViewById(R.id.kekeruhan_riwayat);
            ketinggianAir = itemView.findViewById(R.id.ketinggian_riwayat);
            statusKolam = itemView.findViewById(R.id.status_riwayat);
            timeStamp = itemView.findViewById(R.id.timestamp_riwayat);
            switch1 = itemView.findViewById(R.id.switch1_riwayat);
            switch2 = itemView.findViewById(R.id.switch2_riwayat);
            switch3 = itemView.findViewById(R.id.switch3_riwayat);
            switch4 = itemView.findViewById(R.id.switch4_riwayat);
            switch5 = itemView.findViewById(R.id.switch5_riwayat);
        }
    }
}