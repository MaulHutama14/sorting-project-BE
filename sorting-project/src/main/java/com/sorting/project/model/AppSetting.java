/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author PROSIA
 */
@Entity
@Table(name="APP_SETTING")
public class AppSetting {
    
    @Id
    @Column(name="APP_NAME", unique = true, nullable = false)
    private String appName;
    
    @Column(name="APP_VALUE", nullable = false)
    private String appValue;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppValue() {
        return appValue;
    }

    public void setAppValue(String appValue) {
        this.appValue = appValue;
    }
    
}
