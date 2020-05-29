/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.Alat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PROSIA
 */
@Repository
public interface AlatRepo extends JpaRepository<Alat, Integer> {

    @Query("SELECT a FROM Alat a WHERE a.masterAlat.proses.id = ?1 AND a.tanggalAssign IS NULL AND a.status = 1"
            + " ORDER BY a.tanggalAssign ASC, a.workLoad ASC")
    List<Alat> findOneByMasterDescIsNull(String idProses);
    
    @Query("SELECT a FROM Alat a WHERE a.masterAlat.proses.id = ?1 AND a.tanggalAssign IS NOT NULL AND a.status = 1"
            + " ORDER BY a.tanggalAssign ASC, a.workLoad ASC")
    List<Alat> findOneByMasterDescIsNotNull(String idProses);

    @Query("SELECT a FROM Alat a WHERE a.status=1")
    List<Alat> findAllActive ();

    @Modifying
    @Query("UPDATE Alat a SET a.tanggalAssign=null, a.workLoad=0")
    int refreshAlat();

    @Query("SELECT a FROM Alat a WHERE a.namaAlat = ?1")
    Alat findByNamaAlat (String namaAlat);

    @Query("SELECT a FROM Alat a WHERE a.masterAlat.namaMasterAlat=?1")
    List<Alat> findByMasterNamaAlat (String namaMasterAlat);

}
