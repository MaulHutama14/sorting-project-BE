/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.repo;

import com.sorting.project.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 *
 * @author PROSIA
 */
@Repository
public interface AppSettingRepo extends JpaRepository<AppSetting, String>{

    @Query("SELECT x FROM AppSetting x WHERE x.appName=?1")
    AppSetting findOneById (String appName);
    
}
