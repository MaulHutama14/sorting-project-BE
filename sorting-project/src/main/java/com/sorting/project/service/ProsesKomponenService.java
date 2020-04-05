/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.model.ProsesKomponen;
import com.sorting.project.repo.ProsesKomponenRepo;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PROSIA
 */
@Service
public class ProsesKomponenService {
    
    @Autowired
    private ProsesKomponenRepo prosesKomponenRepo;
    
    public List<ProsesKomponen> findCuttingByDeadlinePriorWaktuJumProsNama() {
        return this.prosesKomponenRepo.findCuttingByDeadlinePriorWaktuJumProsNama();
    }
    
    public List<ProsesKomponen> findByProsesAndSortByIdPorses (String idProses, String orderBy){
        return this.prosesKomponenRepo.findByProsesAndSortByIdPorses(idProses, orderBy);
    }
 
    @Transactional
    public void saveAll (List<ProsesKomponen> prosesKompList){
        this.prosesKomponenRepo.saveAll(prosesKompList);
    }
    
    @Transactional
    public void saveOne (ProsesKomponen item){
        this.prosesKomponenRepo.save(item);
    }
    
    @Transactional
    public void refreshProsesKomponen(){
        this.prosesKomponenRepo.refreshAssignedDate();
    }
    
    public List<String> findProdukAwal(){
        return this.prosesKomponenRepo.findProdukAwal();
    }
    
    public List<ProsesKomponen> findProsesTerakhir(String namaProduk){
        return this.prosesKomponenRepo.findProsesTerakhir(namaProduk);
    }
    
    public List<ProsesKomponen> findByHasilSorting(){
        return this.prosesKomponenRepo.findByHasilSorting();
    };
    
    
}
