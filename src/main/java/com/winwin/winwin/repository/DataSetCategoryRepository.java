package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.DataSetCategory;

@Transactional
@Repository
public interface DataSetCategoryRepository extends JpaRepository<DataSetCategory, Long> {

}
