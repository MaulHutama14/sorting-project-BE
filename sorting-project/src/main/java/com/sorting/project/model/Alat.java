/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.hibernate.annotations.GeneratorType;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TM_ALAT")
public class Alat implements Serializable{

    public Alat() {
        this.ID = UUID.randomUUID().toString();
    }

    @Id
    @Column(name="ID")
    private String ID;
    
    @Column(name="NAMA_ALAT", unique = true,nullable = false, length = 100)
    private String namaAlat;
    
    @ManyToOne
    @JoinColumn(name="NAMA_MASTER_ALAT", referencedColumnName = "NAMA_MASTER_ALAT",nullable = false)
    private MasterAlat masterAlat;
    
    @Column(name="DESKRIPSI_ALAT", length = 255)
    private String deskAlat;
    
    @Column(name="BATAS_BAWAH_UKURAN")
    private BigDecimal batasBawah;
    
    @Column(name="BATAS_ATAS_UKURAN")
    private BigDecimal batasAtas;
    
    @Column(name="TANGGAL_ASSIGN")
    private Date tanggalAssign;

    @Column(name="WORK_LOAD")
    private Integer workLoad;

    @Column(name ="STATUS")
    private Boolean status;
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getNamaAlat() {
        return namaAlat;
    }

    public void setNamaAlat(String namaAlat) {
        this.namaAlat = namaAlat;
    }

    public MasterAlat getMasterAlat() {
        return masterAlat;
    }

    public void setMasterAlat(MasterAlat masterAlat) {
        this.masterAlat = masterAlat;
    }

    public String getDeskAlat() {
        return deskAlat;
    }

    public void setDeskAlat(String deskAlat) {
        this.deskAlat = deskAlat;
    }

    public BigDecimal getBatasBawah() {
        return batasBawah;
    }

    public void setBatasBawah(BigDecimal batasBawah) {
        this.batasBawah = batasBawah;
    }

    public BigDecimal getBatasAtas() {
        return batasAtas;
    }

    public void setBatasAtas(BigDecimal batasAtas) {
        this.batasAtas = batasAtas;
    }

    public Date getTanggalAssign() {
        return tanggalAssign;
    }

    public void setTanggalAssign(Date tanggalAssign) {
        this.tanggalAssign = tanggalAssign;
    }

    public Integer getWorkLoad() {
        return workLoad;
    }

    public void setWorkLoad(Integer workLoad) {
        this.workLoad = workLoad;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
