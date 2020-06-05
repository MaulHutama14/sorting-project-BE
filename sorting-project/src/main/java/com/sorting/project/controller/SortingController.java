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

import org.springframework.beans.factory.annotation.Autowired;
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

    /*@RequestMapping("/backup")
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
    }*/

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

    @RequestMapping("/checkSorting")
    public ResponseEntity<Map<String, Object>> doCheckSorting(@RequestBody Map<String, Object> request) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        List<ProsesKomponen> prosesKomponenList = new ArrayList<>();
        List<ProsesKomponen>  listProsesSave= new ArrayList<>();
        List<TanggalLibur> tglLiburList;
        String proses = "CHECK";
        Date minDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Boolean is2shift;
        List<Alat> listAlat;
        List<Object[]> itemList = new ArrayList<>();
        itemList = (List<Object[]>) request.get("single_shift");
        List<Date[]> tglSingleShift = new ArrayList<>();
        for (Object item : itemList) {
            Map<String,Object> itemAdd = (Map<String, Object>) item;
            Date[] itemToBeAdd = {
                    sdf2.parse(itemAdd.get("tanggal_mulai").toString() + " 00:00"),
                    sdf2.parse(itemAdd.get("tanggal_akhir").toString() + " 23:59")
            };
            tglSingleShift.add(itemToBeAdd);
        }
        Date dt = sdf2.parse(request.get("tanggal_selesai").toString() + " " + request.get("jam_selesai").toString());

        try {
            this.alatService.refreshAlat();
            listAlat = this.alatService.findAllActive();
        } catch (Exception e) {
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

        List<Object[]> prosesDiCek = this.prosesKomponenService.findSortByKomponenAndProses();

        int i = prosesDiCek.size() - 1;
        Date temp = null;
        while (i >= 0) {
            System.out.println("Progress " + i + " dari " + (prosesDiCek.size() - 1));

            List<ProsesKomponen> listKomponenDiSortir = this.prosesKomponenService.findByIdProsesKomponen(prosesDiCek.get(i)[0].toString(), "");

            for (int j = listKomponenDiSortir.size() - 1; j >= 0; j--) {
                int sizeI = prosesDiCek.size() - 1;
                int sizeJ = listKomponenDiSortir.size() - 1;
                ProsesKomponen akanDiAssign = listKomponenDiSortir.get(j);
                Date batasAkhir;
                Produk produk = akanDiAssign.getKomponen().getProduk();
                Alat alat;
                if (akanDiAssign.getAlat() != null && akanDiAssign.getProses().getNamaProses().equalsIgnoreCase("PLM")) {
                    if (akanDiAssign.getAlat().getStatus()) {
                        alat = akanDiAssign.getAlat();
                    } else {
                        alat = this.findAlat(akanDiAssign.getProses().getID(), listAlat,proses);
                    }
                } else {
                    alat = this.findAlat(akanDiAssign.getProses().getID(), listAlat,proses);
                }

                if (j == sizeJ && i == sizeI) { // untuk proses komponen yang paling akhir
                        batasAkhir = dt;
                } else if (j < produk.getKuantitas() && listKomponenDiSortir.size() > produk.getKuantitas()) {
                        ProsesKomponen prosesSebelumnya = listKomponenDiSortir.get(j + produk.getKuantitas());
                        if (alat.getTanggalAssign() != null
                                &&
                                (alat.getTanggalAssign().before(prosesSebelumnya.getAssignDate())
                                ||alat.getTanggalAssign().equals(prosesSebelumnya.getAssignDate()))) {
                            batasAkhir = alat.getTanggalAssign();
                        } else {
                            batasAkhir = prosesSebelumnya.getAssignDate();
                        }
                } else if (sizeJ != 1 && j != sizeJ){
                    ProsesKomponen prosesSebelumnya = listKomponenDiSortir.get(j + 1);
                    if (alat.getTanggalAssign() != null
                            &&
                            (alat.getTanggalAssign().before(prosesSebelumnya.getAssignDate())
                                    ||alat.getTanggalAssign().equals(prosesSebelumnya.getAssignDate()))) {
                        batasAkhir = alat.getTanggalAssign();
                    } else {
                        batasAkhir = prosesSebelumnya.getAssignDate();
                    }
                } else {
                    if (alat.getTanggalAssign() != null &&
                            (alat.getTanggalAssign().before(dt)
                                    ||alat.getTanggalAssign().equals(dt))) {
                        batasAkhir = alat.getTanggalAssign();
                    } else {
                        batasAkhir = dt;
                    }
                }

                is2shift = cekDoubleShift(tglSingleShift, batasAkhir);
                int index = listAlat.indexOf(alat);

                akanDiAssign.setAssignEnd(batasAkhir);
                akanDiAssign = checkAssignDateV2(akanDiAssign, is2shift, tglLiburList);
                listKomponenDiSortir.set(j, akanDiAssign);
                listAlat.get(index).setTanggalAssign(akanDiAssign.getAssignDate());

                if (temp ==null) {
                    temp = akanDiAssign.getAssignDate();
                } else if (akanDiAssign.getAssignDate().before(temp)) {
                    temp = akanDiAssign.getAssignDate();
                }
                listProsesSave.add(akanDiAssign);
            }

            i--;
        }
        this.prosesKomponenService.saveAll(listProsesSave);


        minDate= temp;
        AppSetting appMinDate = appSettingService.findById("MIN_DATE");
        AppSetting lastChecked = appSettingService.findById("LAST_CHECKED");

        appMinDate.setAppValue(sdf.format(minDate));
        lastChecked.setAppValue(sdf.format(new Date()));

        appSettingService.save(appMinDate);
        appSettingService.save(lastChecked);

        result.put("success",true);
        result.put("minDate",appMinDate.getAppValue());
        result.put("lastChecked",lastChecked.getAppValue());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping("/doSorting")
    public ResponseEntity<Map<String, Object>> doSorting(@RequestBody Map<String, Object> request) throws ParseException {
        Map<String, Object> result = new HashMap<>();
        List<ProsesKomponen> prosesKomponenList = new ArrayList<>();
        Boolean success = true;
        String proses = "SORTING";
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        List<ProsesKomponen> listProsesSave = new ArrayList<>();
        List<Object[]> itemList = new ArrayList<>();
        itemList = (List<Object[]>) request.get("single_shift");
        List<Date[]> tglSingleShift = new ArrayList<>();
        for (Object item : itemList) {
            Map<String,Object> itemAdd = (Map<String, Object>) item;
            Date[] itemToBeAdd = {
                    sdf2.parse(itemAdd.get("tanggal_mulai").toString() + " 00:00"),
                    sdf2.parse(itemAdd.get("tanggal_akhir").toString() + " 23:59")
            };
            tglSingleShift.add(itemToBeAdd);
        }
        List<TanggalLibur> tglLiburList;
        Date minDate;
        boolean is2shift = true;
        List<Alat> listAlat =  new ArrayList<>();

        try {
            this.refreshMasterAlatDanSort();
            listAlat = this.alatService.findAllActive();
//            AppSetting app = appSettingService.findById("DOUBLE_SHIFT");
//            is2shift = app.getAppValue().equalsIgnoreCase("1") ? true : false;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            result.put("message","Gagal refresh ulang alat!");
            result.put("success", false);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        try {
            prosesKomponenList = this.prosesKomponenService.findCuttingByDeadlinePriorWaktuJumProsNama();
            minDate = sdf.parse(appSettingService.findById("MIN_DATE").getAppValue());
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

        Date dt = sdf2.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());

        if (false) {
            System.out.println("=================" +
                    "WAKTU MULAI SORTING MELEWATI BATAS MAKSIMAL");
            result.put("success",false);
            result.put("message","Batas maksimal sorting adalah " + sdf.format(minDate));
            return new ResponseEntity<>(result,HttpStatus.OK);
        }


        List<Object[]> prosesDitarik = this.prosesKomponenService.findSortByKomponenAndProses();

        Map<String, Date> waktuProduk = new HashMap<>();
        for (int z = 0; z < prosesDitarik.size(); z++) {
            System.out.println("Progress " + z + " dari " + prosesDitarik.size());
            List<ProsesKomponen> akanDiTarik = this.prosesKomponenService.findByIdProsesKomponen(prosesDitarik.get(z)[0].toString(), "");
            for (int j = 0; j < akanDiTarik.size(); j++) {

                Alat alat;
                if (akanDiTarik.get(j).getAlat() != null && akanDiTarik.get(j).getProses().getNamaProses().equalsIgnoreCase("PLM")) {
                    if (akanDiTarik.get(j).getAlat().getStatus()) {
                        alat = akanDiTarik.get(j).getAlat();
                    } else {
                        alat = this.findAlat(akanDiTarik.get(j).getProses().getID(), listAlat,proses);
                    }
                } else {
                    alat = this.findAlat(akanDiTarik.get(j).getProses().getID(), listAlat,proses);
                }

                akanDiTarik.get(j).setAlat(alat);
                Produk produk = akanDiTarik.get(j).getKomponen().getProduk();
                Long waktuProsesLong = Math.round(akanDiTarik.get(j).getDurasiProses()  * Double.parseDouble("60"));
                Integer waktuProses = Integer.parseInt(waktuProsesLong.toString());
                if (z == 0 && j == 0){

                    is2shift = cekDoubleShift(tglSingleShift, dt);
                    akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));

                    akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));

                } else if (j > 0) {
                    int posisi = j - 1;
                    if (j >= produk.getKuantitas()) {
                        posisi = j - produk.getKuantitas();
                    }
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null
                    && waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).after(akanDiTarik.get(posisi).getAssignEnd())) {
                        dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                    } else {
                        dt = akanDiTarik.get(posisi).getAssignEnd();
                    }
                    is2shift = cekDoubleShift(tglSingleShift, dt);
                    akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));

                    akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));


                } else if (z != 0 && j == 0) {
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null) {
                        dt = waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat());
                    } else {
                        dt = sdf2.parse(request.get("tanggal_mulai").toString() + " " + request.get("jam_mulai").toString());
                    }
                    is2shift = cekDoubleShift(tglSingleShift, dt);
                    akanDiTarik.get(j).setAssignDate(dt);
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(dt, waktuProses));

                    akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));

                }

                int index = listAlat.indexOf(alat);
                listAlat.get(index).setTanggalAssign(akanDiTarik.get(j).getAssignEnd());

                waktuProduk.put(akanDiTarik.get(j).getAlat().getNamaAlat(), akanDiTarik.get(j).getAssignEnd());
                akanDiTarik.get(j).setSortId(z);
                listProsesSave.add(akanDiTarik.get(j));
            }

//            this.prosesKomponenService.saveAll(akanDiTarik);

        }
        this.prosesKomponenService.saveAll(listProsesSave);

        System.out.println("=====================\n"
                + "     JOB DONE \n"
                + "=====================");

//        prosesKomponenService.saveBackUp();
        result.put("success", success);
        result.put("message","Sukses melakukan sorting!");
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
                   List<ProsesKomponen> akanDiTarik = this.prosesKomponenService.findByIdProsesKomponen(prosesDitarik.get(z)[0].toString(), "");
                   for (int j = 0; j < akanDiTarik.size(); j++) {
                       Long waktuProsesLong = Math.round(akanDiTarik.get(j).getDurasiProses() * akanDiTarik.get(j).getKomponen().getProduk().getKuantitas() * Double.parseDouble("60"));
                       Integer waktuProses = Integer.parseInt(waktuProsesLong.toString());
                       if (z == 0 && j == 0){

                           akanDiTarik.get(j).setAssignDate(dt);
                           akanDiTarik.get(j).setAssignEnd(
                                   this.dateManipulator.addSeconds(dt, waktuProses));

                           akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));

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


                           akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));


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

                           akanDiTarik.set(j,this.checkAssignEndV2(akanDiTarik.get(j),is2shift));

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

    private Boolean cekDoubleShift(List<Date[]>tglSingleShift, Date tglAssign) {
        Boolean status = true;
        for (Date[] item : tglSingleShift) {
            if (tglAssign.after(item[0]) && tglAssign.before(item[1])) {
                status = false;
            }
        }
        return status;
    }

    private Alat findAlat (String prosesId, List<Alat> listAlat, String proses) {
        List<Alat> filteredAlat = new ArrayList<>();
        Alat resultAlat = null;

        for (Alat alat : listAlat) {
            if (alat.getMasterAlat().getProses().getID().equalsIgnoreCase(prosesId)) {
                if (alat.getTanggalAssign() == null) {
                    resultAlat = alat;
                    break;
                } else {
                    filteredAlat.add(alat);
                }
            }
        }

        if (resultAlat!= null) {
            return resultAlat;
        } else {
            for (int i = 0; i < filteredAlat.size(); i++) {
                if (i == 0 ) {
                    resultAlat = filteredAlat.get(i);
                } else if (
                        (filteredAlat.get(i).getTanggalAssign().before(resultAlat.getTanggalAssign())
                        || filteredAlat.get(i).getTanggalAssign().equals(resultAlat.getTanggalAssign()))
                && proses.equalsIgnoreCase("SORTING")){
                    resultAlat = filteredAlat.get(i);
                } else if (
                        (filteredAlat.get(i).getTanggalAssign().after(resultAlat.getTanggalAssign())
                        || filteredAlat.get(i).getTanggalAssign().equals(resultAlat.getTanggalAssign()))
                        && proses.equalsIgnoreCase("CHECK")){
                    resultAlat = filteredAlat.get(i);
                }
            }
            return  resultAlat;
        }


    }

    private void refreshMasterAlatDanSort() {
        this.mtaService.refreshMta();
        this.prosesKomponenService.refreshProsesKomponen();
        this.alatService.refreshAlat();
    }

    private ProsesKomponen checkAssignEndV2(ProsesKomponen akanDiAssign, Boolean is2shift) throws ParseException {
        List<TanggalLibur> tglLiburList = this.prosesKomponenService.findAllLibur();
        Calendar tglMulai = Calendar.getInstance();
        Calendar batasAkhir = Calendar.getInstance();
        tglMulai.setTime(akanDiAssign.getAssignDate());
        Long waktuProsesLong = Math.round(akanDiAssign.getDurasiProses()  * Double.parseDouble("60"));

        Calendar jamMulaiKerja =  setBatasTanggal(tglMulai.getTime()).get(0);
        Calendar jamAwalIstirahat =  setBatasTanggal(tglMulai.getTime()).get(1);
        Calendar jamAkhirIstirahat =  setBatasTanggal(tglMulai.getTime()).get(2);
        Calendar jamAkhirKerja =  setBatasTanggal(tglMulai.getTime()).get(3);
        Calendar shift2JamMulai =  setBatasTanggal(tglMulai.getTime()).get(4);
        Calendar shift2CFBMulai =  setBatasTanggal(tglMulai.getTime()).get(5);
        Calendar shift2CFBAkhir =  setBatasTanggal(tglMulai.getTime()).get(6);
        Calendar shift2IstirahatMulai =  setBatasTanggal(tglMulai.getTime()).get(7);
        Calendar shift2IstirhahatAkhir =  setBatasTanggal(tglMulai.getTime()).get(8);
        Calendar shift2JamAkhir =  setBatasTanggal(tglMulai.getTime()).get(9);

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

        if (tglMulai.getTime().compareTo(jamMulaiKerja.getTime()) >= 0
                && tglMulai.getTime().compareTo(jamAwalIstirahat.getTime()) <= 0) {
            batasAkhir = jamAwalIstirahat;
            status = "01";
        } else if (tglMulai.getTime().compareTo(jamAkhirIstirahat.getTime()) >= 0
                && tglMulai.getTime().compareTo(jamAkhirKerja.getTime()) <= 0) {
            batasAkhir = jamAkhirKerja;
            status = "02";
        } else if (tglMulai.getTime().compareTo(shift2JamMulai.getTime()) >= 0
                && tglMulai.getTime().compareTo(shift2IstirahatMulai.getTime()) <= 0) {
            batasAkhir = shift2IstirahatMulai;
            status = "03";
        } else if (tglMulai.getTime().compareTo(shift2IstirhahatAkhir.getTime()) >= 0
                || tglMulai.getTime().compareTo(shift2CFBMulai.getTime()) <= 0 ) {
            if (tglMulai.getTime().compareTo(shift2IstirhahatAkhir.getTime()) >= 0) {
                shift2CFBMulai.add(Calendar.DATE,1);
                shift2CFBAkhir.add(Calendar.DATE,1);
                shift2JamAkhir.add(Calendar.DATE,1);
                batasAkhir = shift2CFBMulai;
            } else {
                batasAkhir = shift2CFBMulai;
            }
            status = "04";
        } else if (tglMulai.getTime().compareTo(shift2CFBAkhir.getTime()) >= 0
                && tglMulai.getTime().compareTo(shift2JamAkhir.getTime()) <= 0 ) {
            batasAkhir = shift2JamAkhir;
            status = "05";
        }

        while (!status.equals("00")) {

            boolean needExtraTime = false;
            long diff = (batasAkhir.getTimeInMillis() - tglMulai.getTimeInMillis()) / 1000;

            if (diff < waktuProsesLong) {
                needExtraTime = true;
                waktuProsesLong = waktuProsesLong - diff;
            } else {
                status = "00";
            }

            switch (status) {
                case "01":
                    if (needExtraTime) {
                        status = "02";
                        tglMulai = jamAkhirIstirahat;
                        batasAkhir = jamAkhirKerja;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "02":
                    if (needExtraTime && is2shift) {
                        status = "03";
                        tglMulai = shift2JamMulai;
                        batasAkhir = shift2IstirahatMulai;
                        break;
                    } else if (needExtraTime && !is2shift) {
                        status = "01";
                        jamMulaiKerja.add(Calendar.DATE,1);
                        if (cekTanggal(tglLiburList,jamMulaiKerja.getTime())) {
                            while (cekTanggal(tglLiburList,jamMulaiKerja.getTime())) {
                                jamMulaiKerja.add(Calendar.DATE,1);
                            }
                        }
                        tglMulai = jamMulaiKerja;
                        jamMulaiKerja =  setBatasTanggal(tglMulai.getTime()).get(0);
                        jamAwalIstirahat =  setBatasTanggal(tglMulai.getTime()).get(1);
                        jamAkhirIstirahat =  setBatasTanggal(tglMulai.getTime()).get(2);
                        jamAkhirKerja =  setBatasTanggal(tglMulai.getTime()).get(3);
                        shift2JamMulai =  setBatasTanggal(tglMulai.getTime()).get(4);
                        shift2CFBMulai =  setBatasTanggal(tglMulai.getTime()).get(5);
                        shift2CFBAkhir =  setBatasTanggal(tglMulai.getTime()).get(6);
                        shift2IstirahatMulai =  setBatasTanggal(tglMulai.getTime()).get(7);
                        shift2IstirhahatAkhir =  setBatasTanggal(tglMulai.getTime()).get(8);
                        shift2JamAkhir =  setBatasTanggal(tglMulai.getTime()).get(9);
                        batasAkhir = jamAwalIstirahat;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "03":
                    if (needExtraTime) {
                        status = "04";
                        tglMulai = shift2IstirhahatAkhir;
                        shift2CFBMulai.add(Calendar.DATE,1);
                        batasAkhir = shift2CFBMulai;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "04":
                    if (needExtraTime) {
                        status = "05";
                        tglMulai = shift2CFBAkhir;
                        batasAkhir = shift2JamAkhir;
                        jamMulaiKerja =  setBatasTanggal(tglMulai.getTime()).get(0);
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "05":
                    if (needExtraTime) {
                        status = "01";
                        while (cekTanggal(tglLiburList,jamMulaiKerja.getTime())) {
                            jamMulaiKerja.add(Calendar.DATE,1);
                        }
                        tglMulai = jamMulaiKerja;
                        jamMulaiKerja =  setBatasTanggal(tglMulai.getTime()).get(0);
                        jamAwalIstirahat =  setBatasTanggal(tglMulai.getTime()).get(1);
                        jamAkhirIstirahat =  setBatasTanggal(tglMulai.getTime()).get(2);
                        jamAkhirKerja =  setBatasTanggal(tglMulai.getTime()).get(3);
                        shift2JamMulai =  setBatasTanggal(tglMulai.getTime()).get(4);
                        shift2CFBMulai =  setBatasTanggal(tglMulai.getTime()).get(5);
                        shift2CFBAkhir =  setBatasTanggal(tglMulai.getTime()).get(6);
                        shift2IstirahatMulai =  setBatasTanggal(tglMulai.getTime()).get(7);
                        shift2IstirhahatAkhir =  setBatasTanggal(tglMulai.getTime()).get(8);
                        shift2JamAkhir =  setBatasTanggal(tglMulai.getTime()).get(9);
                        batasAkhir = jamAwalIstirahat;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
            }
        }

        tglMulai.add(Calendar.SECOND, waktuProsesLong.intValue());
        Date jamAkhir = tglMulai.getTime();
        akanDiAssign.setAssignEnd(jamAkhir);

        return akanDiAssign;
    }

    private ProsesKomponen checkAssignDateV2(ProsesKomponen akanDiAssign, Boolean is2shift, List<TanggalLibur> tglLiburList) throws ParseException {
        Calendar tglMulai = Calendar.getInstance();
        Calendar batasAkhir = Calendar.getInstance();
        int hour = akanDiAssign.getAssignEnd().getHours();

        batasAkhir.setTime(akanDiAssign.getAssignEnd());
        if (hour >= 12) {
            batasAkhir.set(Calendar.AM_PM, Calendar.PM);
        } else {
            batasAkhir.set(Calendar.AM_PM, Calendar.AM);
        }
        Long waktuProsesLong = Math.round(akanDiAssign.getDurasiProses()  * Double.parseDouble("60"));

        Calendar jamMulaiKerja =  setBatasTanggal(batasAkhir.getTime()).get(0);
        Calendar jamAwalIstirahat =  setBatasTanggal(batasAkhir.getTime()).get(1);
        Calendar jamAkhirIstirahat =  setBatasTanggal(batasAkhir.getTime()).get(2);
        Calendar jamAkhirKerja =  setBatasTanggal(batasAkhir.getTime()).get(3);
        Calendar shift2JamMulai =  setBatasTanggal(batasAkhir.getTime()).get(4);
        Calendar shift2CFBMulai =  setBatasTanggal(batasAkhir.getTime()).get(5);
        Calendar shift2CFBAkhir =  setBatasTanggal(batasAkhir.getTime()).get(6);
        Calendar shift2IstirahatMulai =  setBatasTanggal(batasAkhir.getTime()).get(7);
        Calendar shift2IstirhahatAkhir =  setBatasTanggal(batasAkhir.getTime()).get(8);
        Calendar shift2JamAkhir =  setBatasTanggal(batasAkhir.getTime()).get(9);



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

        if (batasAkhir.getTime().compareTo(jamMulaiKerja.getTime()) >= 0
                && batasAkhir.getTime().compareTo(jamAwalIstirahat.getTime()) <= 0) {
            tglMulai = jamMulaiKerja;
            status = "01";
        } else if (batasAkhir.getTime().compareTo(jamAkhirIstirahat.getTime()) >= 0
                && batasAkhir.getTime().compareTo(jamAkhirKerja.getTime()) <= 0) {
            tglMulai = jamAkhirIstirahat;
            status = "02";
        } else if (batasAkhir.getTime().compareTo(shift2JamMulai.getTime()) >= 0
                && batasAkhir.getTime().compareTo(shift2IstirahatMulai.getTime()) <= 0) {
            tglMulai = shift2JamMulai;
            status = "03";
        } else if (batasAkhir.getTime().compareTo(shift2IstirhahatAkhir.getTime()) >= 0
                || batasAkhir.getTime().compareTo(shift2CFBMulai.getTime()) <= 0 ) {
            if (batasAkhir.getTime().compareTo(shift2CFBMulai.getTime()) <= 0) {
                shift2IstirhahatAkhir.add(Calendar.DATE,-1);
                tglMulai = shift2IstirhahatAkhir;
            } else {
                tglMulai = shift2IstirhahatAkhir;
            }
            status = "04";
        } else if (batasAkhir.getTime().compareTo(shift2CFBAkhir.getTime()) >= 0
                && batasAkhir.getTime().compareTo(shift2JamAkhir.getTime()) <= 0 ) {
            tglMulai = shift2CFBAkhir;
            status = "05";
        }

        while (!status.equals("00")) {

            boolean needExtraTime = false;
            long diff = (batasAkhir.getTimeInMillis() - tglMulai.getTimeInMillis()) / 1000;

            if (diff < waktuProsesLong) {
                needExtraTime = true;
                waktuProsesLong = waktuProsesLong - diff;
            } else {
                status = "00";
            }

            switch (status) {
                case "01":
                    if (needExtraTime && is2shift) {
                        status = "05";
                        jamAkhirKerja.add(Calendar.DATE,-1);
                        if (cekTanggal(tglLiburList,jamAkhirKerja.getTime())) {
                            while (cekTanggal(tglLiburList,jamAkhirKerja.getTime())) {
                                jamAkhirKerja.add(Calendar.DATE,-1);
                            }
                        }
                        jamAkhirKerja.add(Calendar.DATE,1);
                        shift2CFBAkhir =  setBatasTanggal(jamAkhirKerja.getTime()).get(6);
                        shift2JamAkhir =  setBatasTanggal(jamAkhirKerja.getTime()).get(9);
                        tglMulai = shift2CFBAkhir;
                        batasAkhir = shift2JamAkhir;
                        break;
                    } else if (needExtraTime && !is2shift) {
                        status = "02";
                        jamAkhirKerja.add(Calendar.DATE,-1);
                        if (cekTanggal(tglLiburList,jamAkhirKerja.getTime())) {
                            while (cekTanggal(tglLiburList,jamAkhirKerja.getTime())) {
                                jamAkhirKerja.add(Calendar.DATE,-1);
                            }
                        }
                        batasAkhir = jamAkhirKerja;
                        jamMulaiKerja =  setBatasTanggal(batasAkhir.getTime()).get(0);
                        jamAwalIstirahat =  setBatasTanggal(batasAkhir.getTime()).get(1);
                        jamAkhirIstirahat =  setBatasTanggal(batasAkhir.getTime()).get(2);
                        jamAkhirKerja =  setBatasTanggal(batasAkhir.getTime()).get(3);
                        shift2JamMulai =  setBatasTanggal(batasAkhir.getTime()).get(4);
                        shift2CFBMulai =  setBatasTanggal(batasAkhir.getTime()).get(5);
                        shift2CFBAkhir =  setBatasTanggal(batasAkhir.getTime()).get(6);
                        shift2IstirahatMulai =  setBatasTanggal(batasAkhir.getTime()).get(7);
                        shift2IstirhahatAkhir =  setBatasTanggal(batasAkhir.getTime()).get(8);
                        shift2JamAkhir =  setBatasTanggal(batasAkhir.getTime()).get(9);
                        tglMulai = jamAkhirIstirahat;
                        batasAkhir = jamAkhirKerja;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "02":
                    if (needExtraTime) {
                        status = "01";
                        tglMulai = jamMulaiKerja;
                        batasAkhir = jamAwalIstirahat;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "03":
                    if (needExtraTime) {
                        status = "02";
                        tglMulai = jamAkhirIstirahat;
                        batasAkhir = jamAkhirKerja;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "04":
                    if (needExtraTime) {
                        status = "03";
                        tglMulai = shift2JamMulai;
                        batasAkhir = shift2IstirahatMulai;
                        break;
                    } else {
                        status = "00";
                        break;
                    }
                case "05":
                    if (needExtraTime) {
                        status = "04";
                        shift2IstirhahatAkhir.add(Calendar.DATE,-1);
                        tglMulai = shift2IstirhahatAkhir;
                        batasAkhir = shift2CFBMulai;
                        jamMulaiKerja =  setBatasTanggal(tglMulai.getTime()).get(0);
                        jamAwalIstirahat =  setBatasTanggal(tglMulai.getTime()).get(1);
                        jamAkhirIstirahat =  setBatasTanggal(tglMulai.getTime()).get(2);
                        jamAkhirKerja =  setBatasTanggal(tglMulai.getTime()).get(3);
                        shift2JamMulai =  setBatasTanggal(tglMulai.getTime()).get(4);
                        shift2CFBMulai =  setBatasTanggal(tglMulai.getTime()).get(5);
                        shift2CFBAkhir =  setBatasTanggal(tglMulai.getTime()).get(6);
                        shift2IstirahatMulai =  setBatasTanggal(tglMulai.getTime()).get(7);
                        shift2IstirhahatAkhir =  setBatasTanggal(tglMulai.getTime()).get(8);
                        shift2JamAkhir =  setBatasTanggal(tglMulai.getTime()).get(9);
                        break;
                    } else {
                        status = "00";
                        break;
                    }
            }
        }

        batasAkhir.add(Calendar.SECOND, -waktuProsesLong.intValue());
        Date jamMulai = batasAkhir.getTime();
        akanDiAssign.setAssignDate(jamMulai);

        return akanDiAssign;
    }

    private List<Calendar> setBatasTanggal (Date tanggalan) {
        List<Calendar> calendars = new ArrayList<>();

        // **AWAL JAM ISTIRAHAT
        Calendar jamAwalIstirahat = Calendar.getInstance();
        jamAwalIstirahat.setTime(tanggalan);
        jamAwalIstirahat.set(Calendar.AM_PM, Calendar.AM);
        jamAwalIstirahat.set(Calendar.HOUR, 11);
        jamAwalIstirahat.set(Calendar.MINUTE, 45);
        jamAwalIstirahat.set(Calendar.SECOND, 00);

        // **AKHIR JAM ISTIRAHAT
        Calendar jamAkhirIstirahat = Calendar.getInstance();
        jamAkhirIstirahat.setTime(tanggalan);
        jamAkhirIstirahat.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirIstirahat.set(Calendar.HOUR, 0);
        jamAkhirIstirahat.set(Calendar.MINUTE, 45);
        jamAkhirIstirahat.set(Calendar.SECOND, 00);

        // ** MULAI JAM KERJA SHIFT-1
        Calendar jamMulaiKerja = Calendar.getInstance();
        jamMulaiKerja.setTime(tanggalan);
        jamMulaiKerja.set(Calendar.AM_PM, Calendar.AM);
        jamMulaiKerja.set(Calendar.HOUR, 8);
        jamMulaiKerja.set(Calendar.MINUTE, 00);
        jamMulaiKerja.set(Calendar.SECOND, 00);

        //** SELESAI JAM KERJA SHIFT-1
        Calendar jamAkhirKerja = Calendar.getInstance();
        jamAkhirKerja.setTime(tanggalan);
        jamAkhirKerja.set(Calendar.AM_PM, Calendar.PM);
        jamAkhirKerja.set(Calendar.HOUR, 4);
        jamAkhirKerja.set(Calendar.MINUTE, 00);
        jamAkhirKerja.set(Calendar.SECOND, 00);

        //**SHIFT 2**//
        Calendar shift2JamMulai = Calendar.getInstance();
        shift2JamMulai.setTime(tanggalan);
        shift2JamMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2JamMulai.set(Calendar.HOUR, 7);
        shift2JamMulai.set(Calendar.MINUTE, 30);
        shift2JamMulai.set(Calendar.SECOND, 00);

        Calendar shift2JamAkhir = Calendar.getInstance();
        shift2JamAkhir.setTime(tanggalan);
        shift2JamAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2JamAkhir.set(Calendar.HOUR, 3);
        shift2JamAkhir.set(Calendar.MINUTE, 30);
        shift2JamAkhir.set(Calendar.SECOND, 00);

        //**CFB = Coffee Break
        Calendar shift2CFBAkhir = Calendar.getInstance();
        shift2CFBAkhir.setTime(tanggalan);
        shift2CFBAkhir.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBAkhir.set(Calendar.HOUR, 01);
        shift2CFBAkhir.set(Calendar.MINUTE, 15);
        shift2CFBAkhir.set(Calendar.SECOND, 00);

        Calendar shift2CFBMulai = Calendar.getInstance();
        shift2CFBMulai.setTime(tanggalan);
        shift2CFBMulai.set(Calendar.AM_PM, Calendar.AM);
        shift2CFBMulai.set(Calendar.HOUR, 01);
        shift2CFBMulai.set(Calendar.MINUTE, 00);
        shift2CFBMulai.set(Calendar.SECOND, 00);

        //**Istirahat shift2
        Calendar shift2IstirhahatAkhir = Calendar.getInstance();
        shift2IstirhahatAkhir.setTime(tanggalan);
        shift2IstirhahatAkhir.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirhahatAkhir.set(Calendar.HOUR, 11);
        shift2IstirhahatAkhir.set(Calendar.MINUTE, 00);
        shift2IstirhahatAkhir.set(Calendar.SECOND, 00);

        Calendar shift2IstirahatMulai = Calendar.getInstance();
        shift2IstirahatMulai.setTime(tanggalan);
        shift2IstirahatMulai.set(Calendar.AM_PM, Calendar.PM);
        shift2IstirahatMulai.set(Calendar.HOUR, 10);
        shift2IstirahatMulai.set(Calendar.MINUTE, 00);
        shift2IstirahatMulai.set(Calendar.SECOND, 00);

        calendars.add(jamMulaiKerja);
        calendars.add(jamAwalIstirahat);
        calendars.add(jamAkhirIstirahat);
        calendars.add(jamAkhirKerja);
        calendars.add(shift2JamMulai);
        calendars.add(shift2CFBMulai);
        calendars.add(shift2CFBAkhir);
        calendars.add(shift2IstirahatMulai);
        calendars.add(shift2IstirhahatAkhir);
        calendars.add(shift2JamAkhir);

        return  calendars;
    }

    @RequestMapping("/download-sorting")
    public ResponseEntity<Map<String, Object>> doDownload(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Object[]> listItem = prosesKomponenService.getHasilSorting();
            result.put("success",true);
            result.put("item",listItem);
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

}
