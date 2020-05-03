/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.controller;

import com.sorting.project.model.*;
import com.sorting.project.model.master.MasterTanggalAlat;
import com.sorting.project.model.master.TanggalLibur;
import com.sorting.project.model.util.DateManipulator;
import com.sorting.project.service.AlatService;
import com.sorting.project.service.AppSettingService;
import com.sorting.project.service.KomponenService;
import com.sorting.project.service.ProsesKomponenService;
import com.sorting.project.service.master.MasterTanggalAlatService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.NoResultException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author PROSIA
 */
@Controller
@RequestMapping("/sorting")
@CrossOrigin(origins = "*")
public class SortingController {

    @Autowired
    private KomponenService komponenService;

    @Autowired
    private ProsesKomponenService prosesKomponenService;

    @Autowired
    private MasterTanggalAlatService mtaService;

    @Autowired
    private AlatService alatService;

    @Autowired
    private AppSettingService appSettingService;

    /*
     *   com.sorting.project.model.util
     */
    private DateManipulator dateManipulator = new DateManipulator();

    @RequestMapping("/backup")
    public ResponseEntity<Map<String, Object>> doBackup() {
        Map<String, Object> result = new HashMap<>();
        try {
           List<Object> responService =  this.prosesKomponenService.saveBackUp();
           if ((boolean) responService.get(0)) {
               result.put("message","Berhasil backup data!");
           } else {
               result.put("message","Gagal backup data!");
           }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result",false);
            result.put("message","Terjadi kesalahan!");
            return  new ResponseEntity<>(result,HttpStatus.EXPECTATION_FAILED);
        }
        result.put("true",true);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @RequestMapping("/restore")
    public ResponseEntity<Map<String, Object>> getBackup() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Object> responService =  this.prosesKomponenService.restore();
            if ((boolean) responService.get(0)) {
                result.put("message",responService);
            } else {
                result.put("message","Gagal backup data!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result",false);
            result.put("message","Terjadi kesalahan!");
            return  new ResponseEntity<>(result,HttpStatus.EXPECTATION_FAILED);
        }
        result.put("true",true);
        return new ResponseEntity<>(result,HttpStatus.OK);
    }

    @RequestMapping("/doSorting")
    public ResponseEntity<Map<String, Object>> doSorting(@RequestBody Map<String, Object> request) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        List<ProsesKomponen> prosesKomponenList = new ArrayList<>();
        Boolean success = true;
        List<ProsesKomponen> listProsesSave = new ArrayList<>();
        List<TanggalLibur> tglLiburList;
        Date minDate = new Date();
        boolean is2shift = true;
        
        try {
            this.refreshMasterAlatDanSort();
            AppSetting app = appSettingService.findById("DOUBLE_SHIFT");
            is2shift = app.getAppValue().equalsIgnoreCase("1") ? true : false;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            result.put("message","Gagal refresh ulang alat!");
            result.put("success", false);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        try {
            prosesKomponenList = this.prosesKomponenService.findCuttingByDeadlinePriorWaktuJumProsNama();
            tglLiburList = this.prosesKomponenService.findAllLibur();
            if (prosesKomponenList.size() == 0) {
                result.put("message","Tidak ada komponen untuk diproses!");
                result.put("success", false);
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("message","Gagal mendapatkan komponen untuk diproses!");
            result.put("success", false);
            return new ResponseEntity<>(result, HttpStatus.OK);

        }

        int i = prosesKomponenList.size() - 1;
        while (i >= 0) {
            prosesKomponenList.get(i).setSortId(i);

            List<ProsesKomponen> listKomponenDiSortir
                    = this.prosesKomponenService.findByProsesAndSortByIdPorses(prosesKomponenList.get(i).getKomponen().getNamaKomponen(), "ASC");

            Date temp = null;
            for (int j = listKomponenDiSortir.size() - 1; j >= 0; j--) {
                ProsesKomponen akanDiAssign = listKomponenDiSortir.get(j);
                List<MasterTanggalAlat> tanggalMaster = new ArrayList<>();
                Alat alat = new Alat();
                if (akanDiAssign.getAlat() != null && akanDiAssign.getProses().getNamaProses().equalsIgnoreCase("PLM")) {
                    if (akanDiAssign.getAlat().getStatus()) {
                        alat = akanDiAssign.getAlat();
                    } else {
                        alat = this.findAvailableAlat(akanDiAssign.getProses().getID());
                    }
                } else {
                    alat = this.findAvailableAlat(akanDiAssign.getProses().getID());
                }

                Long waktuProsesLong = Math.round(akanDiAssign.getDurasiProses() * Double.parseDouble("60"));
                Integer waktuProses = Integer.parseInt(waktuProsesLong.toString());

                waktuProses = akanDiAssign.getKomponen().getProduk().getKuantitas() * waktuProses;

                try {
                    tanggalMaster = this.mtaService.findByNamaAlat(alat.getNamaAlat(), Sort.Direction.DESC.toString());

                    if (tanggalMaster.isEmpty()) {
                        if (i == prosesKomponenList.size() - 1) {
                            if (j == listKomponenDiSortir.size() - 1) {
                                MasterTanggalAlat tanggalDariDeadline = new MasterTanggalAlat();
                                tanggalDariDeadline.setAlat(alat);

                                Date tanggalAkandiAssign = this.dateManipulator.addSeconds(
                                        akanDiAssign.getKomponen().getProduk().getTanggalAkhir(), -waktuProses);

                                tanggalDariDeadline.setTanggalAlat(tanggalAkandiAssign);
                                tanggalMaster.add(tanggalDariDeadline);
                            } else {
                                MasterTanggalAlat tanggalDariDeadline = new MasterTanggalAlat();
                                tanggalDariDeadline.setAlat(alat);

                                Date tanggalAkandiAssign = this.dateManipulator.addSeconds(
                                        listKomponenDiSortir.get(j + 1).getAssignDate(), -waktuProses);

                                tanggalDariDeadline.setTanggalAlat(tanggalAkandiAssign);
                                tanggalMaster.add(tanggalDariDeadline);
                            }
                        } else if (j == listKomponenDiSortir.size() - 1) {
                            MasterTanggalAlat tanggalDariDeadline = new MasterTanggalAlat();
                            tanggalDariDeadline.setAlat(alat);

                            Date tanggalAkandiAssign = this.dateManipulator.addSeconds(
                                    akanDiAssign.getKomponen().getProduk().getTanggalAkhir(), -waktuProses);

                            tanggalDariDeadline.setTanggalAlat(tanggalAkandiAssign);
                            tanggalMaster.add(tanggalDariDeadline);
                        } else {
                            MasterTanggalAlat tanggalDariDeadline = new MasterTanggalAlat();
                            tanggalDariDeadline.setAlat(alat);

                            Date tanggalAkandiAssign = this.dateManipulator.addSeconds(
                                    listKomponenDiSortir.get(j + 1).getAssignDate(), -waktuProses);

                            tanggalDariDeadline.setTanggalAlat(tanggalAkandiAssign);
                            tanggalMaster.add(tanggalDariDeadline);
                        }

                    } else {
                        Date tanggalAkandiAssign = this.dateManipulator.addSeconds(tanggalMaster.get(0).getTanggalAlat(), -waktuProses);

                        if (tanggalAkandiAssign.compareTo(akanDiAssign.getKomponen().getProduk().getTanggalAkhir()) > 0){
                            tanggalAkandiAssign = this.dateManipulator.addSeconds(akanDiAssign.getKomponen().getProduk().getTanggalAkhir(), -waktuProses);
                        }

                        tanggalMaster.get(0).setTanggalAlat(
                                tanggalAkandiAssign);
                    }

                } catch (Exception e) {
                    e.printStackTrace();

                }

                System.out.println("sort id ke : " + i);
                System.out.println("tanggal sebelumnya : " + temp);
                System.out.println("tanggal yang akan diassign : " + tanggalMaster.get(0).getTanggalAlat());
                if (temp != null) {
                    if (tanggalMaster.get(0).getTanggalAlat().compareTo(temp) > 0) {
                        tanggalMaster.get(0).setTanggalAlat(
                                this.dateManipulator.addSeconds(temp, -waktuProses)
                        );

                        akanDiAssign.setAssignDate(
                            this.dateManipulator.addSeconds(temp, -waktuProses)
                        );
                    }
                }

                alat.setTanggalAssign(tanggalMaster.get(0).getTanggalAlat());
                alat.setWorkLoad(alat.getWorkLoad() + waktuProses);

                akanDiAssign = this.loopingSorting(tanggalMaster, akanDiAssign, waktuProses, tglLiburList,is2shift);

                akanDiAssign.setAlat(alat);

                akanDiAssign.setSortId(i);

                akanDiAssign.setIsProses(false);

                temp = tanggalMaster.get(0).getTanggalAlat();
                
                if (i == prosesKomponenList.size() - 1) {
                    minDate = temp;
                } else if (temp.before(minDate)) {
                    minDate = temp;
                }

                this.mtaService.saveOne(tanggalMaster.get(0));
//                listProsesSave.add(akanDiAssign);
//                this.prosesKomponenService.saveOne(akanDiAssign);
                this.alatService.save(alat);
            }

            i--;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date dt = sdf.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());

        if (dt.after(minDate)) {
//            try {
//                this.prosesKomponenService.saveAll(listProsesSave);
//            } catch (Exception e) {
//                e.printStackTrace();
//                success = false;
//                return new ResponseEntity<>(result, HttpStatus.OK);
//            }

            System.out.println("=================" +
                    "WAKTU MULAI SORTING MELEWATI BATAS MAKSIMAL");
            result.put("success",false);
            result.put("message","Batas maksimal sorting adalah " + sdf.format(minDate));
            result.put("jumlah", prosesKomponenList.size());
            result.put("hasil", prosesKomponenList);
            return new ResponseEntity<>(result,HttpStatus.OK);
        }


        List<Object[]> prosesDitarik = this.prosesKomponenService.findSortByKomponenAndProses();

        Map<String, Date> waktuProduk = new HashMap<>();
        for (int z = 0; z < prosesDitarik.size(); z++) {
            List<ProsesKomponen> akanDiTarik = this.prosesKomponenService.findByProsesAndSortByIdPorses(prosesDitarik.get(z)[0].toString(), "");
            for (int j = 0; j < akanDiTarik.size(); j++) {
                Long waktuProsesLong = Math.round(akanDiTarik.get(j).getDurasiProses() * akanDiTarik.get(j).getKomponen().getProduk().getKuantitas() * Double.parseDouble("60"));
                Integer waktuProses = Integer.parseInt(waktuProsesLong.toString());
                if (z == 0 && j == 0){

                    akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));

                    akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));

                } else if (j > 0) {
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null
                    && waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).after(akanDiTarik.get(j - 1).getAssignEnd())) {
                        dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                    } else {
                        dt = akanDiTarik.get(j - 1).getAssignEnd();
                    }

                                       akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));


                    Calendar cat = Calendar.getInstance();
                    cat.setTime(akanDiTarik.get(j).getAssignDate());
                    Calendar catPembanding = cat;
                    catPembanding.set(Calendar.AM_PM,Calendar.AM);
                    catPembanding.set(Calendar.HOUR, 3);
                   if (this.cekTanggal(tglLiburList, cat.getTime())) {

                        while (this.cekTanggal(tglLiburList, cat.getTime())) {
                            cat.add(Calendar.DATE, 1);
                        }
                        cat.set(Calendar.AM_PM,Calendar.AM);
                        cat.set(Calendar.HOUR,8);
                        cat.set(Calendar.MINUTE,0);
                        cat.set(Calendar.SECOND,0);

                        akanDiTarik.get(j).setAssignDate(cat.getTime());
                        akanDiTarik.get(j).setAssignEnd(cat.getTime());
                        akanDiTarik.get(j).setAssignEnd(
                                this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), waktuProses));
                    }


                    akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));


                } else if (z != 0 && j == 0) {
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null) {
                        dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                    } else {
                        dt = sdf.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());
                    }
                    akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));

                    Calendar cat = Calendar.getInstance();
                    cat.setTime(akanDiTarik.get(j).getAssignDate());
                    Calendar catPembanding = cat;
                    catPembanding.set(Calendar.AM_PM,Calendar.AM);
                    catPembanding.set(Calendar.HOUR, 3);
                    if (this.cekTanggal(tglLiburList, cat.getTime())) {

                        while (this.cekTanggal(tglLiburList, cat.getTime())) {
                            cat.add(Calendar.DATE, 1);
                        }

                        cat.set(Calendar.AM_PM,Calendar.AM);
                        cat.set(Calendar.HOUR,8);
                        cat.set(Calendar.MINUTE,0);
                        cat.set(Calendar.SECOND,0);

                        akanDiTarik.get(j).setAssignDate(cat.getTime());
                        akanDiTarik.get(j).setAssignEnd(cat.getTime());
                        akanDiTarik.get(j).setAssignEnd(
                                this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), waktuProses));
                    }

                    akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));

                }
                waktuProduk.put(akanDiTarik.get(j).getAlat().getNamaAlat(), akanDiTarik.get(j).getAssignEnd());


            }

            this.prosesKomponenService.saveAll(akanDiTarik);

        }

        System.out.println("=====================\n"
                + "     JOB DONE \n"
                + "=====================");

        result.put("success", success);
        result.put("message","Sukses melakukan sorting!");
        result.put("jumlah", prosesKomponenList.size());
        result.put("hasil", prosesKomponenList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/do-nest-sorting")
    public ResponseEntity<Map<String, Object>> doNestSorting(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resource = new ArrayList<>();
        List<Map<String, Object>> event = new ArrayList<>();
        boolean is2shift = true;

           try {
            List<Object[]> prosesDitarik = this.prosesKomponenService.findSortByNest();
            List<TanggalLibur> tglLiburList = this.prosesKomponenService.findAllLibur();
               AppSetting app = appSettingService.findById("DOUBLE_SHIFT");
               is2shift = app.getAppValue() == "1" ? true : false;
            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            dt = sdf.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());
            Date temp = new Date();
               Map<String, Date> waktuProduk = new HashMap<>();
               for (int z = 0; z < prosesDitarik.size(); z++) {
                   List<ProsesKomponen> akanDiTarik = this.prosesKomponenService.findByProsesAndSortByIdPorses(prosesDitarik.get(z)[0].toString(), "");
                   for (int j = 0; j < akanDiTarik.size(); j++) {
                       Long waktuProsesLong = Math.round(akanDiTarik.get(j).getDurasiProses() * akanDiTarik.get(j).getKomponen().getProduk().getKuantitas() * Double.parseDouble("60"));
                       Integer waktuProses = Integer.parseInt(waktuProsesLong.toString());
                       if (z == 0 && j == 0){

                           akanDiTarik.get(j).setAssignDate(dt);
                           akanDiTarik.get(j).setAssignEnd(
                                   this.dateManipulator.addSeconds(dt, waktuProses));

                           akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));

                       } else if (j > 0) {
                           if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null
                                   && waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).after(akanDiTarik.get(j - 1).getAssignEnd())) {
                               dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                           } else {
                               dt = akanDiTarik.get(j - 1).getAssignEnd();
                           }

                           akanDiTarik.get(j).setAssignDate(dt);
                           akanDiTarik.get(j).setAssignEnd(
                                   this.dateManipulator.addSeconds(dt, waktuProses));


                           Calendar cat = Calendar.getInstance();
                           cat.setTime(akanDiTarik.get(j).getAssignDate());
                           Calendar catPembanding = cat;
                           catPembanding.set(Calendar.AM_PM,Calendar.AM);
                           catPembanding.set(Calendar.HOUR, 3);
                           if (this.cekTanggal(tglLiburList, cat.getTime())) {

                               while (this.cekTanggal(tglLiburList, cat.getTime())) {
                                   cat.add(Calendar.DATE, 1);
                               }
                               cat.set(Calendar.AM_PM,Calendar.AM);
                               cat.set(Calendar.HOUR,8);
                               cat.set(Calendar.MINUTE,0);
                               cat.set(Calendar.SECOND,0);

                               akanDiTarik.get(j).setAssignDate(cat.getTime());
                               akanDiTarik.get(j).setAssignEnd(cat.getTime());
                               akanDiTarik.get(j).setAssignEnd(
                                       this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), waktuProses));
                           }


                           akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));


                       } else if (z != 0 && j == 0) {
                           if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null) {
                               dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                           } else {
                               dt = sdf.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());
                           }
                           akanDiTarik.get(j).setAssignDate(dt);
                           akanDiTarik.get(j).setAssignEnd(
                                   this.dateManipulator.addSeconds(dt, waktuProses));

                           Calendar cat = Calendar.getInstance();
                           cat.setTime(akanDiTarik.get(j).getAssignDate());
                           Calendar catPembanding = cat;
                           catPembanding.set(Calendar.AM_PM,Calendar.AM);
                           catPembanding.set(Calendar.HOUR, 3);
                           if (this.cekTanggal(tglLiburList, cat.getTime())) {

                               while (this.cekTanggal(tglLiburList, cat.getTime())) {
                                   cat.add(Calendar.DATE, 1);
                               }

                               cat.set(Calendar.AM_PM,Calendar.AM);
                               cat.set(Calendar.HOUR,8);
                               cat.set(Calendar.MINUTE,0);
                               cat.set(Calendar.SECOND,0);

                               akanDiTarik.get(j).setAssignDate(cat.getTime());
                               akanDiTarik.get(j).setAssignEnd(cat.getTime());
                               akanDiTarik.get(j).setAssignEnd(
                                       this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), waktuProses));
                           }

                           akanDiTarik.set(j,this.checkAssignEnd(akanDiTarik.get(j),is2shift));

                       }
                       waktuProduk.put(akanDiTarik.get(j).getAlat().getNamaAlat(), akanDiTarik.get(j).getAssignEnd());


                   }

                   this.prosesKomponenService.saveAll(akanDiTarik);

               }

               result.put("message", "Berhasil melakukan Nest Sorting!");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success",false);
            result.put("message","Gagal melakukan Nest Sorting!");
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
        result.put("success", true);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }
    
    @RequestMapping("/getSorting")
    public ResponseEntity<Map<String, Object>> getSorting(@RequestBody Map<String, Object> request) {
         Map<String, Object> result = new HashMap<>();
         List<Map<String, Object>> resource = new ArrayList<>();
         List<Map<String, Object>> event = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
             Boolean status = (Boolean) request.get("status");
             /*Date start = new SimpleDateFormat( "yyyy-MM-dd").parse(request.get("start").toString());
             Date end = new SimpleDateFormat( "yyyy-MM-dd").parse(request.get("end").toString());*/
             String start =request.get("start").toString();
             String end = request.get("end").toString();
             List<Object[]> produk = prosesKomponenService.findProdukDistinct(status, start, end);
             List<ProsesKomponen> item = prosesKomponenService.findByHasilSorting(status, start, end);
             int id = 1;
            ProsesKomponen temp = null;
             for (int i = 0; i < produk.size(); i++) {
                 List<Map<String, Object>> children = new ArrayList<>();
                 for (ProsesKomponen prosesKomp : item) {
                     if (prosesKomp.getKomponen().getProduk().getNamaProduk().equalsIgnoreCase(produk.get(i)[0].toString())){
                         if ((temp != null && !temp.getKomponen().getNamaKomponen().equalsIgnoreCase(
                                 prosesKomp.getKomponen().getNamaKomponen())) || id == 1 ) {
                             Map<String, Object> newItem = new HashMap<>();
                             newItem.put("name",prosesKomp.getKomponen().getNamaKomponen());
                             newItem.put("id",prosesKomp.getKomponen().getNamaKomponen());
                             children.add(newItem);
                         }


                         Map<String, Object> newEvent = new HashMap<>();
                         newEvent.put("resource",prosesKomp.getKomponen().getNamaKomponen());
                         newEvent.put("id",id);
                         newEvent.put("start",sdf.format(prosesKomp.getAssignDate()));
                         newEvent.put("end",sdf.format(prosesKomp.getAssignEnd()));
                         newEvent.put("text",prosesKomp.getAlat().getNamaAlat());
                         newEvent.put("color","#e69138");
                         event.add(newEvent);

                         id++;
                         temp = prosesKomp;

                     }
                 }
                 Map<String, Object> newResource = new HashMap<>();
                 newResource.put("children",children);
                 newResource.put("name",produk.get(i)[0]);
                 newResource.put("id",produk.get(i)[0]);
                 newResource.put("expanded",true);
                 resource.add(newResource);
             }

             result.put("hasilSorting", item);
             result.put("resource", resource);
             result.put("events", event);
             result.put("message", "Berhasil mendapatkan hasil sorting!");
         } catch (Exception e) {
             e.printStackTrace();
             result.put("success",false);
             result.put("message","Gagal mendapatkan hasil sorting!");
             return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
         }
         result.put("success", true);
         return new ResponseEntity<>(result, HttpStatus.OK);
 
    }

    private Boolean cekTanggal (List<TanggalLibur> tglLiburList, Date tglKomponen) throws ParseException {
        Calendar cat = Calendar.getInstance();
        cat.setTime(tglKomponen);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String tglKompString = sdf.format(tglKomponen);
        Date tglKomp00 = sdf.parse(tglKompString);
        for (int i = 0; i <  tglLiburList.size(); i++) {
            if (tglKomp00.compareTo(tglLiburList.get(i).getTanggalLibur()) == 0 || cat.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
             cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                return true;
            }
        }
        return false;
    }

    private Alat findAvailableAlat(String prosesId) {

        List<Alat> listAlat = this.alatService.findOneByMasterDescIsNull(prosesId);

        System.out.println("BANYAKNYA ALAT " + prosesId + " adalah " + listAlat.size());

        if (listAlat.isEmpty()) {
            listAlat = this.alatService.findOneByMasterDescIsNotNull(prosesId);
        }

        Alat alat = listAlat.get(0);
        return alat;
    }

    private void refreshMasterAlatDanSort() {
        this.mtaService.refreshMta();
//        this.prosesKomponenService.refreshProsesKomponen();
        this.alatService.refreshAlat();
    }

    private ProsesKomponen loopingSorting(List<MasterTanggalAlat> tanggalMaster, ProsesKomponen akanDiAssign,
                                          int waktuProses, List<TanggalLibur> tglLiburList, Boolean is2shift) throws ParseException {
        Calendar cat = Calendar.getInstance();
        cat.setTime(tanggalMaster.get(0).getTanggalAlat());

        // **AWAL JAM ISTIRAHAT
        Calendar jamAwalIstirahat = Calendar.getInstance();
        jamAwalIstirahat.setTime(cat.getTime());
        jamAwalIstirahat.set(Calendar.AM_PM, Calendar.AM);
        jamAwalIstirahat.set(Calendar.HOUR, 11);
        jamAwalIstirahat.set(Calendar.MINUTE, 45);
        jamAwalIstirahat.set(Calendar.SECOND, 00);

        // **AKHIR JAM ISTIRAHAT
        Calendar jamAkhirIstirahat = Calendar.getInstance();
        jamAkhirIstirahat.setTime(cat.getTime());
        jamAkhirIstirahat.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirIstirahat.set(Calendar.HOUR, 0);
        jamAkhirIstirahat.set(Calendar.MINUTE, 45);
        jamAkhirIstirahat.set(Calendar.SECOND, 00);

        // ** MULAI JAM KERJA SHIFT-1
        Calendar jamMulaiKerja = Calendar.getInstance();
        jamMulaiKerja.setTime(cat.getTime());
        jamMulaiKerja.set(Calendar.AM_PM, Calendar.AM);
        jamMulaiKerja.set(Calendar.HOUR, 8);
        jamMulaiKerja.set(Calendar.MINUTE, 00);
        jamMulaiKerja.set(Calendar.SECOND, 00);

        //** SELESAI JAM KERJA SHIFT-1
        Calendar jamAkhirKerja = Calendar.getInstance();
        jamAkhirKerja.setTime(cat.getTime());
        jamAkhirKerja.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirKerja.set(Calendar.HOUR, 4);
        jamAkhirKerja.set(Calendar.MINUTE, 00);
        jamAkhirKerja.set(Calendar.SECOND, 00);

        //**SHIFT 2**//
        Calendar shift2JamMulai = Calendar.getInstance();
        shift2JamMulai.setTime(cat.getTime());
        shift2JamMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2JamMulai.set(Calendar.HOUR, 7);
        shift2JamMulai.set(Calendar.MINUTE, 30);
        shift2JamMulai.set(Calendar.SECOND, 00);

        Calendar shift2JamAkhir = Calendar.getInstance();
        shift2JamAkhir.setTime(cat.getTime());
        shift2JamAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2JamAkhir.set(Calendar.HOUR, 3);
        shift2JamAkhir.set(Calendar.MINUTE, 30);
        shift2JamAkhir.set(Calendar.SECOND, 00);

        //**CFB = Coffee Break
        Calendar shift2CFBAkhir = Calendar.getInstance();
        shift2CFBAkhir.setTime(cat.getTime());
        shift2CFBAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBAkhir.set(Calendar.HOUR, 01);
        shift2CFBAkhir.set(Calendar.MINUTE, 15);
        shift2CFBAkhir.set(Calendar.SECOND, 00);

        Calendar shift2CFBMulai = Calendar.getInstance();
        shift2CFBMulai.setTime(cat.getTime());
        shift2CFBMulai.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBMulai.set(Calendar.HOUR, 01);
        shift2CFBMulai.set(Calendar.MINUTE, 00);
        shift2CFBMulai.set(Calendar.SECOND, 00);

        //**Istirahat shift2
        Calendar shift2IstirhahatAkhir = Calendar.getInstance();
        shift2IstirhahatAkhir.setTime(cat.getTime());
        shift2IstirhahatAkhir.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirhahatAkhir.set(Calendar.HOUR, 11);
        shift2IstirhahatAkhir.set(Calendar.MINUTE, 00);
        shift2IstirhahatAkhir.set(Calendar.SECOND, 00);

        Calendar shift2IstirahatMulai = Calendar.getInstance();
        shift2IstirahatMulai.setTime(cat.getTime());
        shift2IstirahatMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirahatMulai.set(Calendar.HOUR, 10);
        shift2IstirahatMulai.set(Calendar.MINUTE, 00);
        shift2IstirahatMulai.set(Calendar.SECOND, 00);

//                System.out.println(jamAkhirIstirahat.getTime());
//                System.out.println(jamAwalIstirahat.getTime());
//                System.out.println(jamMulaiKerja.getTime());
//                System.out.println(jamAkhirKerja.getTime());
//                System.out.println(shift2JamMulai.getTime());
//                System.out.println(shift2JamAkhir.getTime());
//                System.out.println(shift2CFBMulai.getTime());
//                System.out.println(shift2CFBAkhir.getTime());
//                System.out.println(shift2IstirahatMulai.getTime());
//                System.out.println(shift2IstirhahatAkhir.getTime());
//                System.out.println(tanggalMaster.get(0).getTanggalAlat());
        akanDiAssign.setAssignDate(tanggalMaster.get(0).getTanggalAlat());
        akanDiAssign.setAssignEnd(this.dateManipulator.addSeconds(tanggalMaster.get(0).getTanggalAlat(), waktuProses)
        );

        /*
                 00  :   jam kerja yang valid
                 01  :   diantara shift2akhir dengan shift1awal
                 02  :   diantara jam istirahat
                 03  :   diantara shift1akhir dengan shift2awal
                 04  :   diantara shift2 Coffee Break
                 05  :   diantara jam makan shift2
                 06  :   hari libur
         */
        String status = "";

        if (cat.getTime().compareTo(jamAkhirIstirahat.getTime()) < 0
                && cat.getTime().compareTo(jamAwalIstirahat.getTime()) > 0) {
            status = "02";
        } else if (cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0
                && cat.getTime().compareTo(shift2JamAkhir.getTime()) > 0  && is2shift) {
            status = "01";
        } else if (cat.getTime().compareTo(shift2JamMulai.getTime()) < 0
                && cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && is2shift) {
            status = "03";
        } else if (cat.getTime().compareTo(shift2CFBAkhir.getTime()) < 0
                && cat.getTime().compareTo(shift2CFBMulai.getTime()) > 0  && is2shift) {
            status = "04";
        } else if (cat.getTime().compareTo(shift2IstirhahatAkhir.getTime()) < 0
                && cat.getTime().compareTo(shift2IstirahatMulai.getTime()) > 0  && is2shift) {
            status = "05";
        } else if (this.cekTanggal(tglLiburList,cat.getTime()) ||
                (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)){
            status = "06";
        }  else if (cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && !is2shift) {
            status = "07";
        }
        /*else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
            status = "06";
        } */
        else {
            status = "00";
        }

        while (!status.equals("00")) {

            Date jamMulaiKembali;
            Long diffSeconds;
            Long diffMinutes;
            Long diffHours;
            Long diff;

            switch (status) {
                case "01":
                    diff = cat.getTime().getTime() - jamMulaiKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2JamAkhir.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "02":
                    diff = cat.getTime().getTime() - jamAkhirIstirahat.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan saat istirahat pada sort ke " + i + diffHours + ":" + diffMinutes + ":" + diffSeconds);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamAwalIstirahat.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "03":
                    diff = cat.getTime().getTime() - shift2JamMulai.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamAkhirKerja.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "04":
                    diff = cat.getTime().getTime() - shift2CFBAkhir.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2CFBMulai.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "05":
                    diff = cat.getTime().getTime() - shift2IstirhahatAkhir.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2IstirahatMulai.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "06":
                    diff = cat.getTime().getTime() - jamMulaiKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    /*if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -1);
                    } else {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -2);
                    }*/

                    jamMulaiKembali = shift2JamAkhir.getTime();
                    int amountDiff = 0;
                    while (cekTanggal(tglLiburList, cat.getTime())) {
                        cat.setTime(this.dateManipulator.addDays(cat.getTime(), -1));
                        amountDiff++;
                    }

                    jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -amountDiff);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKembali, diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
                case "07" :
                    diff = cat.getTime().getTime() - jamMulaiKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    cat.setTime(this.dateManipulator.addDays(cat.getTime(), -1));
                    jamAkhirKerja.add(Calendar.DATE, -1);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamAkhirKerja.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggalMaster.get(0).setTanggalAlat(jamMulaiKembali);
                    break;
            }

            cat = Calendar.getInstance();
            cat.setTime(tanggalMaster.get(0).getTanggalAlat());

            // **AWAL JAM ISTIRAHAT
            jamAwalIstirahat.setTime(cat.getTime());
            jamAwalIstirahat.set(Calendar.AM_PM, Calendar.AM);
            jamAwalIstirahat.set(Calendar.HOUR, 11);
            jamAwalIstirahat.set(Calendar.MINUTE, 45);
            jamAwalIstirahat.set(Calendar.SECOND, 00);

            // **AKHIR JAM ISTIRAHAT
            jamAkhirIstirahat.setTime(cat.getTime());
            jamAkhirIstirahat.set(Calendar.AM_PM, Calendar.PM);
            jamAkhirIstirahat.set(Calendar.HOUR, 0);
            jamAkhirIstirahat.set(Calendar.MINUTE, 45);
            jamAkhirIstirahat.set(Calendar.SECOND, 00);

            // ** MULAI JAM KERJA SHIFT-1
            jamMulaiKerja.setTime(cat.getTime());
            jamMulaiKerja.set(Calendar.AM_PM, Calendar.AM);
            jamMulaiKerja.set(Calendar.HOUR, 8);
            jamMulaiKerja.set(Calendar.MINUTE, 00);
            jamMulaiKerja.set(Calendar.SECOND, 00);

            //** SELESAI JAM KERJA SHIFT-1
            jamAkhirKerja.setTime(cat.getTime());
            jamAkhirKerja.set(Calendar.AM_PM, Calendar.PM);
            jamAkhirKerja.set(Calendar.HOUR, 4);
            jamAkhirKerja.set(Calendar.MINUTE, 00);
            jamAkhirKerja.set(Calendar.SECOND, 00);

            //**SHIFT 2**//
            shift2JamMulai.setTime(cat.getTime());
            shift2JamMulai.set(Calendar.AM_PM, Calendar.PM);
            shift2JamMulai.set(Calendar.HOUR, 7);
            shift2JamMulai.set(Calendar.MINUTE, 30);
            shift2JamMulai.set(Calendar.SECOND, 00);

            shift2JamAkhir.setTime(cat.getTime());
            shift2JamAkhir.set(Calendar.AM_PM, Calendar.AM);
            shift2JamAkhir.set(Calendar.HOUR, 3);
            shift2JamAkhir.set(Calendar.MINUTE, 30);
            shift2JamAkhir.set(Calendar.SECOND, 00);

            //**CFB = Coffee Break
            shift2CFBAkhir.setTime(cat.getTime());
            shift2CFBAkhir.set(Calendar.AM_PM, Calendar.AM);
            shift2CFBAkhir.set(Calendar.HOUR, 01);
            shift2CFBAkhir.set(Calendar.MINUTE, 15);
            shift2CFBAkhir.set(Calendar.SECOND, 00);

            shift2CFBMulai.setTime(cat.getTime());
            shift2CFBMulai.set(Calendar.AM_PM, Calendar.AM);
            shift2CFBMulai.set(Calendar.HOUR, 01);
            shift2CFBMulai.set(Calendar.MINUTE, 00);
            shift2CFBMulai.set(Calendar.SECOND, 00);

            //**Istirahat shift2
            shift2IstirhahatAkhir.setTime(cat.getTime());
            shift2IstirhahatAkhir.set(Calendar.AM_PM, Calendar.PM);
            shift2IstirhahatAkhir.set(Calendar.HOUR, 11);
            shift2IstirhahatAkhir.set(Calendar.MINUTE, 00);
            shift2IstirhahatAkhir.set(Calendar.SECOND, 00);

            shift2IstirahatMulai.setTime(cat.getTime());
            shift2IstirahatMulai.set(Calendar.AM_PM, Calendar.PM);
            shift2IstirahatMulai.set(Calendar.HOUR, 10);
            shift2IstirahatMulai.set(Calendar.MINUTE, 00);
            shift2IstirahatMulai.set(Calendar.SECOND, 00);

//                    System.out.println(jamAkhirIstirahat.getTime());
//                    System.out.println(jamAwalIstirahat.getTime());
//                    System.out.println(jamMulaiKerja.getTime());
//                    System.out.println(tanggalMaster.get(0).getTanggalAlat());
            akanDiAssign.setAssignDate(tanggalMaster.get(0).getTanggalAlat());
            akanDiAssign.setAssignEnd(this.dateManipulator.addSeconds(tanggalMaster.get(0).getTanggalAlat(), waktuProses)
            );

            /*
                     00  :   diantara jam berangkat dan sebelum istirahat ATAU setelah jam istirahat dan sebelum pulang
                     01  :   diantara shift2akhir dengan shift1awal
                     02  :   diantara jam istirahat
                     03  :   diantara shift1akhir dengan shift2awal
                     04  :   diantara shift2 Coffee Break
                     05  :   diantara jam makan shift2
                
             */
            if (cat.getTime().compareTo(jamAkhirIstirahat.getTime()) < 0
                    && cat.getTime().compareTo(jamAwalIstirahat.getTime()) > 0) {
                status = "02";
            } else if (cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0
                    && cat.getTime().compareTo(shift2JamAkhir.getTime()) > 0) {
                status = "01";
            } else if (cat.getTime().compareTo(shift2JamMulai.getTime()) < 0
                    && cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0) {
                status = "03";
            } else if (cat.getTime().compareTo(shift2CFBAkhir.getTime()) < 0
                    && cat.getTime().compareTo(shift2CFBMulai.getTime()) > 0) {
                status = "04";
            } else if (cat.getTime().compareTo(shift2IstirhahatAkhir.getTime()) < 0
                    && cat.getTime().compareTo(shift2IstirahatMulai.getTime()) > 0) {
                status = "05";
            }else if (this.cekTanggal(tglLiburList,cat.getTime()) ||
                    (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)){
                status = "06";
            }  else if (cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && !is2shift) {
                status = "07";
            }
        /*else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
            status = "06";
        } */ else {
                status = "00";
            }

        }

        return akanDiAssign;
    }

    private ProsesKomponen checkAssignEnd(ProsesKomponen akanDiAssign, Boolean is2shift) throws ParseException {
        Calendar cat = Calendar.getInstance();
        cat.setTime(akanDiAssign.getAssignEnd());

        // **AWAL JAM ISTIRAHAT
        Calendar jamAwalIstirahat = Calendar.getInstance();
        jamAwalIstirahat.setTime(cat.getTime());
        jamAwalIstirahat.set(Calendar.AM_PM, Calendar.AM);
        jamAwalIstirahat.set(Calendar.HOUR, 11);
        jamAwalIstirahat.set(Calendar.MINUTE, 45);
        jamAwalIstirahat.set(Calendar.SECOND, 00);

        // **AKHIR JAM ISTIRAHAT
        Calendar jamAkhirIstirahat = Calendar.getInstance();
        jamAkhirIstirahat.setTime(cat.getTime());
        jamAkhirIstirahat.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirIstirahat.set(Calendar.HOUR, 0);
        jamAkhirIstirahat.set(Calendar.MINUTE, 45);
        jamAkhirIstirahat.set(Calendar.SECOND, 00);

        // ** MULAI JAM KERJA SHIFT-1
        Calendar jamMulaiKerja = Calendar.getInstance();
        jamMulaiKerja.setTime(cat.getTime());
        jamMulaiKerja.set(Calendar.AM_PM, Calendar.AM);
        jamMulaiKerja.set(Calendar.HOUR, 8);
        jamMulaiKerja.set(Calendar.MINUTE, 00);
        jamMulaiKerja.set(Calendar.SECOND, 00);

        //** SELESAI JAM KERJA SHIFT-1
        Calendar jamAkhirKerja = Calendar.getInstance();
        jamAkhirKerja.setTime(cat.getTime());
        jamAkhirKerja.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirKerja.set(Calendar.HOUR, 4);
        jamAkhirKerja.set(Calendar.MINUTE, 00);
        jamAkhirKerja.set(Calendar.SECOND, 00);

        //**SHIFT 2**//
        Calendar shift2JamMulai = Calendar.getInstance();
        shift2JamMulai.setTime(cat.getTime());
        shift2JamMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2JamMulai.set(Calendar.HOUR, 7);
        shift2JamMulai.set(Calendar.MINUTE, 30);
        shift2JamMulai.set(Calendar.SECOND, 00);

        Calendar shift2JamAkhir = Calendar.getInstance();
        shift2JamAkhir.setTime(cat.getTime());
        shift2JamAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2JamAkhir.set(Calendar.HOUR, 3);
        shift2JamAkhir.set(Calendar.MINUTE, 30);
        shift2JamAkhir.set(Calendar.SECOND, 00);

        //**CFB = Coffee Break
        Calendar shift2CFBAkhir = Calendar.getInstance();
        shift2CFBAkhir.setTime(cat.getTime());
        shift2CFBAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBAkhir.set(Calendar.HOUR, 01);
        shift2CFBAkhir.set(Calendar.MINUTE, 15);
        shift2CFBAkhir.set(Calendar.SECOND, 00);

        Calendar shift2CFBMulai = Calendar.getInstance();
        shift2CFBMulai.setTime(cat.getTime());
        shift2CFBMulai.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBMulai.set(Calendar.HOUR, 01);
        shift2CFBMulai.set(Calendar.MINUTE, 00);
        shift2CFBMulai.set(Calendar.SECOND, 00);

        //**Istirahat shift2
        Calendar shift2IstirhahatAkhir = Calendar.getInstance();
        shift2IstirhahatAkhir.setTime(cat.getTime());
        shift2IstirhahatAkhir.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirhahatAkhir.set(Calendar.HOUR, 11);
        shift2IstirhahatAkhir.set(Calendar.MINUTE, 00);
        shift2IstirhahatAkhir.set(Calendar.SECOND, 00);

        Calendar shift2IstirahatMulai = Calendar.getInstance();
        shift2IstirahatMulai.setTime(cat.getTime());
        shift2IstirahatMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirahatMulai.set(Calendar.HOUR, 10);
        shift2IstirahatMulai.set(Calendar.MINUTE, 00);
        shift2IstirahatMulai.set(Calendar.SECOND, 00);

        /*
                 00  :   jam kerja yang valid
                 01  :   diantara shift2akhir dengan shift1awal
                 02  :   diantara jam istirahat
                 03  :   diantara shift1akhir dengan shift2awal
                 04  :   diantara shift2 Coffee Break
                 05  :   diantara jam makan shift2
                 06  :   hari libur
         */
        String status = "";

        if (cat.getTime().compareTo(jamAkhirIstirahat.getTime()) < 0
                && cat.getTime().compareTo(jamAwalIstirahat.getTime()) > 0) {
            status = "02";
        } else if (cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0
                && cat.getTime().compareTo(shift2JamAkhir.getTime()) > 0 && is2shift) {
            status = "01";
        } else if (cat.getTime().compareTo(shift2JamMulai.getTime()) < 0
                && cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && is2shift) {
            status = "03";
        } else if (cat.getTime().compareTo(shift2CFBAkhir.getTime()) < 0
                && cat.getTime().compareTo(shift2CFBMulai.getTime()) > 0 && is2shift) {
            status = "04";
        } else if (cat.getTime().compareTo(shift2IstirhahatAkhir.getTime()) < 0
                && cat.getTime().compareTo(shift2IstirahatMulai.getTime()) > 0 && is2shift) {
            status = "05";
        }  else if (cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && !is2shift) {
            status = "07";
        }
//        else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
//                || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
//                && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
//            status = "06";
//        }
        else {
            status = "00";
        }

        while (!status.equals("00")) {

            Date jamMulaiKembali;
            Long diffSeconds;
            Long diffMinutes;
            Long diffHours;
            Long diff;

            switch (status) {
                case "01":
                    diff = cat.getTime().getTime() - shift2JamAkhir.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKerja.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "02":
                    diff = cat.getTime().getTime() - jamAwalIstirahat.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan saat istirahat pada sort ke " + i + diffHours + ":" + diffMinutes + ":" + diffSeconds);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamAkhirIstirahat.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "03":
                    diff = cat.getTime().getTime() - jamAkhirKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2JamMulai.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "04":
                    diff = cat.getTime().getTime() - shift2CFBMulai.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2CFBAkhir.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "05":
                    diff = cat.getTime().getTime() - shift2IstirahatMulai.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2IstirhahatAkhir.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "06":
                    diff = cat.getTime().getTime() - jamMulaiKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), 1);
                    } else {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), 2);
                    }
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKembali, diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
                case "07":
                    diff = cat.getTime().getTime() - jamAkhirKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    cat.setTime(this.dateManipulator.addDays(cat.getTime(), 1));
                    jamMulaiKerja.add(Calendar.DATE, 1);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKerja.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignEnd(jamMulaiKembali);
                    break;
            }

            cat = Calendar.getInstance();
            cat.setTime(akanDiAssign.getAssignEnd());

            // **AWAL JAM ISTIRAHAT
            jamAwalIstirahat.setTime(cat.getTime());
            jamAwalIstirahat.set(Calendar.AM_PM, Calendar.AM);
            jamAwalIstirahat.set(Calendar.HOUR, 11);
            jamAwalIstirahat.set(Calendar.MINUTE, 45);
            jamAwalIstirahat.set(Calendar.SECOND, 00);

            // **AKHIR JAM ISTIRAHAT
            jamAkhirIstirahat.setTime(cat.getTime());
            jamAkhirIstirahat.set(Calendar.AM_PM, Calendar.PM);
            jamAkhirIstirahat.set(Calendar.HOUR, 0);
            jamAkhirIstirahat.set(Calendar.MINUTE, 45);
            jamAkhirIstirahat.set(Calendar.SECOND, 00);

            // ** MULAI JAM KERJA SHIFT-1
            jamMulaiKerja.setTime(cat.getTime());
            jamMulaiKerja.set(Calendar.AM_PM, Calendar.AM);
            jamMulaiKerja.set(Calendar.HOUR, 8);
            jamMulaiKerja.set(Calendar.MINUTE, 00);
            jamMulaiKerja.set(Calendar.SECOND, 00);

            //** SELESAI JAM KERJA SHIFT-1
            jamAkhirKerja.setTime(cat.getTime());
            jamAkhirKerja.set(Calendar.AM_PM, Calendar.PM);
            jamAkhirKerja.set(Calendar.HOUR, 4);
            jamAkhirKerja.set(Calendar.MINUTE, 00);
            jamAkhirKerja.set(Calendar.SECOND, 00);

            //**SHIFT 2**//
            shift2JamMulai.setTime(cat.getTime());
            shift2JamMulai.set(Calendar.AM_PM, Calendar.PM);
            shift2JamMulai.set(Calendar.HOUR, 7);
            shift2JamMulai.set(Calendar.MINUTE, 30);
            shift2JamMulai.set(Calendar.SECOND, 00);

            shift2JamAkhir.setTime(cat.getTime());
            shift2JamAkhir.set(Calendar.AM_PM, Calendar.AM);
            shift2JamAkhir.set(Calendar.HOUR, 3);
            shift2JamAkhir.set(Calendar.MINUTE, 30);
            shift2JamAkhir.set(Calendar.SECOND, 00);

            //**CFB = Coffee Break
            shift2CFBAkhir.setTime(cat.getTime());
            shift2CFBAkhir.set(Calendar.AM_PM, Calendar.AM);
            shift2CFBAkhir.set(Calendar.HOUR, 01);
            shift2CFBAkhir.set(Calendar.MINUTE, 15);
            shift2CFBAkhir.set(Calendar.SECOND, 00);

            shift2CFBMulai.setTime(cat.getTime());
            shift2CFBMulai.set(Calendar.AM_PM, Calendar.AM);
            shift2CFBMulai.set(Calendar.HOUR, 01);
            shift2CFBMulai.set(Calendar.MINUTE, 00);
            shift2CFBMulai.set(Calendar.SECOND, 00);

            //**Istirahat shift2
            shift2IstirhahatAkhir.setTime(cat.getTime());
            shift2IstirhahatAkhir.set(Calendar.AM_PM, Calendar.PM);
            shift2IstirhahatAkhir.set(Calendar.HOUR, 11);
            shift2IstirhahatAkhir.set(Calendar.MINUTE, 00);
            shift2IstirhahatAkhir.set(Calendar.SECOND, 00);

            shift2IstirahatMulai.setTime(cat.getTime());
            shift2IstirahatMulai.set(Calendar.AM_PM, Calendar.PM);
            shift2IstirahatMulai.set(Calendar.HOUR, 10);
            shift2IstirahatMulai.set(Calendar.MINUTE, 00);
            shift2IstirahatMulai.set(Calendar.SECOND, 00);

            /*
                     00  :   diantara jam berangkat dan sebelum istirahat ATAU setelah jam istirahat dan sebelum pulang
                     01  :   diantara shift2akhir dengan shift1awal
                     02  :   diantara jam istirahat
                     03  :   diantara shift1akhir dengan shift2awal
                     04  :   diantara shift2 Coffee Break
                     05  :   diantara jam makan shift2

             */
            if (cat.getTime().compareTo(jamAkhirIstirahat.getTime()) < 0
                    && cat.getTime().compareTo(jamAwalIstirahat.getTime()) > 0) {
                status = "02";
            } else if (cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0
                    && cat.getTime().compareTo(shift2JamAkhir.getTime()) > 0) {
                status = "01";
            } else if (cat.getTime().compareTo(shift2JamMulai.getTime()) < 0
                    && cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && is2shift) {
                status = "03";
            } else if (cat.getTime().compareTo(shift2CFBAkhir.getTime()) < 0
                    && cat.getTime().compareTo(shift2CFBMulai.getTime()) > 0 && is2shift) {
                status = "04";
            } else if (cat.getTime().compareTo(shift2IstirhahatAkhir.getTime()) < 0
                    && cat.getTime().compareTo(shift2IstirahatMulai.getTime()) > 0 && is2shift) {
                status = "05";
            } else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                    && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
                status = "06";
            }  else if (cat.getTime().compareTo(jamAkhirKerja.getTime()) > 0 && !is2shift) {
                status = "07";
            } else {
                status = "00";
            }

        }

        return akanDiAssign;
    }

}
