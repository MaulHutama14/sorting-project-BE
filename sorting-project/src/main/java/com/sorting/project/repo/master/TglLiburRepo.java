package com.sorting.project.repo.master;

import com.sorting.project.model.master.TanggalLibur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TglLiburRepo extends JpaRepository<TanggalLibur, String> {

}
