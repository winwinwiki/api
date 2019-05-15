package com.winwin.winwin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.NaicsData;
import com.winwin.winwin.repository.OrgNaicsDataRepository;

@Service
public class OrgNaicsDataService {

	@Autowired
	private OrgNaicsDataRepository naicsDataRepository;

	public List<NaicsData> getAllOrgNaicsData() {
		return naicsDataRepository.findAll();
	}
}
