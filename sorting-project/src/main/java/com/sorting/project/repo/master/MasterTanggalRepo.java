/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo.master;

import com.sorting.project.model.master.MasterTanggalAlat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PROSIA
 */
@Repository
public interface MasterTanggalRepo extends JpaRepository<MasterTanggalAlat, Integer>{
    
    @Query("SELECT mta FROM MasterTanggalAlat mta WHERE mta.alat.namaAlat=?1 ORDER BY ?2")
    public List<MasterTanggalAlat> findByNamaAlat(String namaAlat, String orderBy);
    
}
