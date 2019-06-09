package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.DataSetCategory;

/**
 * @author ArvindKhatik
 *
 */
@Transactional
@Repository
public interface DataSetCategoryRepository extends JpaRepository<DataSetCategory, Long> {
	@Query(value = "select * from dataset_category where id = :id", nativeQuery = true)
	DataSetCategory findDataSetCategoryById(@Param("id") Long id);

}
