package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.winwin.winwin.entity.ResourceCategory;

@Transactional
@Repository
public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Long> {

}
