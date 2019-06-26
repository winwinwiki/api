package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.ResourceCategory;

/**
 * @author ArvindKhatik
 *
 */
@Repository
public interface ResourceCategoryRepository extends JpaRepository<ResourceCategory, Long> {
	@Query(value = "select * from resource_category where id = :id", nativeQuery = true)
	ResourceCategory findResourceCategoryById(@Param("id") Long id);

}
