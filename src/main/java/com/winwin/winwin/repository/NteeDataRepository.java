package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.NteeData;

@Repository
public interface NteeDataRepository extends JpaRepository<NteeData, Long> {
	NteeData findByCode(String code);

}
