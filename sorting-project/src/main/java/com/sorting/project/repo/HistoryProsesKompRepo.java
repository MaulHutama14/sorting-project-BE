package com.sorting.project.repo;

import com.sorting.project.model.HistoryProsesKomponen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryProsesKompRepo extends JpaRepository<HistoryProsesKomponen, String> {

    @Query("SELECT a FROM HistoryProsesKomponen a WHERE a.createdOn=(SELECT max(a.createdOn) FROM HistoryProsesKomponen a)")
    public HistoryProsesKomponen findLatest();

}
