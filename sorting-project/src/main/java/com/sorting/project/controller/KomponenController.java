package com.sorting.project.controller;


import com.sorting.project.model.*;
import com.sorting.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping("/komponen")
public class KomponenController {

    @Autowired
    private KomponenService komponenService;

    @Autowired
    ProsesKomponenService prosesKomponenService;

    @Autowired
    AlatService alatService;

    @Autowired
    private ProsesService prosesService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProdukService produkService;

    @RequestMapping("/list")
    ResponseEntity<Map<String,Object>> doList (@RequestBody Map<String, Object> request) {
        List<ProsesKomponen> prosesKomponenList;
        List<Komponen> komponenList = new ArrayList<>();
        Map<String,Object> response = new HashMap<>();
        try {
            String namaProduk = request.get("nama_produk").toString();

            prosesKomponenList = prosesKomponenService.findByProduk(namaProduk);
            for (int i = 0; i<prosesKomponenList.size(); i++) {
                if (i==0) {
                    komponenList.add(prosesKomponenList.get(i).getKomponen());
                } else {
                    if (!komponenList.contains(prosesKomponenList.get(i).getKomponen())){
                        komponenList.add(prosesKomponenList.get(i).getKomponen());
                    }
                }
            }
            response.put("prosesKompList",prosesKomponenList);
            response.put("komponen",komponenList);
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
        Map<String,Object> response = new HashMap<>();

        try {
            Komponen komponen = this.komponenService.findOneById(request.get("id").toString());
            if (komponen.getAktif()) {
                komponen.setAktif(false);
            } else {
                komponen.setAktif(true);
            }
            this.komponenService.save(komponen);
            response.put("message","Berhasil edit data komponen!");
            response.put("result",true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message","Gagal edit data komponen!");
            response.put("result",false);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping("/upload")
    ResponseEntity<Map<String,Object>> doAdd (@RequestBody Map<String, Object> request) {
        Map<String,Object> response = new HashMap<>();
        try {

            List<Map<String,Object>> itemSaved = (List<Map<String,Object>>) request.get("item");
            Produk produk = produkService.findOneById(request.get("produk").toString());
            prosesKomponenService.nonAktifSemuaByProduk(produk.getId());
            List<ProsesKomponen> prosesKompSaved = new ArrayList<>();
            Komponen newKomponen;
            for (int i = 0; i < itemSaved.size(); i++) {
                Boolean baru = true;
                newKomponen =  komponenService.findByNameAndProdukName(itemSaved.get(i).get("namaKomponen").toString(), produk.getNamaProduk());

                if (newKomponen != null) {
                        baru = false;
                        newKomponen.setNamaBagian(itemSaved.get(i).get("namaBagian").toString());
                        newKomponen.setAktif(true);
                        newKomponen.setModifiedBy(userService.findOneById("1"));
                        newKomponen.setModifiedOn(new Date());
                        newKomponen.setKuantitas(itemSaved.get(i).get("kuantitas").toString());
                        newKomponen.setLebar(itemSaved.get(i).get("lebar") != null ? Double.parseDouble(itemSaved.get(i).get("lebar").toString()) : null);
                        newKomponen.setTinggi(itemSaved.get(i).get("tinggi") != null ? Double.parseDouble(itemSaved.get(i).get("tinggi").toString()) : null);
                        newKomponen.setPanjang(itemSaved.get(i).get("panjang") != null ? Double.parseDouble(itemSaved.get(i).get("panjang").toString()): null);
                        newKomponen.setProduk(produk);
                        newKomponen.setNamaKomponen(itemSaved.get(i).get("namaKomponen").toString());
                        newKomponen.setPrioritas(itemSaved.get(i).get("prioritas").toString());
                        this.komponenService.save(newKomponen);
                    }


                if (baru) {
                    newKomponen = new Komponen();
                    newKomponen.setNamaBagian(itemSaved.get(i).get("namaBagian").toString());
                    newKomponen.setAktif(true);
                    newKomponen.setCreatedBy(userService.findOneById("1"));
                    newKomponen.setKuantitas(itemSaved.get(i).get("kuantitas").toString());
                    newKomponen.setLebar(itemSaved.get(i).get("lebar") != null ? Double.parseDouble(itemSaved.get(i).get("lebar").toString()) : null);
                    newKomponen.setTinggi(itemSaved.get(i).get("tinggi") != null ? Double.parseDouble(itemSaved.get(i).get("tinggi").toString()) : null);
                    newKomponen.setPanjang(itemSaved.get(i).get("panjang") != null ? Double.parseDouble(itemSaved.get(i).get("panjang").toString()): null);
                    newKomponen.setProduk(produk);
                    newKomponen.setNamaKomponen(itemSaved.get(i).get("namaKomponen").toString());
                    newKomponen.setPrioritas(itemSaved.get(i).get("prioritas").toString());

                    this.komponenService.save(newKomponen);
                }

                ProsesKomponen prosesKomponen = new ProsesKomponen();

                Integer kuantitas = newKomponen.getProduk().getKuantitas();
                for (int number = 1 ; number <= kuantitas; number++ ) {
                    if (itemSaved.get(i).get("plm") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "PLM",newKomponen, number);
                        if (itemSaved.get(i).get("alatPLM") != null) {
                            Alat alat = alatService.findByNamaAlat(itemSaved.get(i).get("alatPLM").toString());
                            prosesKomponen.setAlat(alat);
                        }
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("et") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "ET",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("sgc") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "SGC",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("bvl") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "BVL",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("bs") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "BS",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("hgc") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "HGC",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("agc") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "AGC",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("stp") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "STP",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("hpp") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "HPP",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("bpb") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "BPB",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }   if (itemSaved.get(i).get("rb") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "RB",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("rd") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "RD",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("td") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "TD",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("hb") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "HB",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("gl") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "GL",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("lb") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "LB",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }  if (itemSaved.get(i).get("gr") != null) {
                        prosesKomponen = checkProsesKomp(itemSaved.get(i) , "GR",newKomponen, number);
                        prosesKompSaved.add(prosesKomponen);
                    }
                }
            }
            if (prosesKompSaved.size() != 0) {
                prosesKomponenService.saveAll(prosesKompSaved);
            }
            response.put("message","Berhasil simpan data komponen!");
            response.put("result",true);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message","Gagal simpan data komponen!");
            response.put("result",false);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    private ProsesKomponen checkProsesKomp (Map<String, Object> item, String proses, Komponen komponen, Integer number) {
        ProsesKomponen newProsesKomp = new ProsesKomponen();
        Boolean baru = true;
        if (prosesKomponenService.checkProsesKomponen(komponen.getId(), proses, number)) {
            baru = false;
            newProsesKomp =this.prosesKomponenService.findOneByKompProcNumb(komponen.getId(), proses, number);
            newProsesKomp.setIsProses(false);
            newProsesKomp.setNomor(number);
            newProsesKomp.setProses(this.prosesService.findOneById(proses));
            newProsesKomp.setKomponen(komponen);
            newProsesKomp.setDurasiProses(Double.parseDouble(item.get(proses.toLowerCase()).toString()));
            newProsesKomp.setAktif(true);
        }

        if (baru) {
            newProsesKomp.setIsProses(false);
            newProsesKomp.setProses(this.prosesService.findOneById(proses));
            newProsesKomp.setKomponen(komponen);
            newProsesKomp.setNomor(number);
            newProsesKomp.setDurasiProses(Double.parseDouble(item.get(proses.toLowerCase()).toString()));
            newProsesKomp.setAktif(true);
        }
        return newProsesKomp;
    }

}
