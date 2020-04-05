/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.controller;

import com.sorting.project.model.Komponen;
import com.sorting.project.service.KomponenService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author PROSIA
 */
@Controller
@RequestMapping("/home")
public class HomeController {
    
    @Autowired
    private KomponenService komponenService;
    
    @RequestMapping("/welcome")
    public String doHome (){
              
//        String a = request.get("item").toString();
        
        List<Komponen> komponenList = komponenService.findAll();
        System.out.println(komponenList.get(0).getDurasiPengerjaan());
        
        
        System.out.println("MASOOOKK");
        
        return "/home";
    }
    
}
