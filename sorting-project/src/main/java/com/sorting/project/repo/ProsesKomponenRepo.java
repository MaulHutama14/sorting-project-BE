/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.ProsesKomponen;

import java.util.Date;
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
public interface ProsesKomponenRepo extends JpaRepository<ProsesKomponen, Integer>{
    
    @Query("SELECT tpk FROM ProsesKomponen tpk GROUP BY tpk.komponen.namaKomponen \n"
            + "ORDER BY tpk.komponen.produk.tanggalAkhir ASC, tpk.komponen.prioritas ASC, tpk.komponen.durasiPengerjaan DESC, COUNT(tpk) DESC, tpk.komponen.namaKomponen ASC")
    List<ProsesKomponen> findCuttingByDeadlinePriorWaktuJumProsNama();
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.namaKomponen=?1 ORDER BY tpk.proses.sortId ASC")
    List<ProsesKomponen> findByProsesAndSortByIdPorses (String idProses, String orderBy);

    @Query("SELECT DISTINCT tpk.komponen.namaKomponen FROM ProsesKomponen tpk  ORDER BY tpk.sortId ASC, tpk.proses.sortId ASC")
    List<String> findSortByKomponenAndProses ();
    
    @Modifying
    @Query("UPDATE ProsesKomponen tpk SET tpk.assignDate=null, tpk.assignDateStr=null,tpk.assignEnd=null, tpk.assignEndStr=null, tpk.alat=null")
    void refreshAssignedDate();
    
    @Query("SELECT DISTINCT tpk.komponen.produk.namaProduk FROM ProsesKomponen tpk ORDER BY tpk.komponen.produk.tanggalAkhir ASC")
    List<String> findProdukAwal();
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.produk.namaProduk=?1 ORDER BY  tpk.proses.sortId DESC")
    List<ProsesKomponen> findProsesTerakhir(String namaProduk);
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.isProses=?1 " +
            " AND (DATE_FORMAT(tpk.assignDate, '%Y-%m-%d') = ?2 " +
            " OR DATE_FORMAT(tpk.assignEnd, '%Y-%m-%d') = ?3 ) ORDER BY  tpk.sortId ASC")
    List<ProsesKomponen> findByHasilSorting(Boolean status, String start, String end);

    @Query("SELECT DISTINCT tpk.komponen.produk.namaProduk FROM ProsesKomponen tpk WHERE tpk.isProses=?1 " +
            " AND (DATE_FORMAT(tpk.assignDate, '%Y-%m-%d') = ?2 " +
            " OR DATE_FORMAT(tpk.assignEnd, '%Y-%m-%d') = ?3 ) ORDER BY  tpk.sortId ASC")
    List<Object[]> findProdukDistinct(Boolean status, String start, String end);
    
}
