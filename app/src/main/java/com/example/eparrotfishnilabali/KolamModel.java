package com.example.eparrotfishnilabali;

public class KolamModel {

    private String idKolam;
    private String nama_kolam;
    private String image_url;
    private String url_database;

    private Float ph_air;
    private Integer kekeruhan_air;
    private Integer ketinggian_air;
    private Boolean switch1;
    private Boolean switch2;
    private Boolean switch3;
    private Boolean switch4;
    private Boolean switch5;
    private String status_kolam;

    public KolamModel() {

    }

    public KolamModel(String idKolam, String nama_kolam, String image_url, String url_database, Float ph_air, Integer kekeruhan_air, Integer ketinggian_air, Boolean switch1, Boolean switch2, Boolean switch3, Boolean switch4, Boolean switch5, String status_kolam) {
        this.idKolam = idKolam;
        this.nama_kolam = nama_kolam;
        this.image_url = image_url;
        this.url_database = url_database;
        this.ph_air = ph_air;
        this.kekeruhan_air = kekeruhan_air;
        this.ketinggian_air = ketinggian_air;
        this.switch1 = switch1;
        this.switch2 = switch2;
        this.switch3 = switch3;
        this.switch4 = switch4;
        this.switch5 = switch5;
        this.status_kolam = status_kolam;
    }

    public String getIdKolam() {
        return idKolam;
    }

    public void setIdKolam(String idKolam) {
        this.idKolam = idKolam;
    }

    public String getNama_kolam() {
        return nama_kolam;
    }

    public void setNama_kolam(String nama_kolam) {
        this.nama_kolam = nama_kolam;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getUrl_database() {
        return url_database;
    }

    public void setUrl_database(String url_database) {
        this.url_database = url_database;
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

    public String getStatus_kolam() {
        return status_kolam;
    }

    public void setStatus_kolam(String status_kolam) {
        this.status_kolam = status_kolam;
    }

}

