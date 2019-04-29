package com.winwin.winwin.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.winwin.winwin.entity.OrgNteeData;
import com.winwin.winwin.repository.OrgNteeDataRepository;

@Service
public class OrgNteeDataService {

	@Autowired
	private OrgNteeDataRepository nteeDataRepository;

	public List<OrgNteeData> getAllOrgNteeData() {
		return nteeDataRepository.findAll();
	}
}
