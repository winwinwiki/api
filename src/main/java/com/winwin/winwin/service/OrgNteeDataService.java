package com.winwin.winwin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.NteeData;
import com.winwin.winwin.repository.NteeDataRepository;

@Service
public class OrgNteeDataService {

	@Autowired
	private NteeDataRepository nteeDataRepository;

	@CachePut(value="ntee_data_list")
	public List<NteeData> getAllOrgNteeData() {
		return nteeDataRepository.findAllNteeData();
	}
}
