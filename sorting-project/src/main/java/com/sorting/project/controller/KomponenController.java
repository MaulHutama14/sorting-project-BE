package com.sorting.project.controller;


import com.sorting.project.model.Komponen;
import com.sorting.project.service.KomponenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/komponen")
public class KomponenController {

    @Autowired
    private KomponenService komponenService;

    @RequestMapping("/list")
    ResponseEntity<Map<String,Object>> doList (@RequestBody Map<String, Object> request) {
        List<Komponen> komponenList;
        Map<String,Object> response = new HashMap<>();
        try {
            komponenList = komponenService.findAll();
            response.put("item",komponenList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/edit")
    ResponseEntity<Map<String,Object>> doEdit (@RequestBody Map<String, Object> request) {
        return null;
    }

}
