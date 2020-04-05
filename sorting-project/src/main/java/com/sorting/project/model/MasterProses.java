/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.io.Serializable;
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
@Table(name = "TM_MASTER_PROSES")
public class MasterProses implements Serializable{
    
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "NAMA_PROSES", length = 80)
    private String namaProses;
    
    @Column(name = "DESKRIPSI_PROSES", length = 255)
    private String deskripsiProses;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNamaProses() {
        return namaProses;
    }

    public void setNamaProses(String namaProses) {
        this.namaProses = namaProses;
    }

    public String getDeskripsiProses() {
        return deskripsiProses;
    }

    public void setDeskripsiProses(String deskripsiProses) {
        this.deskripsiProses = deskripsiProses;
    }
    
    
    
}
