package com.sorting.project.controller;

import com.sorting.project.model.Alat;
import com.sorting.project.model.Produk;
import com.sorting.project.service.AlatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/alat")
public class AlatController {

    @Autowired
    private AlatService alatService;

    @RequestMapping("/list")
    ResponseEntity<Map<String,Object>> doList (@RequestBody Map<String, Object> request) {
        List<Alat> alatList;
        Map<String,Object> response = new HashMap<>();
        try {
            alatList = alatService.findAllActive();
            response.put("item",alatList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/list-detail")
    ResponseEntity<Map<String,Object>> doDetailList (@RequestBody Map<String, Object> request) {
        List<Alat> alatList;
        Map<String,Object> response = new HashMap<>();
        try {
            String masterNamaAlat = request.get("master_nama_alat").toString();

            alatList = alatService.findByMasterNamaAlat(masterNamaAlat);
            response.put("item",alatList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
