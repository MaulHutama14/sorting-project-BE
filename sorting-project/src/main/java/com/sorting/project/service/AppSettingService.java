/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.model.AppSetting;
import com.sorting.project.repo.AppSettingRepo;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PROSIA
 */
@Service
public class AppSettingService {

    @Autowired
    private AppSettingRepo appSettingRepo;
    
    public AppSetting findById(String appName) {
        return this.appSettingRepo.findOneById(appName);
    }
    
}
