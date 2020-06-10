package com.sorting.project.controller;

import com.sorting.project.model.Produk;
import com.sorting.project.model.User;
import com.sorting.project.service.ProdukService;
import com.sorting.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/produk")
@CrossOrigin(origins = "*")
public class ProdukController {

    @Autowired
    private ProdukService produkService;

    @Autowired
    private UserService userService;

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
            Map<String,Object> item = (Map<String, Object>) request.get("itemTambah");
            Produk newProduk = new Produk();
            User user = userService.findOneById("1");
            String deadlineDateStr  = item.get("tanggalProduk").toString();
            SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
            Date deadlineDate = sdf.parse(deadlineDateStr);
            Integer kuantitas = Integer.parseInt(item.get("kuantitas").toString());
            Boolean status = Boolean.parseBoolean(item.get("statusProduk").toString());

            newProduk.setNamaProduk(item.get("namaProduk").toString());
            newProduk.setTanggalAkhir(deadlineDate);
            newProduk.setCreatedBy(user);
            newProduk.setKuantitas(kuantitas);
            newProduk.setStatusProduk(status);
            newProduk.setStatusPengerjaan(0);

            produkService.save(newProduk);
            response.put("message", "Berhasil simpan produk!");
            response.put("success", Boolean.TRUE);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            response.put("message","Produk sudah ada!");
            response.put("success", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal simpan produk!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/edit")
    ResponseEntity<Map<String,Object>> doEditProduk (@RequestBody Map<String, Object> request) {
        List<Produk> produkList;
        Map<String,Object> response = new HashMap<>();
        try {
            Map<String,Object> item = (Map<String, Object>) request.get("itemTambah");
            Produk produk = produkService.findOneById(item.get("id").toString());
            String deadlineDateStr  = item.get("tanggalProduk").toString();
            SimpleDateFormat sdf =  new SimpleDateFormat("yyyy-MM-dd");
            Date deadlineDate = sdf.parse(deadlineDateStr);
            Integer kuantitas = Integer.parseInt(item.get("kuantitas").toString());
            String deskripsi = item.get("statusProduk").toString();


            produk.setNamaProduk(item.get("namaProduk").toString());
            produk.setTanggalAkhir(deadlineDate);
            produk.setKuantitas(kuantitas);
            produk.setDeskripsi(deskripsi);

            produkService.save(produk);
            response.put("message", "Berhasil simpan produk!");
            response.put("success", Boolean.TRUE);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            response.put("message","Produk sudah ada!");
            response.put("success", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal simpan produk!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping("/edit-status-produk")
    ResponseEntity<Map<String,Object>> doEdit (@RequestBody Map<String, Object> request) {
        Map<String,Object> response = new HashMap<>();

        try {
            List<String> listIdProses = (List<String>) request.get("id");
            Boolean status = Boolean.parseBoolean(request.get("status").toString());

            produkService.updateStatusProduk(status, listIdProses);
            response.put("message", "Berhasil simpan status produk!");
            response.put("success", Boolean.TRUE);
        } catch (NullPointerException e) {
            response.put("message", "Parameter produk atau status tidak boleh kosong!");
            response.put("success", Boolean.FALSE);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "Gagal simpan status produk!");
            response.put("success", Boolean.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
