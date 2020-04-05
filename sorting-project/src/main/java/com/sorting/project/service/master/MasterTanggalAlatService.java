/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service.master;

import com.sorting.project.model.master.MasterTanggalAlat;
import com.sorting.project.repo.MasterAlatRepo;
import com.sorting.project.repo.master.MasterTanggalRepo;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PROSIA
 */
@Service
public class MasterTanggalAlatService {
    
    @Autowired
    private MasterTanggalRepo mtRepo;
    
    public  List<MasterTanggalAlat> findByNamaAlat(String namaAlat, String orderBy){
        return this.mtRepo.findByNamaAlat( namaAlat, orderBy);
    }
    
    @Transactional
    public void saveAll(List<MasterTanggalAlat> list){
        this.mtRepo.saveAll(list);
    }
    
    @Transactional
    public void saveOne(MasterTanggalAlat alat){
        this.mtRepo.save(alat);
    }
    
    @Transactional
    public void refreshMta(){
        this.mtRepo.deleteAll();
    }
    
}
