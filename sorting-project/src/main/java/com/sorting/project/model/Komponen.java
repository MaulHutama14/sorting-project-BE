/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author hp
 */
@Entity
@Table(name="TX_KOMPONEN")
public class Komponen implements Serializable{

    public Komponen() {
        this.id = UUID.randomUUID().toString();
        this.createdOn = new Date();
    }

    @Id
    @Column(name="ID")
    private String id;
    
    @Column(name="PRIORITAS", length = 50, nullable = false)
    private String prioritas;

    @Column(name = "PRIORITAS_NEST")
    private String prioritasNest;
        
    @Column(name="NAMA_KOMPONEN", length=50, nullable = false)
    private String namaKomponen;
    
    @Column(name="NAMA_BAGIAN", length = 255)
    private String namaBagian;
    
    @Column(name="KUANTITAS")
    private String kuantitas;
    
    @Column(name="DURASI_PENGERJAAN", nullable = false)
    private Integer durasiPengerjaan;
    
    @ManyToOne
    @JoinColumn(name = "MASTER_NAMA_PRODUK", referencedColumnName = "NAMA_PRODUK", nullable = false)
    private Produk produk;

    @Column(name = "UKURAN", nullable = false)
    private Integer ukuran;


    @Column(name = "CREATED_ON", nullable = false)
    private Date createdOn;

    @Column(name = "MODIFIED_ON")
    private Date modifiedOn;

    @JoinColumn(name = "CREATED_BY", referencedColumnName = "USER_NAME", nullable = false)
    @ManyToOne
    private User createdBy;

    @JoinColumn(name = "MODIFIED_BY", referencedColumnName = "USER_NAME")
    @ManyToOne
    private User modifiedBy;

    @Column(name = "IS_ACTIVE")
    private Boolean isAktif;

    @Column(name = "PANJANG")
    private Double panjang;

    @Column(name = "LEBAR")
    private Double lebar;

    @Column(name = "TINGGI")
    private Double tinggi;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrioritas() {
        return prioritas;
    }

    public void setPrioritas(String prioritas) {
        this.prioritas = prioritas;
    }

    public String getNamaKomponen() {
        return namaKomponen;
    }

    public void setNamaKomponen(String namaKomponen) {
        this.namaKomponen = namaKomponen;
    }

    public Integer getDurasiPengerjaan() {
        return durasiPengerjaan;
    }

    public void setDurasiPengerjaan(Integer durasiPengerjaan) {
        this.durasiPengerjaan = durasiPengerjaan;
    }

    public Produk getProduk() {
        return produk;
    }

    public void setProduk(Produk produk) {
        this.produk = produk;
    }

    public Integer getUkuran() {
        return ukuran;
    }

    public void setUkuran(Integer ukuran) {
        this.ukuran = ukuran;
    }

    public String getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(String kuantitas) {
        this.kuantitas = kuantitas;
    }

    public String getNamaBagian() {
        return namaBagian;
    }

    public void setNamaBagian(String namaBagian) {
        this.namaBagian = namaBagian;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Boolean getAktif() {
        return isAktif;
    }

    public void setAktif(Boolean aktif) {
        isAktif = aktif;
    }

    public Double getPanjang() {
        return panjang;
    }

    public void setPanjang(Double panjang) {
        this.panjang = panjang;
    }

    public Double getLebar() {
        return lebar;
    }

    public void setLebar(Double lebar) {
        this.lebar = lebar;
    }

    public Double getTinggi() {
        return tinggi;
    }

    public void setTinggi(Double tinggi) {
        this.tinggi = tinggi;
    }

    public String getPrioritasNest() {
        return prioritasNest;
    }

    public void setPrioritasNest(String prioritasNest) {
        this.prioritasNest = prioritasNest;
    }
}
