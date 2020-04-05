/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.repo.MasterAlatRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PROSIA
 */
@Service
public class MasterAlatService {
    
    @Autowired
    private MasterAlatRepo masterAlatRepo;
    
    public void deleteAll(){
        this.masterAlatRepo.deleteAll();
    }
    
}
