package com.sorting.project.service;

import com.sorting.project.model.Proses;
import com.sorting.project.repo.ProsesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProsesService {

    @Autowired
    private ProsesRepo prosesRepo;

    public Proses findOneById (String id) {
        return prosesRepo.findOneById(id);
    }

}
