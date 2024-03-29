package com.sorting.project;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author PROSIA
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.sorting.project.*")
public class SortingProjectApplication {
    
    public static void main(String[] args) {
		      SpringApplication.run(SortingProjectApplication.class, args);
	}
    
}
