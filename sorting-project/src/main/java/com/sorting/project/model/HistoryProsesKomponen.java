package com.sorting.project.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="TX_PROSES_KOMPONEN_HISTORY")
public class HistoryProsesKomponen {

    public HistoryProsesKomponen() {
        this.id = UUID.randomUUID().toString();
        this.createdOn = new Date();
    }

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name="HISTORY_PROSES_KOMPONEN", nullable = false)
    private String historyProsesKomponen;

    @ManyToOne
    @JoinColumn(name="CREATED_BY",referencedColumnName = "USER_NAME", nullable = false)
    private User createdBy;

    @Column(name = "CREATED_ON", nullable = false)
    private Date createdOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHistoryProsesKomponen() {
        return historyProsesKomponen;
    }

    public void setHistoryProsesKomponen(String historyProsesKomponen) {
        this.historyProsesKomponen = historyProsesKomponen;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
