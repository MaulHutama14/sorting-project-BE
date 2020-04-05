/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model.master;

import com.sorting.project.model.Alat;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name="MASTER_TANGGAL_ALAT")
public class MasterTanggalAlat {
    
    @Id
    @Column(name="ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    
    @Column(name="TANGGAL_ALAT")
    private Date tanggalAlat;
    
    @ManyToOne
    @JoinColumn(name="NAMA_ALAT", referencedColumnName = "NAMA_ALAT")
    private Alat alat;

    public Date getTanggalAlat() {
        return tanggalAlat;
    }

    public void setTanggalAlat(Date tanggalAlat) {
        this.tanggalAlat = tanggalAlat;
    }

    public Alat getAlat() {
        return alat;
    }

    public void setAlat(Alat alat) {
        this.alat = alat;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    
    
    
}
