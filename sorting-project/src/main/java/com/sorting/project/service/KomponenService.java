/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.model.Komponen;
import com.sorting.project.repo.KomponenRepo;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 *
 * @author PROSIA
 */
@Service
public class KomponenService {

    @Autowired
    private KomponenRepo komponenRepo;

    public List<Komponen> findAll() {
        return this.komponenRepo.findAll();
    }

    @Transactional
    public void save(Komponen komponen) {
        this.komponenRepo.save(komponen);
    }

    public Komponen findOneById(String id) {
        return this.komponenRepo.findOneById(id);
    }

    public List<Komponen> findSortedByPrioritas(List<Order> orderList) {
        
        return this.komponenRepo.findAll(Sort.by(orderList));
    }
    
}
