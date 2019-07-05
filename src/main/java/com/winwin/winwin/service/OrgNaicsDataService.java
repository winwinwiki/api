package com.winwin.winwin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.repository.NaicsDataRepository;

@Service
public class OrgNaicsDataService {

	@Autowired
	private NaicsDataRepository naicsDataRepository;

	@Cacheable("naics_data_list")
	public List<NaicsData> getAllOrgNaicsData() {
		return naicsDataRepository.findAllNaicsData();
	}
}
