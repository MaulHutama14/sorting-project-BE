package com.sorting.project.controller;

import com.sorting.project.model.Alat;
import com.sorting.project.model.MasterAlat;
import com.sorting.project.model.Produk;
import com.sorting.project.service.AlatService;
import com.sorting.project.service.MasterAlatService;
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

    @Autowired
    private MasterAlatService masterAlatService;

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

    @RequestMapping("/edit-pengerjaan")
    ResponseEntity<Map<String,Object>> doEditPengerjaan (@RequestBody Map<String, Object> request) {
        Map<String,Object> response = new HashMap<>();

        try {
            List<String> listIdAlat = (List<String>) request.get("list_id");
            Boolean status = Boolean.parseBoolean(request.get("status").toString());

            alatService.editStatus(status, listIdAlat);
            response.put("message","Berhasil ubah status Alat!");
            response.put("result",true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            response.put("message","Parameter request ada yang salah!");
            response.put("result",false);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message","Gagal ubah status Alat!");
            response.put("result",false);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping("/do-add")
    ResponseEntity<Map<String,Object>> doAdd (@RequestBody Map<String, Object> request) {
        Map<String,Object> response = new HashMap<>();

        try {
            Map<String, Object> listIdAlat = (Map<String,Object>) request.get("item");
            String namaAlat = listIdAlat.get("namaAlat").toString();
            String deskripsi = listIdAlat.get("deskripsi").toString();
            Boolean status = Boolean.parseBoolean(listIdAlat.get("status").toString());
            MasterAlat masterAlat = masterAlatService.findById(Integer.parseInt(listIdAlat.get("idMasterAlat").toString()));
            Alat alat = new Alat();
            alat.setNamaAlat(namaAlat);
            alat.setDeskAlat(deskripsi);
            alat.setMasterAlat(masterAlat);
            alat.setStatus(status);

            alatService.save(alat);
            response.put("message","Berhasil tambah Alat!");
            response.put("result",true);
        } catch (NullPointerException e) {
            e.printStackTrace();
            response.put("message","Parameter request ada yang salah!");
            response.put("result",false);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message","Gagal tambah Alat!");
            response.put("result",false);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
    }


}
