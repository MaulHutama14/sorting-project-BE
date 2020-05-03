/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PROSIA
 */
@Repository
public interface UserRepo extends JpaRepository<User, String>{

    @Query("SELECT u FROM User u WHERE u.id=?1")
    public User findOneById (String id);
    
}
