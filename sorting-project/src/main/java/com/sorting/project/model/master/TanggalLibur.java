package com.sorting.project.model.master;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="TANGGAL_LIBUR")
public class TanggalLibur {

    public TanggalLibur() {
        this.id = UUID.randomUUID().toString();
    }

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DESKRIPSI_LIBUR")
    private String deskLibur;

    @Column(name = "TANGGAL_LIBUR")
    private Date tanggalLibur;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeskLibur() {
        return deskLibur;
    }

    public void setDeskLibur(String deskLibur) {
        this.deskLibur = deskLibur;
    }

    public Date getTanggalLibur() {
        return tanggalLibur;
    }

    public void setTanggalLibur(Date tanggalLibur) {
        this.tanggalLibur = tanggalLibur;
    }
}
