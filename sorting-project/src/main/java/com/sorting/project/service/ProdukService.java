package com.sorting.project.service;

import com.sorting.project.model.Produk;
import com.sorting.project.repo.ProdukRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class ProdukService {

    @Autowired
    private ProdukRepo produkRepo;

    public List<Produk> findAll() {
        return this.produkRepo.findAll();
    }

    public Produk findOneById (String id) {
        return this.produkRepo.findOneById(id);
    }

    @Transactional
    public void save (Produk produk) {
        this.produkRepo.save(produk);
    }

}
