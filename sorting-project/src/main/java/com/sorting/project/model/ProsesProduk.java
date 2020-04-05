/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.ManyToAny;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TX_PROSES_PRODUK")
public class ProsesProduk {
    
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "NAMA_PRODUK", referencedColumnName = "NAMA_PRODUK")
    private Produk produk;
    
    @ManyToOne
    @JoinColumn(name = "NAMA_PROSES", referencedColumnName = "ID")
    private MasterProses proses;
    
    @Column(name = "DURASI_TOTAL_PROSES")
    private Integer durasiTotalProses;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Produk getProduk() {
        return produk;
    }

    public void setProduk(Produk produk) {
        this.produk = produk;
    }

    public MasterProses getProses() {
        return proses;
    }

    public void setProses(MasterProses proses) {
        this.proses = proses;
    }

    public Integer getDurasiTotalProses() {
        return durasiTotalProses;
    }

    public void setDurasiTotalProses(Integer durasiTotalProses) {
        this.durasiTotalProses = durasiTotalProses;
    }
    
    
    
}
