package com.sorting.project.service;

import com.sorting.project.model.Produk;
import com.sorting.project.repo.ProdukRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProdukService {

    @Autowired
    private ProdukRepo produkRepo;

    public List<Produk> findAll() {
        return this.produkRepo.findAll();
    }

}
