package com.sorting.project.service;

import com.sorting.project.model.User;
import com.sorting.project.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public User findOneById (String id) {
       return this.userRepo.findOneById(id);
    }

}
