/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sorting.project.service;

import com.sorting.project.model.HistoryProsesKomponen;
import com.sorting.project.model.ProsesKomponen;
import com.sorting.project.model.User;
import com.sorting.project.model.master.TanggalLibur;
import com.sorting.project.repo.HistoryProsesKompRepo;
import com.sorting.project.repo.ProsesKomponenRepo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.transaction.Transactional;

import com.sorting.project.repo.UserRepo;
import com.sorting.project.repo.master.TglLiburRepo;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author PROSIA
 */
@Service
public class ProsesKomponenService {

    @Autowired
    private ProsesKomponenRepo prosesKomponenRepo;

    @Autowired
    private HistoryProsesKompRepo historyProsesKompRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private TglLiburRepo tglLiburRepo;

    public List<ProsesKomponen> findAll () {
        return this.prosesKomponenRepo.findAll();
    }

    public List<TanggalLibur> findAllLibur () {
        return this.tglLiburRepo.findAll();
    }

    public List<Object> restore () {
        List<Object> pesan = new ArrayList<>();
        HistoryProsesKomponen historyProsesKomp =  new HistoryProsesKomponen();
        try {
           HistoryProsesKomponen itemList = historyProsesKompRepo.findLatest();
           String[] parsedString = itemList.getHistoryProsesKomponen().split("-parse-");
            pesan.add(true);
            pesan.add(parsedString);
            pesan.add("BERHASIL");
        } catch (Exception e) {
            e.printStackTrace();
            pesan = new ArrayList<>();
            pesan.add(false);
            pesan.add(e.getMessage());
        }

        return pesan;
    }


    @Transactional
    public List<Object> saveBackUp () {
        List<Object> pesan = new ArrayList<>();
        HistoryProsesKomponen historyProsesKomp =  new HistoryProsesKomponen();
        try {
            List<ProsesKomponen> itemList = prosesKomponenRepo.findAll();
            StringBuffer itemToBeAdd = new StringBuffer();
            for (int i = 0; i < itemList.size(); i++) {
                itemToBeAdd.append(itemList.get(i).getID()+";");
                itemToBeAdd.append(itemList.get(i).getKomponen().getNamaKomponen()+";");
                itemToBeAdd.append(itemList.get(i).getProses().getNamaProses()+";");
                itemToBeAdd.append(itemList.get(i).getAlat().getNamaAlat()+";");
                itemToBeAdd.append(itemList.get(i).getAssignDate()+";");
                itemToBeAdd.append(itemList.get(i).getAssignEnd()+";");
                itemToBeAdd.append(itemList.get(i).getDurasiProses()+";");
                itemToBeAdd.append(itemList.get(i).getIsProses()+";");
                itemToBeAdd.append(itemList.get(i).getSortId()+"-parse-");
            }
            User user = userRepo.getOne("1");
            HashMap<String, Object> item = new HashMap<>();
            item.put("item",itemList);
            historyProsesKomp.setHistoryProsesKomponen(itemToBeAdd.toString());
            historyProsesKomp.setCreatedBy(user);
            this.historyProsesKompRepo.save(historyProsesKomp);
            pesan.add(true);
            pesan.add("BERHASIL");
        } catch (Exception e) {
            e.printStackTrace();
            pesan = new ArrayList<>();
            pesan.add(false);
            pesan.add(e.getMessage());
        }

        return pesan;
    }

    public List<ProsesKomponen> findCuttingByDeadlinePriorWaktuJumProsNama() {
        return this.prosesKomponenRepo.findCuttingByDeadlinePriorWaktuJumProsNama();
    }
    
    public List<ProsesKomponen> findByProsesAndSortByIdPorses (String idProses, String orderBy){
        return this.prosesKomponenRepo.findByProsesAndSortByIdPorses(idProses, orderBy);
    }

    public List<Object[]> findSortByKomponenAndProses () {
        return this.prosesKomponenRepo.findSortByKomponenAndProses();
    }

    public List<Object[]> findSortByNest () {
        return this.prosesKomponenRepo.findSortByNest();
    }

    @Transactional
    public void saveAll (List<ProsesKomponen> prosesKompList){
        this.prosesKomponenRepo.saveAll(prosesKompList);
    }
    
    @Transactional
    public void saveOne (ProsesKomponen item){
        this.prosesKomponenRepo.save(item);
    }
    
    @Transactional
    public void refreshProsesKomponen(){
        this.prosesKomponenRepo.refreshAssignedDate();
    }
    
    public List<String> findProdukAwal(){
        return this.prosesKomponenRepo.findProdukAwal();
    }
    
    public List<ProsesKomponen> findProsesTerakhir(String namaProduk){
        return this.prosesKomponenRepo.findProsesTerakhir(namaProduk);
    }
    
    public List<ProsesKomponen> findByHasilSorting(Boolean status, String start, String end){
        return this.prosesKomponenRepo.findByHasilSorting(status, start,  end);
    };

    public List<Object[]> findProdukDistinct(Boolean status, String start, String end){
        return this.prosesKomponenRepo.findProdukDistinct(status, start,  end);
    };

    public List<ProsesKomponen> findByProduk(String namaProduk) {
        return this.prosesKomponenRepo.findByProduk(namaProduk);
    }

    public ProsesKomponen findOneById (String id) {
        return this.prosesKomponenRepo.findOneById(id);
    }
    
}
