/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TX_PRODUK")
public class Produk implements Serializable{
    
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "NAMA_PRODUK", length = 50)
    private String namaProduk;
    
    @Column(name = "TANGGAL_AKHIR")
    private Date tanggalAkhir;
    
    private String tanggalAkhirStr;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
    
    
    
}
