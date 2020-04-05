/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
@Table(name = "TX_PROSES_KOMPONEN")
public class ProsesKomponen {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer ID;

    @Column(name = "DURASI_PROSES")
    private Integer durasiProses;

    @Column(name = "ASSIGN_DATE")
    private Date assignDate;

    @Column(name="ASSIGN_DATE_STR")
    private String assignDateStr;

    @Column(name = "ASSIGN_END")
    private Date assignEnd;

    @Column(name="ASSIGN_END_STR")
    private String assignEndStr;

    @ManyToOne
    @JoinColumn(name = "NAMA_ID_ALAT", referencedColumnName = "NAMA_ALAT")
    private Alat alat;

    @ManyToOne
    @JoinColumn(name = "ID_PROSES", referencedColumnName = "ID")
    private Proses proses;

    @ManyToOne
    @JoinColumn(name = "NAMA_KOMPONEN", referencedColumnName = "NAMA_KOMPONEN")
    private Komponen komponen;

    @Column(name = "SORT_ID")
    private Integer sortId;

    public Date getAssignEnd() {
        return assignEnd;
    }

    public void setAssignEnd(Date assignEnd) {
        this.assignEnd = assignEnd;

        if (this.assignEnd != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            this.setAssignDateStr(sdf.format(assignEnd));
        }
    }

    public String getAssignEndStr() {
        return assignEndStr;
    }

    public void setAssignEndStr(String assignEndStr) {
        this.assignEndStr = assignEndStr;
    }

    public Alat getAlat() {
        return alat;
    }

    public void setAlat(Alat alat) {
        this.alat = alat;
    }

    public Integer getSortId() {
        return sortId;
    }

    public void setSortId(Integer sortId) {
        this.sortId = sortId;
    }

    public Date getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Date assignDate) throws ParseException {
        this.assignDate = assignDate;

        if (this.assignDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            this.setAssignDateStr(sdf.format(assignDate));

        }
    }

    public String getAssignDateStr() {
        return assignDateStr;
    }

    public void setAssignDateStr(String assignDateStr) {
        this.assignDateStr = assignDateStr;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getDurasiProses() {
        return durasiProses;
    }

    public void setDurasiProses(Integer durasiProses) {
        this.durasiProses = durasiProses;
    }

    public Proses getProses() {
        return proses;
    }

    public void setProses(Proses proses) {
        this.proses = proses;
    }

    public Komponen getKomponen() {
        return komponen;
    }

    public void setKomponen(Komponen komponen) {
        this.komponen = komponen;
    }

}
