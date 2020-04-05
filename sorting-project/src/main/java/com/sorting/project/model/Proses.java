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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name="TM_PROSES")
public class Proses implements Serializable{
    
    @Id
    @Column(name="ID", length = 15)
    private String ID;
    
    @Column(name="NAMA_PROSES", length = 100)
    private String namaProses;
    
    @ManyToOne
    @JoinColumn(name="NAMA_M_PROSES", referencedColumnName = "NAMA_PROSES")
    private MasterProses masterProses;
    
    @Column(name="DESKRIPSI_PROSES", length = 255)
    private String deskProses;
    
    @Column(name="SORT_ID", unique = true)
    private Integer sortId;

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNamaProses() {
        return namaProses;
    }

    public void setNamaProses(String namaProses) {
        this.namaProses = namaProses;
    }

    public MasterProses getMasterProses() {
        return masterProses;
    }

    public void setMasterProses(MasterProses masterProses) {
        this.masterProses = masterProses;
    }

    public String getDeskProses() {
        return deskProses;
    }

    public void setDeskProses(String deskProses) {
        this.deskProses = deskProses;
    }
    
    
    
}
