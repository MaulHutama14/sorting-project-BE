/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.*;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TM_MASTER_ALAT")
public class MasterAlat implements Serializable{
    
    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name = "NAMA_MASTER_ALAT", length = 100, unique = true, nullable = false)
    private String namaMasterAlat;
    
    @Column(name = "DESKRIPSI_ALAT", length = 255)
    private String deskripsiAlat;
    
    @ManyToOne
    @JoinColumn(name="ID_JENIS_PROSES", referencedColumnName = "ID", nullable = false)
    private Proses proses; 

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNamaMasterAlat() {
        return namaMasterAlat;
    }

    public void setNamaMasterAlat(String namaMasterAlat) {
        this.namaMasterAlat = namaMasterAlat;
    }

    public String getDeskripsiAlat() {
        return deskripsiAlat;
    }

    public void setDeskripsiAlat(String deskripsiAlat) {
        this.deskripsiAlat = deskripsiAlat;
    }

    public Proses getProses() {
        return proses;
    }

    public void setProses(Proses proses) {
        this.proses = proses;
    }
    
}
