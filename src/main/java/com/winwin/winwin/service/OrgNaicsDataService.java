package com.winwin.winwin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.repository.NaicsDataRepository;

@Service
public class OrgNaicsDataService {

	@Autowired
	private NaicsDataRepository naicsDataRepository;

	public List<NaicsData> getAllOrgNaicsData() {
		return naicsDataRepository.findAllNaicsData();
	}
}
