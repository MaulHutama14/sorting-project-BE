/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.Komponen;
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
public interface KomponenRepo extends JpaRepository<Komponen, String>{

    @Query("SELECT a FROM Komponen a WHERE a.id=?1")
    public Komponen findOneById(String id);

    @Query("SELECT a FROM Komponen a WHERE a.namaKomponen=?1 AND a.produk.namaProduk=?2")
    Komponen findByNameAndProdukName (String komponenName, String produkId);

    @Modifying
    @Query("UPDATE Komponen tk SET tk.isAktif=?1 WHERE tk.id IN ?2 ")
    void updateStatusAktif (Boolean status, List<String> id);

}
