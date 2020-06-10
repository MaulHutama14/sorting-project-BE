/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.Produk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 *
 * @author PROSIA
 */
@Repository
public interface ProdukRepo extends JpaRepository<Produk, String>{

    @Query("SELECT p FROM Produk p ORDER BY p.tanggalAkhir ASC")
    public List<Produk> findAll ();

    @Query("SELECT p FROM Produk p WHERE p.id=?1")
    public Produk findOneById (String id);

    @Modifying
    @Query("UPDATE Produk tk SET tk.statusProduk=?1, tk.modifiedOn = now() WHERE tk.id IN ?2 ")
    void updateStatusProduk (Boolean status, List<String> id);
    
}
