package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.winwin.winwin.entity.Classification;

/**
 * @author ArvindKhatik
 *
 */
@Repository
public interface ClassificationRepository extends JpaRepository<Classification, Long> {
	@Query(value = "select * from classification where id = :id", nativeQuery = true)
	Classification findClassificationById(@Param("id") Long id);
}
