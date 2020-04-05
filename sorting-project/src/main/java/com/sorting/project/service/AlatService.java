/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.model.Alat;
import com.sorting.project.repo.AlatRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author PROSIA
 */
@Service
public class AlatService {

    @Autowired
    private AlatRepo alatRepo;

    public List<Alat> findOneByMasterDescIsNotNull(String idProses) {
        return this.alatRepo.findOneByMasterDescIsNotNull(idProses);
    }
    
    public List<Alat> findOneByMasterDescIsNull(String idProses) {
        return this.alatRepo.findOneByMasterDescIsNull(idProses);
    }
    
    @Transactional
    public void save(Alat alat) {
        this.alatRepo.save(alat);
    }

    @Transactional
    public int refreshAlat() {
        return this.alatRepo.refreshAlat();
    }

}
