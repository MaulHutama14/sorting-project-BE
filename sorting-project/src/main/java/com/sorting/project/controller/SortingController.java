/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.controller;

import com.sorting.project.model.Alat;
import com.sorting.project.model.Komponen;
import com.sorting.project.model.MasterAlat;
import com.sorting.project.model.ProsesKomponen;
import com.sorting.project.model.master.MasterTanggalAlat;
import com.sorting.project.model.util.DateManipulator;
import com.sorting.project.service.AlatService;
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

    /*
     *   com.sorting.project.model.util
     */
    private DateManipulator dateManipulator = new DateManipulator();

    @RequestMapping("/doSorting")
    public ResponseEntity<Map<String, Object>> doSorting() throws ParseException {
        Map<String, Object> result = new HashMap<>();
        List<ProsesKomponen> prosesKomponenList = new ArrayList<>();
        Boolean success = true;
        List<ProsesKomponen> listProsesSave = new ArrayList<>();

        try {
            this.refreshMasterAlatDanSort();
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
            result.put("success", success);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        try {
            prosesKomponenList = this.prosesKomponenService.findCuttingByDeadlinePriorWaktuJumProsNama();
        } catch (Exception e) {
            e.printStackTrace();
            prosesKomponenList = null;
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
                Alat alat = this.findAvailableAlat(akanDiAssign.getProses().getID());

                int waktuProses = Integer.parseInt(akanDiAssign.getKomponen().getKuantitas()) * akanDiAssign.getDurasiProses() * 60;

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

                akanDiAssign = this.loopingSorting(tanggalMaster, akanDiAssign, waktuProses);

                akanDiAssign.setAlat(alat);

                akanDiAssign.setSortId(i);

                akanDiAssign.setIsProses(false);

                temp = tanggalMaster.get(0).getTanggalAlat();

                this.mtaService.saveOne(tanggalMaster.get(0));
                listProsesSave.add(akanDiAssign);
//                this.prosesKomponenService.saveOne(akanDiAssign);
                this.alatService.save(alat);
            }

            i--;
        }

        try {
            this.prosesKomponenService.saveAll(listProsesSave);
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            return new ResponseEntity<>(result, HttpStatus.OK);
        }

        List<String> prosesDitarik = this.prosesKomponenService.findSortByKomponenAndProses();
        Date dt = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.set(Calendar.HOUR, 8);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.DATE, 1);
        dt = c.getTime();
        Date temp = new Date();
        Map<String, Date> waktuProduk = new HashMap<>();
        Long diff;
        Long diffDays;
        Long diffSeconds;
        Long diffMinutes;
        Long diffHours;
        for (int z = 0; z < prosesDitarik.size(); z++) {
            List<ProsesKomponen> akanDiTarik = this.prosesKomponenService.findByProsesAndSortByIdPorses(prosesDitarik.get(z), "");
            for (int j = 0; j < akanDiTarik.size(); j++) {
                if (z == 0 && j == 0){
                    diff = akanDiTarik.get(j).getAssignDate().getTime() - dt.getTime();
//                    diffDays = diff / (24 * 60 * 60 * 1000);
                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                    akanDiTarik.get(j).setAssignDate(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignDate(), -diffDays.intValue()));
//                    akanDiTarik.get(j).setAssignEnd(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignEnd(), -diffDays.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignDate(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignEnd(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignDate(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignEnd(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignDate(), -diffSeconds.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), -diffSeconds.intValue()));

                } else if (j > 0) {
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null
                    && waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).after(akanDiTarik.get(j - 1).getAssignEnd())) {
                        diff = akanDiTarik.get(j).getAssignDate().getTime() - waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).getTime();
                    } else {
                        diff = akanDiTarik.get(j).getAssignDate().getTime() - akanDiTarik.get(j - 1).getAssignEnd().getTime();
                    }
//                    diffDays = diff / (24 * 60 * 60 * 1000);
                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                    akanDiTarik.get(j).setAssignDate(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignDate(), -diffDays.intValue()));
//                    akanDiTarik.get(j).setAssignEnd(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignEnd(), -diffDays.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignDate(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignEnd(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignDate(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignEnd(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignDate(), -diffSeconds.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), -diffSeconds.intValue()));

                    if (j == akanDiTarik.size() - 1) {
                        temp = akanDiTarik.get(j).getAssignEnd();
                    }

                } else if (z != 0 && j == 0) {
                    if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) != null
                            && waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).after(temp)) {
                        diff = akanDiTarik.get(j).getAssignDate().getTime() - waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()).getTime();
                    } else if (waktuProduk.get(akanDiTarik.get(j).getAlat().getNamaAlat()) == null
                            && akanDiTarik.get(j).getProses().getSortId()==1){
                        diff = akanDiTarik.get(j).getAssignDate().getTime() - dt.getTime();
                    } else {
                        diff = akanDiTarik.get(j).getAssignDate().getTime() - temp.getTime();
                    }
//                    diffDays = diff / (24 * 60 * 60 * 1000);
                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                    akanDiTarik.get(j).setAssignDate(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignDate(), -diffDays.intValue()));
//                    akanDiTarik.get(j).setAssignEnd(
//                            this.dateManipulator.addDays(akanDiTarik.get(j).getAssignEnd(), -diffDays.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignDate(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addHours(akanDiTarik.get(j).getAssignEnd(), -diffHours.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignDate(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addMinutes(akanDiTarik.get(j).getAssignEnd(), -diffMinutes.intValue()));
                    akanDiTarik.get(j).setAssignDate(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignDate(), -diffSeconds.intValue()));
                    akanDiTarik.get(j).setAssignEnd(
                            this.dateManipulator.addSeconds(akanDiTarik.get(j).getAssignEnd(), -diffSeconds.intValue()));

                }
                waktuProduk.put(akanDiTarik.get(j).getAlat().getNamaAlat(), akanDiTarik.get(j).getAssignEnd());


            }

            this.prosesKomponenService.saveAll(akanDiTarik);

        }

     /*       System.out.println("nama produk " + namaProduk.get(z));

            prosesDitarik = this.prosesKomponenService.findProsesTerakhir(namaProduk.get(z));

            if (z != 0) {

                List<ProsesKomponen> prosesSebelumnya = this.prosesKomponenService.findProsesTerakhir(namaProduk.get(z - 1));
                Long diff = prosesDitarik.get(prosesDitarik.size() - 1).getAssignDate().getTime() - prosesSebelumnya.get(prosesSebelumnya.size() - 1).getAssignDate().getTime();
                Long diffDays = diff / (24 * 60 * 60 * 1000);

                for (int aa = 0; aa < prosesDitarik.size(); aa++) {

                   prosesDitarik.get(aa).setAssignDate(
                            this.dateManipulator.addDays(prosesDitarik.get(aa).getAssignDate(), -diffDays.intValue()));
                    prosesDitarik.get(aa).setAssignEnd(
                            this.dateManipulator.addDays(prosesDitarik.get(aa).getAssignEnd(), -diffDays.intValue()));

                }

            } else {
                Long diff = prosesDitarik.get(prosesDitarik.size() - 1).getAssignDate().getTime() - input.getTime().getTime();
                Long diffDays = diff / (24 * 60 * 60 * 1000);
                for (int aa = 0; aa < prosesDitarik.size(); aa++) {

                    prosesDitarik.get(aa).setAssignDate(
                            this.dateManipulator.addDays(prosesDitarik.get(aa).getAssignDate(), -diffDays.intValue()));
                    prosesDitarik.get(aa).setAssignEnd(
                            this.dateManipulator.addDays(prosesDitarik.get(aa).getAssignEnd(), -diffDays.intValue()));


                }
            }

            this.prosesKomponenService.saveAll(prosesDitarik);
        }*/

        System.out.println("=====================\n"
                + "     JOB DONE \n"
                + "=====================");

        result.put("success", success);
        result.put("jumlah", prosesKomponenList.size());
        result.put("hasil", prosesKomponenList);
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
        this.prosesKomponenService.refreshProsesKomponen();
        this.alatService.refreshAlat();
    }

    private ProsesKomponen loopingSorting(List<MasterTanggalAlat> tanggalMaster, ProsesKomponen akanDiAssign, int waktuProses) throws ParseException {
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
        } else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
            status = "06";
        } else {
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
                    if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -1);
                    } else {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -2);
                    }
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKembali, diffSeconds.intValue());
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
            } else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                    && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
                status = "06";
            } else {
                status = "00";
            }

        }

        return akanDiAssign;
    }

    private Date loopingKedua(Date tanggal, ProsesKomponen akanDiAssign) throws ParseException {
        int waktuProses = akanDiAssign.getDurasiProses() * Integer.parseInt(akanDiAssign.getKomponen().getKuantitas() ) * 60;
        tanggal = this.dateManipulator.addSeconds(tanggal, waktuProses);
        Calendar cat = Calendar.getInstance();
        cat.setTime(tanggal);

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
        akanDiAssign.setAssignDate(tanggal);
        akanDiAssign.setAssignEnd(this.dateManipulator.addSeconds(tanggal, waktuProses)
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
        } else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
            status = "06";
        } else {
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
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKerja.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
                case "02":
                    diff = cat.getTime().getTime() - jamAkhirIstirahat.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan saat istirahat pada sort ke " + i + diffHours + ":" + diffMinutes + ":" + diffSeconds);
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamAkhirIstirahat.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
                case "03":
                    diff = cat.getTime().getTime() - shift2JamMulai.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2JamMulai.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
                case "04":
                    diff = cat.getTime().getTime() - shift2CFBAkhir.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2CFBAkhir.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
                case "05":
                    diff = cat.getTime().getTime() - shift2IstirhahatAkhir.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    jamMulaiKembali = this.dateManipulator.addSeconds(shift2IstirhahatAkhir.getTime(), diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
                case "06":
                    diff = cat.getTime().getTime() - jamMulaiKerja.getTime().getTime();

                    diffSeconds = diff / 1000 % 60;
                    diffMinutes = diff / (60 * 1000) % 60;
                    diffHours = diff / (60 * 60 * 1000);

//                            System.out.println("Perbedaan pada sebelum jam kerja sort ke " + i + " " + diffHours + " jam " + diffMinutes + " menit " + diffSeconds + " detik");
                    if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        jamMulaiKembali = this.dateManipulator.addDays(jamMulaiKerja.getTime(), -1);
                    } else {
                        jamMulaiKembali = this.dateManipulator.addDays(shift2JamAkhir.getTime(), -2);
                    }
                    jamMulaiKembali = this.dateManipulator.addSeconds(jamMulaiKembali, diffSeconds.intValue());
                    jamMulaiKembali = this.dateManipulator.addMinutes(jamMulaiKembali, diffMinutes.intValue());
                    jamMulaiKembali = this.dateManipulator.addHours(jamMulaiKembali, diffHours.intValue());

                    akanDiAssign.setAssignDate(jamMulaiKembali);
                    tanggal = jamMulaiKembali;
                    break;
            }

            cat = Calendar.getInstance();
            cat.setTime(tanggal);

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
            akanDiAssign.setAssignDate(tanggal);
            akanDiAssign.setAssignEnd(this.dateManipulator.addSeconds(tanggal, waktuProses)
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
            } else if (cat.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                    || (cat.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                    && cat.getTime().compareTo(jamMulaiKerja.getTime()) < 0)) {
                status = "06";
            } else {
                status = "00";
            }

        }

        return tanggal;
    }

}
