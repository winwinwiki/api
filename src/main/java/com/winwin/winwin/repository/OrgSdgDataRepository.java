package com.winwin.winwin.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winwin.winwin.entity.SdgData;

/**
 * @author ArvindKhatik
 *
 */
public interface OrgSdgDataRepository extends JpaRepository<SdgData, Long> {
}