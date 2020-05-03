/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.MasterProses;
import com.sorting.project.model.Proses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PROSIA
 */
@Repository
public interface ProsesRepo extends JpaRepository<Proses, String>{

    @Query("SELECT p FROM Proses p WHERE p.id=?1")
    public Proses findOneById(String id);
    
}
