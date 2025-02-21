package com.example.eparrotfishnilabali;

import com.google.firebase.Timestamp;

public class RiwayatModel {
    private String idRiwayat;
    private String idKolam;
    private Float ph_air;
    private Integer kekeruhan_air;
    private Integer ketinggian_air;
    private Boolean switch1;
    private Boolean switch2;
    private Boolean switch3;
    private Boolean switch4;
    private Boolean switch5;
    private String statusKolam;
    private Timestamp timestamp;

    public RiwayatModel() {

    }

    public RiwayatModel(String idRiwayat, String idKolam, Float ph_air, Integer kekeruhan_air, Integer ketinggian_air, Boolean switch1, Boolean switch2, Boolean switch3, Boolean switch4, Boolean switch5, String statusKolam, Timestamp timestamp) {
        this.idRiwayat = idRiwayat;
        this.idKolam = idKolam;
        this.ph_air = ph_air;
        this.kekeruhan_air = kekeruhan_air;
        this.ketinggian_air = ketinggian_air;
        this.switch1 = switch1;
        this.switch2 = switch2;
        this.switch3 = switch3;
        this.switch4 = switch4;
        this.switch5 = switch5;
        this.statusKolam = statusKolam;
        this.timestamp = timestamp;
    }

    public String getIdRiwayat() {
        return idRiwayat;
    }

    public void setIdRiwayat(String idRiwayat) {
        this.idRiwayat = idRiwayat;
    }

    public String getIdKolam() {
        return idKolam;
    }

    public void setIdKolam(String idKolam) {
        this.idKolam = idKolam;
    }

    public Float getPh_air() {
        return ph_air;
    }

    public void setPh_air(Float ph_air) {
        this.ph_air = ph_air;
    }

    public Integer getKekeruhan_air() {
        return kekeruhan_air;
    }

    public void setKekeruhan_air(Integer kekeruhan_air) {
        this.kekeruhan_air = kekeruhan_air;
    }

    public Integer getKetinggian_air() {
        return ketinggian_air;
    }

    public void setKetinggian_air(Integer ketinggian_air) {
        this.ketinggian_air = ketinggian_air;
    }

    public Boolean getSwitch1() {
        return switch1;
    }

    public void setSwitch1(Boolean switch1) {
        this.switch1 = switch1;
    }

    public Boolean getSwitch2() {
        return switch2;
    }

    public void setSwitch2(Boolean switch2) {
        this.switch2 = switch2;
    }

    public Boolean getSwitch3() {
        return switch3;
    }

    public void setSwitch3(Boolean switch3) {
        this.switch3 = switch3;
    }

    public Boolean getSwitch4() {
        return switch4;
    }

    public void setSwitch4(Boolean switch4) {
        this.switch4 = switch4;
    }

    public Boolean getSwitch5() {
        return switch5;
    }

    public void setSwitch5(Boolean switch5) {
        this.switch5 = switch5;
    }

    public String getStatusKolam() {
        return statusKolam;
    }

    public void setStatusKolam(String statusKolam) {
        this.statusKolam = statusKolam;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}