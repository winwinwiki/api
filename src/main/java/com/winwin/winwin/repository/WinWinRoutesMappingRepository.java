/**
 * 
 */
package com.winwin.winwin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.winwin.winwin.entity.WinWinRoutesMapping;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Repository
public interface WinWinRoutesMappingRepository extends JpaRepository<WinWinRoutesMapping, Long> {
	@Query(value = "select * from winwin_routes_mapping where is_active = true", nativeQuery = true)
	public List<WinWinRoutesMapping> findAllActiveRoutes();

}
