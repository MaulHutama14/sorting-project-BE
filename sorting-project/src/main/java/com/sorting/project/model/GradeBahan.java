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
import javax.persistence.Table;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name = "TM_GRADE_BAHAN")
public class GradeBahan {
    
    @Id
    @Column(name = "ID_GRADE_BAHAN", length = 150, nullable = false, unique = true)
    private String namaGrade;

    @Column(name = "VALUE_GRADE", length = 100)
    private String valueGrade;

    public String getNamaGrade() {
        return namaGrade;
    }

    public void setNamaGrade(String namaGrade) {
        this.namaGrade = namaGrade;
    }

    public String getValueGrade() {
        return valueGrade;
    }

    public void setValueGrade(String valueGrade) {
        this.valueGrade = valueGrade;
    }
    
    
    
}
