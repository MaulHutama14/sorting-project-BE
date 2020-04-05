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

    public List<Komponen> findSortedByPrioritas(List<Order> orderList) {
        
        return this.komponenRepo.findAll(Sort.by(orderList));
    }
    
}
