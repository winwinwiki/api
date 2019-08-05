package com.winwin.winwin.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.OrganizationDataSet;
import com.winwin.winwin.entity.OrganizationNote;
import com.winwin.winwin.entity.OrganizationRegionServed;
import com.winwin.winwin.entity.OrganizationResource;
import com.winwin.winwin.entity.OrganizationSdgData;
import com.winwin.winwin.entity.OrganizationSpiData;
import com.winwin.winwin.entity.Program;
import com.winwin.winwin.entity.ProgramDataSet;
import com.winwin.winwin.entity.ProgramRegionServed;
import com.winwin.winwin.entity.ProgramResource;
import com.winwin.winwin.entity.ProgramSdgData;
import com.winwin.winwin.entity.ProgramSpiData;
import com.winwin.winwin.entity.WinWinRoutesMapping;
import com.winwin.winwin.payload.AddressElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationDataSetElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationNoteElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationRegionServedElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationResourceElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationSdgElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationSpiElasticSearchPayload;
import com.winwin.winwin.payload.ProgramDataSetElasticSearchPayload;
import com.winwin.winwin.payload.ProgramElasticSearchPayload;
import com.winwin.winwin.payload.ProgramRegionServedElasticSearchPayload;
import com.winwin.winwin.payload.ProgramResourceElasticSearchPayload;
import com.winwin.winwin.payload.ProgramSdgElasticSearchPayload;
import com.winwin.winwin.payload.ProgramSpiElasticSearchPayload;
import com.winwin.winwin.repository.OrgSdgDataMapRepository;
import com.winwin.winwin.repository.OrgSpiDataMapRepository;
import com.winwin.winwin.repository.OrganizationDataSetRepository;
import com.winwin.winwin.repository.OrganizationNoteRepository;
import com.winwin.winwin.repository.OrganizationRegionServedRepository;
import com.winwin.winwin.repository.OrganizationRepository;
import com.winwin.winwin.repository.OrganizationResourceRepository;
import com.winwin.winwin.repository.ProgramDataSetRepository;
import com.winwin.winwin.repository.ProgramRegionServedRepository;
import com.winwin.winwin.repository.ProgramRepository;
import com.winwin.winwin.repository.ProgramResourceRepository;
import com.winwin.winwin.repository.ProgramSdgDataMapRepository;
import com.winwin.winwin.repository.ProgramSpiDataMapRepository;
import com.winwin.winwin.repository.WinWinRoutesMappingRepository;
import com.winwin.winwin.service.WinWinElasticSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Service
public class WinWinElasticSearchServiceImpl implements WinWinElasticSearchService {

	@Autowired
	OrganizationRepository organizationRepository;
	@Autowired
	ProgramRepository programRepository;
	@Autowired
	OrganizationNoteRepository organizationNoteRepository;
	@Autowired
	OrganizationDataSetRepository organizationDataSetRepository;
	@Autowired
	OrganizationResourceRepository organizationResourceRepository;
	@Autowired
	OrganizationRegionServedRepository organizationRegionServedRepository;
	@Autowired
	OrgSpiDataMapRepository orgSpiDataMapRepository;
	@Autowired
	OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	ProgramDataSetRepository programDataSetRepository;
	@Autowired
	ProgramResourceRepository programResourceRepository;
	@Autowired
	ProgramRegionServedRepository programRegionServedRepository;
	@Autowired
	ProgramSpiDataMapRepository programSpiDataMapRepository;
	@Autowired
	ProgramSdgDataMapRepository programSdgDataMapRepository;
	@Autowired
	WinWinRoutesMappingRepository winWinRoutesMappingRepository;

	static final EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();

	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinElasticSearchServiceImpl.class);

	private Map<String, String> winwinRoutesMap = null;

	/**
	 * send organization's data to Elastic Search
	 */
	@Override
	@Async
	public void sendPostRequestToElasticSearch() {
		// send organization data to elastic
		try {
			LOGGER.info("process: sendPostRequestToElasticSearch has been started successfully");
			Integer numOfOrganizations = 40;
			// Integer numOfOrganizations =
			// organizationRepository.findAllOrganizationsCount();
			Integer pageSize = 1000;
			Integer pageNumAvailable = numOfOrganizations / pageSize;
			Integer totalPageNumAvailable = null;
			if ((Math.floorMod(numOfOrganizations, pageSize)) > 0)
				totalPageNumAvailable = pageNumAvailable + 1;

			for (int pageNum = 0; pageNum < totalPageNumAvailable; pageNum++) {
				// set page number and page size for organization
				Pageable pageable = PageRequest.of(pageNum, pageSize);
				LOGGER.info(
						"sending data to Elastic Search from " + (pageNum + 1) + " to " + ((pageNum + 1) * pageSize));
				sendDataToElasticSearch(pageable);
				LOGGER.info("data has been sent successfully to Elastic Search from " + (pageNum + 1) + " to "
						+ ((pageNum + 1) * pageSize) + " ");
			} // end of loop
			LOGGER.info("process: sendPostRequestToElasticSearch has been ended successfully");
		} catch (Exception e) {
			LOGGER.error("exception occoured while sending post request to ElasticSearch", e);
		}

	}

	/**
	 * @throws IOException
	 * @param pageable
	 */
	@Async
	private void sendDataToElasticSearch(Pageable pageable) throws IOException {
		final String serviceName = "es";
		final String region = System.getenv("AWS_REGION2");
		final String index = System.getenv("AWS_ES_INDEX");
		final String type = System.getenv("AWS_ES_INDEX_TYPE");

		// fetch all the data of organization by pageNum and pageSize
		List<OrganizationElasticSearchPayload> organizationPayloadList = prepareDataForElasticSearch(pageable);
		RestHighLevelClient esClient = esClient(serviceName, region);

		for (OrganizationElasticSearchPayload payload : organizationPayloadList) {
			final String id = "organization_" + payload.getId().toString();
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper objectMapper = new ObjectMapper();

			// get Organization object as a JSON string
			String jsonStr = objectMapper.writeValueAsString(payload);

			// Create the document as a hash map from JSON string
			@SuppressWarnings("unchecked")
			Map<String, String> document = objectMapper.readValue(jsonStr, Map.class);

			// Form the indexing request, send it, and print the response
			IndexRequest request = new IndexRequest(index, type, id).source(document);
			try {
				LOGGER.info("sending data of organization id : " + payload.getId() + " to ElasticSearch");
				IndexResponse response = esClient.index(request, RequestOptions.DEFAULT);
				LOGGER.info("data of organization id : " + payload.getId()
						+ " has been successfully sent to ElasticSearch index with id as: " + response.getId());

			} catch (ElasticsearchException e) {
				if (e.status() == RestStatus.CONFLICT) {
					LOGGER.error("exception occoured due to conflict while sending post request to ElasticSearch", e);
				}
			}
		}

	}

	private List<OrganizationElasticSearchPayload> prepareDataForElasticSearch(Pageable pageable) {
		List<OrganizationElasticSearchPayload> organizationPayloadList = new ArrayList<OrganizationElasticSearchPayload>();
		List<Organization> organizationList = new ArrayList<Organization>();

		try {
			File file = new File("winwin_elasticSearch_log.txt");
			// Create the file
			LOGGER.info("fetching details from elastic search log File: " + file.getName());

			FileWriter txtWriter = new FileWriter(file, true);
			String fileContent = FileUtils.readFileToString(file, "UTF-8");
			Date lastUpdatedDate = null;
			Date currentUpdatedDate = null;

			if (!StringUtils.isEmpty(fileContent)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				lastUpdatedDate = sdf.parse(fileContent);
			}

			/*
			 * find all the organizations to send into ElasticSearch if
			 * lastUpdatedDate is not found else find all the organizations from
			 * lastUpdatedDate
			 */
			if (null != pageable) {
				if (lastUpdatedDate == null) {
					organizationList = organizationRepository.findAllOrganizations(pageable);
				} else {
					organizationList = organizationRepository.findAllOrganizationsFromLastUpdatedDate(pageable,
							lastUpdatedDate);
				}

			}

			/*
			 * List<Long> ids = new ArrayList<Long>(); ids.add(37353L);
			 * 
			 * for (Long id : ids) { organizationList = new
			 * ArrayList<Organization>(); Organization organization =
			 * organizationRepository.findOrgById(id);
			 * 
			 * if (null != organization) organizationList.add(organization);
			 */

			if (null != organizationList) {
				// set Organization Map
				Map<Long, Organization> organizationMap = organizationList.stream()
						.collect(Collectors.toMap(Organization::getId, Organization -> Organization));

				// using for-each loop for iteration over Map.entrySet()
				for (Map.Entry<Long, Organization> organizationFromMap : organizationMap.entrySet()) {
					currentUpdatedDate = organizationFromMap.getValue().getUpdatedAt();

					if (lastUpdatedDate == null) {
						lastUpdatedDate = organizationFromMap.getValue().getUpdatedAt();
						if (file.createNewFile()) {
						} else {
							LOGGER.info("deleting existing  elastic search log File:: " + file.getName());
							file.delete();
							LOGGER.info(
									" elastic search log File:  " + file.getName() + " has been successfully deleted");

							LOGGER.info("creating again  elastic search log File: " + file.getName());
							file.createNewFile();
							LOGGER.info(
									" elastic search log File: " + file.getName() + " is successfully created again!");
						}
						txtWriter.write(lastUpdatedDate.toString());
					}

					if (null != currentUpdatedDate && null != lastUpdatedDate) {
						if (currentUpdatedDate.compareTo(lastUpdatedDate) > 0) {
							lastUpdatedDate = organizationFromMap.getValue().getUpdatedAt();
							if (file.createNewFile()) {
							} else {
								LOGGER.info("deleting existing  elastic search log File:: " + file.getName());
								file.delete();
								LOGGER.info(" elastic search log File:  " + file.getName()
										+ " has been successfully deleted");

								LOGGER.info("creating again  elastic search log File: " + file.getName());
								file.createNewFile();
								LOGGER.info(" elastic search log File: " + file.getName()
										+ " is successfully created again!");
							}
							txtWriter.write(currentUpdatedDate.toString());
						}
					}

					OrganizationElasticSearchPayload organizationPayload = new OrganizationElasticSearchPayload();
					// copy organization values to organizationPayload
					BeanUtils.copyProperties(organizationFromMap.getValue(), organizationPayload);

					// copy remaining organization values to
					// organizationPayload
					if (null != organizationFromMap.getValue().getNaicsCode())
						organizationPayload.setNaics_code(organizationFromMap.getValue().getNaicsCode().getCode());

					if (null != organizationFromMap.getValue().getNteeCode())
						organizationPayload.setNtee_code(organizationFromMap.getValue().getNteeCode().getCode());

					if (null != organizationFromMap.getValue().getParentId()) {
						// find parentOrganization first in map if not found
						// then make DB call
						Organization parentOrganization = organizationMap
								.get(organizationFromMap.getValue().getParentId());

						if (parentOrganization == null)
							parentOrganization = organizationRepository
									.findOrgById(organizationFromMap.getValue().getParentId());

						if (null != parentOrganization)
							organizationPayload.setParentOrganizationName(parentOrganization.getName());

					}

					// set adminUrl for organization
					if (winwinRoutesMap == null) {
						// set winWin routes map
						setWinWinRoutesMap();
					}
					if (null != winwinRoutesMap) {
						if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
								&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)) {
							organizationPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
									+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
									+ organizationFromMap.getValue().getId());
						}

					}

					setOrganizationAddress(organizationFromMap, organizationPayload);

					setOrganizationNotes(organizationFromMap, organizationPayload);

					setOrganizationDataSets(organizationFromMap, organizationPayload);

					setOrganizationResources(organizationFromMap, organizationPayload);

					setOrganizationRegionServed(organizationFromMap, organizationPayload);

					setOrganizationSpiData(organizationFromMap, organizationPayload);

					setOrganizationSdgData(organizationFromMap, organizationPayload);

					// set all the programs of an organization
					setOrganizationPrograms(organizationFromMap, organizationPayload);

					// add organizationPayload to organizationPayloadList
					organizationPayloadList.add(organizationPayload);
				}

			} // end of if (null != organizationList) {

			// }

			// flush the changes and close txtWriter
			txtWriter.flush();
			txtWriter.close();

		} catch (BeansException e) {
			LOGGER.error("exception occoured while sending post request to ElasticSearch", e);
		} catch (IOException e) {
			LOGGER.error("exception occoured while sending post request to ElasticSearch", e);
		} catch (ParseException e) {
			LOGGER.error("exception occoured while sending post request to ElasticSearch", e);
		}

		return organizationPayloadList;

	}

	/**
	 * set FrontEnd Routes for WINWIN
	 */
	private void setWinWinRoutesMap() {
		winwinRoutesMap = new HashMap<String, String>();
		List<WinWinRoutesMapping> activeRoutes = winWinRoutesMappingRepository.findAllActiveRoutes();
		if (null != activeRoutes)
			winwinRoutesMap = activeRoutes.stream()
					.collect(Collectors.toMap(WinWinRoutesMapping::getKey, WinWinRoutesMapping::getValue));
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationPrograms(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all programs of an organization
		List<Program> programList = programRepository.findAllProgramList(organizationFromMap.getValue().getId());
		List<ProgramElasticSearchPayload> programPayloadList = new ArrayList<ProgramElasticSearchPayload>();

		if (null != programList) {
			// set Organization Map
			Map<Long, Program> programMap = programList.stream()
					.collect(Collectors.toMap(Program::getId, Program -> Program));

			// using for-each loop for iteration over Map.entrySet()
			for (Map.Entry<Long, Program> programFromMap : programMap.entrySet()) {
				ProgramElasticSearchPayload programPayload = new ProgramElasticSearchPayload();
				// copy program values to programPayload
				BeanUtils.copyProperties(programFromMap.getValue(), programPayload);

				// copy remaining program values to programPayload
				setProgramDataSets(programFromMap, programPayload);

				setProgramResources(programFromMap, programPayload);

				setProgramRegionServed(programFromMap, programPayload);

				setProgramSpiData(programFromMap, programPayload);

				setProgramSdgData(programFromMap, programPayload);

				// set adminUrl for organization programs
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)) {
						programPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId());
					}
				}

				// set programPayload into programPayloadList
				programPayloadList.add(programPayload);

			}

		} // end of if (null != programList) {

		// set programPayloadList to organizationPayload
		organizationPayload.setPrograms(programPayloadList);
	}// end of method

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramSdgData(Map.Entry<Long, Program> programFromMap,
			ProgramElasticSearchPayload programPayload) {
		// fetch all sdgDataMapping of an program
		List<ProgramSdgData> programSdgDataMappingList = programSdgDataMapRepository
				.getProgramSdgMapDataByOrgId(programFromMap.getValue().getId());
		List<ProgramSdgElasticSearchPayload> sdgDataMappingPayloadList = new ArrayList<ProgramSdgElasticSearchPayload>();

		if (null != programSdgDataMappingList) {
			for (ProgramSdgData sdgDataMapping : programSdgDataMappingList) {
				ProgramSdgElasticSearchPayload sdgDataMappingPayload = new ProgramSdgElasticSearchPayload();
				// copy sdgDataMapping values to sdgDataMappingPayload
				BeanUtils.copyProperties(sdgDataMapping, sdgDataMappingPayload);

				// copy remaining sdgDataMapping values to
				// sdgDataMappingPayload
				if (null != sdgDataMapping.getSdgData())
					sdgDataMappingPayload.setName(sdgDataMapping.getSdgData().getShortName());
				sdgDataMappingPayload.setCode(sdgDataMapping.getSdgData().getShortNameCode());

				// set adminUrl for programs SDG TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.SDG_TAGS)
							&& null != programFromMap.getValue().getOrganization()) {
						sdgDataMappingPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ programFromMap.getValue().getOrganization().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.SDG_TAGS));
					}
				}

				// set sdgDataMappingPayload to
				// sdgDataMappingPayloadList
				sdgDataMappingPayloadList.add(sdgDataMappingPayload);
			}
		}
		// set sdgDataMappingPayloadList to programPayload
		programPayload.setSdg(sdgDataMappingPayloadList);
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramSpiData(Map.Entry<Long, Program> programFromMap,
			ProgramElasticSearchPayload programPayload) {
		// fetch all spiDataMapping of an program
		List<ProgramSpiData> programSpiDataMappingList = programSpiDataMapRepository
				.getProgramSpiMapDataByOrgId(programFromMap.getValue().getId());
		List<ProgramSpiElasticSearchPayload> spiDataMappingPayloadList = new ArrayList<ProgramSpiElasticSearchPayload>();

		if (null != programSpiDataMappingList) {
			for (ProgramSpiData spiDataMapping : programSpiDataMappingList) {
				ProgramSpiElasticSearchPayload spiDataMappingPayload = new ProgramSpiElasticSearchPayload();
				// copy spiDataMapping values to spiDataMappingPayload
				BeanUtils.copyProperties(spiDataMapping, spiDataMappingPayload);

				// copy remaining spiDataMapping values to
				// spiDataMappingPayload
				if (null != spiDataMapping.getSpiData())
					spiDataMappingPayload.setName(spiDataMapping.getSpiData().getIndicatorName());

				// set adminUrl for programs SPI TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.SPI_TAGS)
							&& null != programFromMap.getValue().getOrganization()) {
						spiDataMappingPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ programFromMap.getValue().getOrganization().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.SPI_TAGS));
					}
				}

				// set spiDataMappingPayload to
				// spiDataMappingPayloadList
				spiDataMappingPayloadList.add(spiDataMappingPayload);
			}
		}
		// set spiDataMappingPayloadList to programPayload
		programPayload.setSpi(spiDataMappingPayloadList);
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramRegionServed(Map.Entry<Long, Program> programFromMap,
			ProgramElasticSearchPayload programPayload) {
		// fetch all regionServed of an program
		List<ProgramRegionServed> programRegionServedList = programRegionServedRepository
				.findAllActiveProgramRegions(programFromMap.getValue().getId());
		List<ProgramRegionServedElasticSearchPayload> regionServedPayloadList = new ArrayList<ProgramRegionServedElasticSearchPayload>();

		if (null != programRegionServedList) {
			for (ProgramRegionServed regionServed : programRegionServedList) {
				ProgramRegionServedElasticSearchPayload regionServedPayload = new ProgramRegionServedElasticSearchPayload();
				// copy regionServed values to regionServedPayload
				BeanUtils.copyProperties(regionServed, regionServedPayload);

				// copy remaining regionServed values to
				// regionServedPayload
				if (null != regionServed.getRegionMaster())
					regionServedPayload.setName(regionServed.getRegionMaster().getRegionName());

				// set adminUrl for programs regionServed
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.REGIONS_SERVED)
							&& null != programFromMap.getValue().getOrganization()) {
						regionServedPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ programFromMap.getValue().getOrganization().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.REGIONS_SERVED));
					}
				}

				// set regionServedPayload to resourcePayloadList
				regionServedPayloadList.add(regionServedPayload);
			}
		}
		// set regionServedPayloadList to programPayload
		programPayload.setRegionServed(regionServedPayloadList);
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramResources(Map.Entry<Long, Program> programFromMap,
			ProgramElasticSearchPayload programPayload) {
		// fetch all active resources of an program
		List<ProgramResource> programResourceList = programResourceRepository
				.findAllActiveProgramResources(programFromMap.getValue().getId());
		List<ProgramResourceElasticSearchPayload> resourcePayloadList = new ArrayList<ProgramResourceElasticSearchPayload>();

		if (null != programResourceList) {
			for (ProgramResource resource : programResourceList) {
				ProgramResourceElasticSearchPayload resourcePayload = new ProgramResourceElasticSearchPayload();
				// copy resource values to resourcePayload
				BeanUtils.copyProperties(resource, resourcePayload);

				// copy remaining resource values to resourcePayload
				if (null != resource.getResourceCategory())
					resourcePayload.setName(resource.getResourceCategory().getCategoryName());

				// set adminUrl for programs resources
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.RESOURCES)
							&& null != programFromMap.getValue().getOrganization()) {
						resourcePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ programFromMap.getValue().getOrganization().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.RESOURCES));
					}
				}

				// set resourcePayload to resourcePayloadList
				resourcePayloadList.add(resourcePayload);
			}
		}
		// set resourcePayloadList to programPayload
		programPayload.setResources(resourcePayloadList);
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramDataSets(Map.Entry<Long, Program> programFromMap,
			ProgramElasticSearchPayload programPayload) {
		// fetch all active datasets of an program
		List<ProgramDataSet> programDataSetList = programDataSetRepository
				.findAllActiveProgramDataSets(programFromMap.getValue().getId());
		List<ProgramDataSetElasticSearchPayload> dataSetPayloadList = new ArrayList<ProgramDataSetElasticSearchPayload>();

		if (null != programDataSetList) {
			for (ProgramDataSet dataset : programDataSetList) {
				ProgramDataSetElasticSearchPayload dataSetPayload = new ProgramDataSetElasticSearchPayload();
				// copy dataSet values to dataSetPayload
				BeanUtils.copyProperties(dataset, dataSetPayload);

				// copy remaining dataSet values to dataSetPayload
				if (null != dataset.getDataSetCategory())
					dataSetPayload.setName(dataset.getDataSetCategory().getCategoryName());

				// set adminUrl for programs resources
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.DATASETS)
							&& null != programFromMap.getValue().getOrganization()) {
						dataSetPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ programFromMap.getValue().getOrganization().getId()
								+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
								+ programFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.DATASETS));
					}
				}

				// set dataSetPayload to dataSetPayloadList
				dataSetPayloadList.add(dataSetPayload);
			}
		}
		// set dataSetPayloadList to programPayload
		programPayload.setDatasets(dataSetPayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationSdgData(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all sdgDataMapping of an organization
		List<OrganizationSdgData> organizationSdgDataMappingList = orgSdgDataMapRepository
				.getOrgSdgMapDataByOrgId(organizationFromMap.getValue().getId());
		List<OrganizationSdgElasticSearchPayload> sdgDataMappingPayloadList = new ArrayList<OrganizationSdgElasticSearchPayload>();

		if (null != organizationSdgDataMappingList) {
			for (OrganizationSdgData sdgDataMapping : organizationSdgDataMappingList) {
				OrganizationSdgElasticSearchPayload sdgDataMappingPayload = new OrganizationSdgElasticSearchPayload();
				// copy sdgDataMapping values to sdgDataMappingPayload
				BeanUtils.copyProperties(sdgDataMapping, sdgDataMappingPayload);

				// copy remaining sdgDataMapping values to
				// sdgDataMappingPayload
				if (null != sdgDataMapping.getSdgData())
					sdgDataMappingPayload.setName(sdgDataMapping.getSdgData().getShortName());
				sdgDataMappingPayload.setCode(sdgDataMapping.getSdgData().getShortNameCode());

				// set adminUrl for organization SDG TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.SDG_TAGS)) {
						sdgDataMappingPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.SDG_TAGS));
					}
				}

				// set sdgDataMappingPayload to
				// sdgDataMappingPayloadList
				sdgDataMappingPayloadList.add(sdgDataMappingPayload);
			}
		}
		// set sdgDataMappingPayloadList to organizationPayload
		organizationPayload.setSdg(sdgDataMappingPayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationSpiData(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all spiDataMapping of an organization
		List<OrganizationSpiData> organizationSpiDataMappingList = orgSpiDataMapRepository
				.getOrgSpiMapDataByOrgId(organizationFromMap.getValue().getId());
		List<OrganizationSpiElasticSearchPayload> spiDataMappingPayloadList = new ArrayList<OrganizationSpiElasticSearchPayload>();

		if (null != organizationSpiDataMappingList) {
			for (OrganizationSpiData spiDataMapping : organizationSpiDataMappingList) {
				OrganizationSpiElasticSearchPayload spiDataMappingPayload = new OrganizationSpiElasticSearchPayload();
				// copy spiDataMapping values to spiDataMappingPayload
				BeanUtils.copyProperties(spiDataMapping, spiDataMappingPayload);

				// copy remaining spiDataMapping values to
				// spiDataMappingPayload
				if (null != spiDataMapping.getSpiData())
					spiDataMappingPayload.setName(spiDataMapping.getSpiData().getIndicatorName());

				// set adminUrl for organization SPI TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.SPI_TAGS)) {
						spiDataMappingPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.SPI_TAGS));
					}
				}

				// set spiDataMappingPayload to
				// spiDataMappingPayloadList
				spiDataMappingPayloadList.add(spiDataMappingPayload);
			}
		}
		// set spiDataMappingPayloadList to organizationPayload
		organizationPayload.setSpi(spiDataMappingPayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationRegionServed(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all regionServed of an organization
		List<OrganizationRegionServed> organizationRegionServedList = organizationRegionServedRepository
				.findAllActiveOrgRegions(organizationFromMap.getValue().getId());
		List<OrganizationRegionServedElasticSearchPayload> regionServedPayloadList = new ArrayList<OrganizationRegionServedElasticSearchPayload>();

		if (null != organizationRegionServedList) {
			for (OrganizationRegionServed regionServed : organizationRegionServedList) {
				OrganizationRegionServedElasticSearchPayload regionServedPayload = new OrganizationRegionServedElasticSearchPayload();
				// copy regionServed values to regionServedPayload
				BeanUtils.copyProperties(regionServed, regionServedPayload);

				// copy remaining regionServed values to regionServedPayload
				if (null != regionServed.getRegionMaster())
					regionServedPayload.setName(regionServed.getRegionMaster().getRegionName());

				// set adminUrl for organization regionServed
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.REGIONS_SERVED)) {
						regionServedPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.REGIONS_SERVED));
					}
				}

				// set regionServedPayload to resourcePayloadList
				regionServedPayloadList.add(regionServedPayload);
			}
		}
		// set regionServedPayloadList to organizationPayload
		organizationPayload.setRegionServed(regionServedPayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationResources(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all active resources of an organization
		List<OrganizationResource> organizationResourceList = organizationResourceRepository
				.findAllActiveOrgResources(organizationFromMap.getValue().getId());
		List<OrganizationResourceElasticSearchPayload> resourcePayloadList = new ArrayList<OrganizationResourceElasticSearchPayload>();

		if (null != organizationResourceList) {
			for (OrganizationResource resource : organizationResourceList) {
				OrganizationResourceElasticSearchPayload resourcePayload = new OrganizationResourceElasticSearchPayload();
				// copy resource values to resourcePayload
				BeanUtils.copyProperties(resource, resourcePayload);

				// copy remaining resource values to resourcePayload
				if (null != resource.getResourceCategory())
					resourcePayload.setName(resource.getResourceCategory().getCategoryName());

				// set adminUrl for organization resources
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.RESOURCES)) {
						resourcePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.RESOURCES));
					}
				}

				// set resourcePayload to resourcePayloadList
				resourcePayloadList.add(resourcePayload);
			}
		}
		// set resourcePayloadList to organizationPayload
		organizationPayload.setResources(resourcePayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationDataSets(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all active dataSets of an organization
		List<OrganizationDataSet> organizationDataSetList = organizationDataSetRepository
				.findAllActiveOrgDataSets(organizationFromMap.getValue().getId());
		List<OrganizationDataSetElasticSearchPayload> dataSetPayloadList = new ArrayList<OrganizationDataSetElasticSearchPayload>();

		if (null != organizationDataSetList) {
			for (OrganizationDataSet dataset : organizationDataSetList) {
				OrganizationDataSetElasticSearchPayload dataSetPayload = new OrganizationDataSetElasticSearchPayload();
				// copy dataSet values to dataSetPayload
				BeanUtils.copyProperties(dataset, dataSetPayload);

				// copy remaining dataSet values to dataSetPayload
				if (null != dataset.getDataSetCategory())
					dataSetPayload.setName(dataset.getDataSetCategory().getCategoryName());

				// set adminUrl for organization dataSets
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.DATASETS)) {
						dataSetPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.DATASETS));
					}
				}

				// set dataSetPayload to dataSetPayloadList
				dataSetPayloadList.add(dataSetPayload);
			}
		}
		// set dataSetPayloadList to organizationPayload
		organizationPayload.setDatasets(dataSetPayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationNotes(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		// fetch all notes of an organization
		List<OrganizationNote> organizationNoteList = organizationNoteRepository
				.findAllOrgNotesList(organizationFromMap.getValue().getId());
		List<OrganizationNoteElasticSearchPayload> notePayloadList = new ArrayList<OrganizationNoteElasticSearchPayload>();

		if (null != organizationNoteList) {
			for (OrganizationNote note : organizationNoteList) {
				OrganizationNoteElasticSearchPayload notePayload = new OrganizationNoteElasticSearchPayload();
				// copy note values to notePayload
				BeanUtils.copyProperties(note, notePayload);

				// set adminUrl for organization notes
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap) {
					if (winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
							&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
							&& winwinRoutesMap.containsKey(OrganizationConstants.NOTES)) {
						notePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organizationFromMap.getValue().getId()
								+ winwinRoutesMap.get(OrganizationConstants.NOTES));
					}
				}

				// set notePayload to notePayloadList
				notePayloadList.add(notePayload);
			}
		}
		// set notePayloadList to organizationPayload
		organizationPayload.setNotes(notePayloadList);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationAddress(Map.Entry<Long, Organization> organizationFromMap,
			OrganizationElasticSearchPayload organizationPayload) {
		if (null != organizationFromMap.getValue().getAddress()) {
			AddressElasticSearchPayload addressPayload = new AddressElasticSearchPayload();
			// copy organization address values to addressPayload
			BeanUtils.copyProperties(organizationFromMap.getValue().getAddress(), addressPayload);
			// set addressPayload to organizationPayload
			organizationPayload.setAddress(addressPayload);
		}
	}

	// Adds the interceptor to the ES REST client
	public static RestHighLevelClient esClient(String serviceName, String region) {
		AWS4Signer signer = new AWS4Signer();
		signer.setServiceName(serviceName);
		signer.setRegionName(region);
		HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer,
				envCredentialsProvider);
		return new RestHighLevelClient(RestClient.builder(HttpHost.create(System.getenv("AWS_ES_ENDPOINT")))
				.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
	}

}
