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
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.isAktif=1 AND tpk.komponen.produk.statusProduk=1 \n"
            + "ORDER BY tpk.komponen.prioritas ASC")
    List<ProsesKomponen> findCuttingByDeadlinePriorWaktuJumProsNama();
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.id=?1 AND tpk.isAktif=1  AND tpk.komponen.isAktif=1 AND tpk.komponen.produk.statusProduk=1 ORDER BY tpk.proses.sortId ASC, tpk.nomor ASC")
    List<ProsesKomponen> findByIdProsesKomponen(String idProses, String orderBy);

    @Query("SELECT DISTINCT tpk.komponen.id FROM ProsesKomponen tpk WHERE tpk.isAktif=1 AND tpk.komponen.isAktif=1 AND tpk.komponen.produk.statusProduk=1 " +
            " ORDER BY  tpk.komponen.prioritasNest ASC")
    List<Object[]> findSortByNest ();

    @Query("SELECT DISTINCT tpk.komponen.id FROM ProsesKomponen tpk WHERE tpk.isAktif=1 AND tpk.komponen.isAktif=1 AND tpk.komponen.produk.statusProduk=1 " +
            " ORDER BY  tpk.komponen.prioritas ASC")
    List<Object[]> findSortByKomponenAndProses ();
    
    @Modifying
    @Query("UPDATE ProsesKomponen tpk SET tpk.assignDate=null, tpk.assignDateStr=null,tpk.assignEnd=null, tpk.assignEndStr=null, tpk.alat=null WHERE tpk.proses.id!='PLM'")
    void refreshAssignedDate();
    
    @Query("SELECT DISTINCT tpk.komponen.produk.namaProduk FROM ProsesKomponen tpk ORDER BY tpk.komponen.produk.tanggalAkhir ASC")
    List<String> findProdukAwal();
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.produk.namaProduk=?1 ORDER BY  tpk.proses.sortId DESC")
    List<ProsesKomponen> findProsesTerakhir(String namaProduk);
    
    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.isProses=?1 AND tpk.isAktif=1 AND tpk.komponen.produk.statusProduk=1" +
            " AND (DATE_FORMAT(tpk.assignDate, '%Y-%m-%d') = ?2 " +
            " OR DATE_FORMAT(tpk.assignEnd, '%Y-%m-%d') = ?3 ) ORDER BY  tpk.sortId ASC, tpk.proses.sortId ASC")
    List<ProsesKomponen> findByHasilSorting(Boolean status, String start, String end);

    @Query("SELECT DISTINCT tpk.komponen.produk.namaProduk FROM ProsesKomponen tpk WHERE tpk.isProses=?1 AND tpk.isAktif=1 AND tpk.komponen.produk.statusProduk=1" +
            " AND (DATE_FORMAT(tpk.assignDate, '%Y-%m-%d') = ?2 " +
            " OR DATE_FORMAT(tpk.assignEnd, '%Y-%m-%d') = ?3 ) ORDER BY  tpk.sortId ASC, tpk.proses.sortId ASC")
    List<Object[]> findProdukDistinct(Boolean status, String start, String end);

    @Query("SELECT tpk FROM ProsesKomponen tpk\n" +
            "WHERE tpk.komponen.produk.namaProduk=?1 AND tpk.isAktif=1 \n" +
            "ORDER BY tpk.nomor ASC, tpk.sortId ASC, tpk.proses.sortId ASC")
    List<ProsesKomponen> findByProduk(String namaProduk);

    @Query("SELECT u FROM ProsesKomponen u WHERE u.id=?1")
    public ProsesKomponen findOneById (String id);

    @Query("SELECT CASE WHEN COUNT(x) > 0 THEN true WHEN COUNT(x)=0 THEN false END FROM ProsesKomponen x WHERE x.komponen.id=?1 " +
            " AND x.proses.id=?2 AND x.nomor=?3")
    Boolean checkProsesKomponen (String idKomponen, String idProses, Integer numberKomponen);

    @Query("SELECT u FROM ProsesKomponen u WHERE u.komponen.id=?1 " +
            " AND u.proses.id=?2 AND u.nomor=?3")
    ProsesKomponen findOneByKompProcNumb (String idKomponen, String idProses, Integer numberKomponen);

    @Query("SELECT tpk FROM ProsesKomponen tpk WHERE tpk.komponen.isAktif=1 AND tpk.komponen.produk.statusProduk=1" +
            " ORDER BY  tpk.sortId ASC, tpk.nomor ASC, tpk.proses.sortId ASC")
    List<ProsesKomponen> findHasilSortingAll();

    @Modifying
    @Query(value = "UPDATE tx_proses_komponen tpk JOIN tx_komponen k ON k.id=tpk.id_komponen " +
            " JOIN tx_produk p ON p.nama_produk=k.master_nama_produk" +
            " SET tpk.is_aktif=false WHERE p.nama_produk=?1", nativeQuery = true)
    void nonAktifSemuaByProduk (String idProduk);

    @Modifying
    @Query("UPDATE ProsesKomponen tpk SET tpk.isProses=?1 WHERE tpk.id IN ?2 ")
    void updateStatusPengerjaan (Boolean status, List<String> id);

    @Query("SELECT tpk.nomor, tpk.assignDate, tpk.assignEnd, tpk.komponen.namaKomponen, " +
            " tpk.komponen.produk.namaProduk, tpk.alat.namaAlat, tpk.proses.namaProses" +
            " FROM ProsesKomponen tpk" +
            " WHERE tpk.isAktif=1" +
            " ORDER BY tpk.sortId ASC, tpk.nomor ASC, tpk.proses.sortId ASC")
    List<Object[]> getHasilSorting() ;

}
