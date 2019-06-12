package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.SdgData;

/**
 * @author ArvindKhatik
 *
 */

@Transactional
@Repository
public interface SdgDataRepository extends JpaRepository<SdgData, Long> {
	@Query(value = "select * from sdg_data where id = :id", nativeQuery = true)
	SdgData findSdgObjById(@Param("id") Long id);

	@Query(value = "select * from sdg_data where is_active = true", nativeQuery = true )
	List<SdgData> findAllSdgData();
}