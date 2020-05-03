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
import javax.persistence.*;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TX_PRODUK")
public class Produk implements Serializable{

    public Produk() {
        this.id = UUID.randomUUID().toString();
        this.createdOn = new Date();
    }

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "KUANTITAS")
    private Integer kuantitas;
    
    @Column(name = "NAMA_PRODUK", length = 50)
    private String namaProduk;
    
    @Column(name = "TANGGAL_AKHIR")
    private Date tanggalAkhir;

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

    @Column(name = "STATUS_PRODUK")
    private Boolean statusProduk;

    @Column(name = "STATUS_PENGERJAAN")
    private Boolean statusPengerjaan;

    private String tanggalAkhirStr;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamaProduk() {
        return namaProduk;
    }

    public void setNamaProduk(String namaProduk) {
        this.namaProduk = namaProduk;
    }

    public Date getTanggalAkhir() {
        return tanggalAkhir;
    }

    public void setTanggalAkhir(Date tanggalAkhir) {
        this.tanggalAkhir = tanggalAkhir;
        
        if (tanggalAkhir != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            this.setTanggalAkhirStr(tanggalAkhirStr);
        }
    }

    public String getTanggalAkhirStr() {
        return tanggalAkhirStr;
    }

    public void setTanggalAkhirStr(String tanggalAkhirStr) {
        this.tanggalAkhirStr = tanggalAkhirStr;
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

    public Integer getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(Integer kuantitas) {
        this.kuantitas = kuantitas;
    }

    public Boolean getStatusProduk() {
        return statusProduk;
    }

    public void setStatusProduk(Boolean statusProduk) {
        this.statusProduk = statusProduk;
    }

    public Boolean getStatusPengerjaan() {
        return statusPengerjaan;
    }

    public void setStatusPengerjaan(Boolean statusPengerjaan) {
        this.statusPengerjaan = statusPengerjaan;
    }
}
