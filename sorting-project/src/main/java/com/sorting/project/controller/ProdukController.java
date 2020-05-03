package com.sorting.project.controller;

import com.sorting.project.model.Produk;
import com.sorting.project.service.ProdukService;
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
@RequestMapping("/produk")
@CrossOrigin(origins = "*")
public class ProdukController {

    @Autowired
    private ProdukService produkService;

    @RequestMapping("/list")
    ResponseEntity<Map<String,Object>> doList (@RequestBody Map<String, Object> request) {
        List<Produk> produkList;
        Map<String,Object> response = new HashMap<>();
        try {
            produkList = produkService.findAll();
            response.put("item",produkList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/getDetailProduk")
    ResponseEntity<Map<String,Object>> doDetail (@RequestBody Map<String, Object> request) {
        List<Produk> produkList;
        Map<String,Object> response = new HashMap<>();
        try {
            produkList = produkService.findAll();
            response.put("item",produkList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/add")
    ResponseEntity<Map<String,Object>> doAdd (@RequestBody Map<String, Object> request) {
        List<Produk> produkList;
        Map<String,Object> response = new HashMap<>();
        try {
            Object item = request.get("itemTambah");
            produkList = produkService.findAll();
            response.put("item",produkList);
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
        List<Produk> produkList;
        Map<String,Object> response = new HashMap<>();
        try {
            produkList = produkService.findAll();
            response.put("item",produkList);
            response.put("success", Boolean.TRUE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal mengambil data!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
