package com.winwin.winwin.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.RestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.http.AWSRequestSigningApacheInterceptor;
import com.winwin.winwin.Logger.CustomMessageSource;
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
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.entity.WinWinRoutesMapping;
import com.winwin.winwin.payload.AddressElasticSearchPayload;
import com.winwin.winwin.payload.DataSetElasticSearchPayload;
import com.winwin.winwin.payload.FrameworkElasticSearchPayload;
import com.winwin.winwin.payload.NotesElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationDataSetElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationFrameworksPayload;
import com.winwin.winwin.payload.OrganizationNoteElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationRegionServedElasticSearchPayload;
import com.winwin.winwin.payload.OrganizationResourceElasticSearchPayload;
import com.winwin.winwin.payload.RegionServedElasticSearchPayload;
import com.winwin.winwin.payload.ResourceElasticSearchPayload;
import com.winwin.winwin.payload.UserPayload;
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
import com.winwin.winwin.service.SlackNotificationSenderService;
import com.winwin.winwin.service.WinWinElasticSearchService;
import com.winwin.winwin.util.CommonUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Service
public class WinWinElasticSearchServiceImpl implements WinWinElasticSearchService {
	@Autowired
	private CustomMessageSource customMessageSource;
	@Autowired
	private OrganizationRepository organizationRepository;
	@Autowired
	private ProgramRepository programRepository;
	@Autowired
	private OrganizationNoteRepository organizationNoteRepository;
	@Autowired
	private OrganizationDataSetRepository organizationDataSetRepository;
	@Autowired
	private OrganizationResourceRepository organizationResourceRepository;
	@Autowired
	private OrganizationRegionServedRepository organizationRegionServedRepository;
	@Autowired
	private OrgSpiDataMapRepository orgSpiDataMapRepository;
	@Autowired
	private OrgSdgDataMapRepository orgSdgDataMapRepository;
	@Autowired
	private ProgramDataSetRepository programDataSetRepository;
	@Autowired
	private ProgramResourceRepository programResourceRepository;
	@Autowired
	private ProgramRegionServedRepository programRegionServedRepository;
	@Autowired
	private ProgramSpiDataMapRepository programSpiDataMapRepository;
	@Autowired
	private ProgramSdgDataMapRepository programSdgDataMapRepository;
	@Autowired
	private WinWinRoutesMappingRepository winWinRoutesMappingRepository;
	@Autowired
	private SlackNotificationSenderService slackNotificationSenderService;

	private static final EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();
	private static final Logger LOGGER = LoggerFactory.getLogger(WinWinElasticSearchServiceImpl.class);
	private Map<String, String> winwinRoutesMap = null;

	private static final String ORG_INDEX = System.getenv("AWS_ES_ORG_INDEX");
	private static final String RESOURCE_INDEX = System.getenv("AWS_ES_RES_INDEX");
	private static final String DATASET_INDEX = System.getenv("AWS_ES_DS_INDEX");
	private static final String FRAMEWORK_INDEX = System.getenv("AWS_ES_FW_INDEX");
	private static final String REGION_SERVED_INDEX = System.getenv("AWS_ES_RS_INDEX");
	private static final String NOTES_INDEX = System.getenv("AWS_ES_NOTES_INDEX");

	private static final String ES_ENDPOINT = System.getenv("AWS_ES_ENDPOINT");
	private static final String ES_ENDPOINT_PORT = System.getenv("AWS_ES_ENDPOINT_PORT");
	private static final String ES_ENDPOINT_SCHEME = System.getenv("AWS_ES_ENDPOINT_SCHEME");
	private static final String APP_ENV_NAME = System.getenv("WINWIN_ENV");
	private static final String INITIATED_BY = "initiated by user: ";
	private static final String SPI_TYPE = "spi";
	private static final String SDG_TYPE = "sdg";

	@Value("${slack.channel}")
	private String slackChannelName;

	/**
	 * send organization's data to Elastic Search
	 */
	@Override
	@Async
	public void sendPostRequestToElasticSearch(UserPayload user) {
		SlackMessage slackMessage = null;
		FileWriter txtWriter = null;
		// send organization data to elastic
		try {
			LOGGER.info("process: sendPostRequestToElasticSearch has been started successfully");

			// for Slack Notification
			Date date = CommonUtils.getFormattedDate();
			slackMessage = SlackMessage.builder().username("WinWinMessageNotifier")
					.text("WinWinWiki Publish To Kibana Process has been started successfully for app env: "
							+ APP_ENV_NAME + " , " + INITIATED_BY + "" + user.getUserDisplayName() + " at " + date)
					.channel(slackChannelName).as_user("true").build();
			slackNotificationSenderService.sendSlackMessageNotification(slackMessage);

			File file = new File("winwin_elasticSearch_log.txt");
			// Create the file
			LOGGER.info("fetching details from elastic search log File: {} ", file.getName());
			txtWriter = new FileWriter(file, true);
			String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8.toString());
			Date lastUpdatedDate = null;

			if (!StringUtils.isEmpty(fileContent)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				lastUpdatedDate = sdf.parse(fileContent);
			}
			/*
			 * find all the organizations to send into ElasticSearch if lastUpdatedDate is
			 * not found else find all the organizations from lastUpdatedDate
			 */
			Integer numOfOrganizations = null;
			if (lastUpdatedDate == null) {
				numOfOrganizations = organizationRepository.findAllOrganizationsCount();
			} else {
				numOfOrganizations = organizationRepository
						.findAllOrganizationsCountFromLastUpdatedDate(lastUpdatedDate);
			}

			if (null != numOfOrganizations) {
				Integer pageSize = 1500;
				Integer pageNumAvailable = numOfOrganizations / pageSize;
				Integer totalPageNumAvailable = null;
				if ((Math.floorMod(numOfOrganizations, pageSize)) > 0)
					totalPageNumAvailable = pageNumAvailable + 1;
				else
					totalPageNumAvailable = pageNumAvailable;

				// check index exist status and create index if not exist
				int shardValue = 5;
				int replicaValue = 1;

				if (!isIndexExist(ORG_INDEX)) {
					createIndex(ORG_INDEX, shardValue, replicaValue);
				}

				if (!isIndexExist(RESOURCE_INDEX)) {
					createIndex(RESOURCE_INDEX, shardValue, replicaValue);
				}

				if (!isIndexExist(DATASET_INDEX)) {
					createIndex(DATASET_INDEX, shardValue, replicaValue);
				}

				if (!isIndexExist(FRAMEWORK_INDEX)) {
					createIndex(FRAMEWORK_INDEX, shardValue, replicaValue);
				}

				if (!isIndexExist(REGION_SERVED_INDEX)) {
					createIndex(REGION_SERVED_INDEX, shardValue, replicaValue);
				}

				if (!isIndexExist(NOTES_INDEX)) {
					createIndex(NOTES_INDEX, shardValue, replicaValue);
				}

				/*
				 * check if numOfOrganizations > 100 than set individual replica to 0 to
				 * increase index write efficiency in bulk
				 */
				if (numOfOrganizations > 100) {
					updateIndexSettings(ORG_INDEX, 0);
					updateIndexSettings(RESOURCE_INDEX, 0);
					updateIndexSettings(DATASET_INDEX, 0);
					updateIndexSettings(FRAMEWORK_INDEX, 0);
					updateIndexSettings(REGION_SERVED_INDEX, 0);
					updateIndexSettings(NOTES_INDEX, 0);
				}

				for (int pageNum = 0; pageNum < totalPageNumAvailable; pageNum++) {
					// set page number and page size for organization
					Pageable pageable = PageRequest.of(pageNum, pageSize);
					LOGGER.info("sending data to Elastic Search Indexes: {},{},{},{},{},{} from {} to {}", ORG_INDEX,
							RESOURCE_INDEX, DATASET_INDEX, FRAMEWORK_INDEX, REGION_SERVED_INDEX, NOTES_INDEX,
							((pageNum * pageSize) + 1), ((pageNum + 1) * pageSize));
					// send data to ES
					sendDataToElasticSearch(pageable, file, txtWriter, lastUpdatedDate);
					LOGGER.info(
							"data has been sent successfully to Elastic Search Indexes: {},{},{},{},{},{} from {} to {}",
							ORG_INDEX, RESOURCE_INDEX, DATASET_INDEX, FRAMEWORK_INDEX, REGION_SERVED_INDEX, NOTES_INDEX,
							((pageNum * pageSize) + 1), ((pageNum + 1) * pageSize));
				} // end of loop
			}
			LOGGER.info("process: sendPostRequestToElasticSearch has been ended successfully");
			date = CommonUtils.getFormattedDate();
			slackMessage.setText(("WinWinWiki Publish To Kibana Process has been ended successfully for app env: "
					+ APP_ENV_NAME + " , " + INITIATED_BY + "" + user.getUserDisplayName() + " at " + date));
			slackNotificationSenderService.sendSlackMessageNotification(slackMessage);

			/*
			 * check if numOfOrganizations > 100 than set individual replica to 1 once the
			 * bulk index write process ends
			 */
			if (numOfOrganizations > 100) {
				updateIndexSettings(ORG_INDEX, 1);
				updateIndexSettings(RESOURCE_INDEX, 1);
				updateIndexSettings(DATASET_INDEX, 1);
				updateIndexSettings(FRAMEWORK_INDEX, 1);
				updateIndexSettings(REGION_SERVED_INDEX, 1);
				updateIndexSettings(NOTES_INDEX, 1);
			}
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.es.post"), e);
			Date date = CommonUtils.getFormattedDate();
			slackMessage.setText(("WinWinWiki Publish To Kibana Process has failed to run for app env: " + APP_ENV_NAME
					+ " , " + INITIATED_BY + "" + user.getUserDisplayName() + " at " + date + " due to error: \n"
					+ e.getMessage()));
			slackNotificationSenderService.sendSlackMessageNotification(slackMessage);
		} finally {
			try {
				// flush the changes and close txtWriter
				if (null != txtWriter) {
					txtWriter.flush();
					txtWriter.close();
				}

			} catch (IOException e) {
				LOGGER.error("exception occoured while writing updated date into ElasticSearch file ", e);
			}

		}
	}

	private void updateIndexSettings(String index, int replicaValue) throws Exception {
		UpdateSettingsRequest request = new UpdateSettingsRequest(index);
		String settingKey1 = "index.number_of_replicas";
		String settingKey2 = "index.blocks.read_only_allow_delete";
		Settings settings = Settings.builder().put(settingKey1, replicaValue).put(settingKey2, false).build();

		// set settings to request
		request.settings(settings);

		try {

			// check index exist status and create index if not exist
			if (!isIndexExist(index)) {
				createIndex(index, 5, 1);
			}

			// set post request for ElasticSearch
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {
				// get rest client connection
				RestHighLevelClient esClient = esClientForEC2HostedElasticSearch(
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD));
				AcknowledgedResponse updateSettingsResponse = esClient.indices().putSettings(request,
						RequestOptions.DEFAULT);

				// check for index settings updated successfully
				if (Boolean.TRUE.equals(updateSettingsResponse.isAcknowledged()))
					LOGGER.info("Index Settings updated for Index: {}", index);
				else
					LOGGER.info("Index Settings not updated for Index: {} ", index);

				// close client
				esClient.close();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to update index settings for index: {}", index, e);
			// throw exception to main method
			throw e;
		}
	}

	private boolean isIndexExist(String index) throws Exception {
		GetIndexRequest request = new GetIndexRequest(index);
		boolean exists = false;

		try {
			// set post request for ElasticSearch
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {
				// get rest client connection
				RestHighLevelClient esClient = esClientForEC2HostedElasticSearch(
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD));

				exists = esClient.indices().exists(request, RequestOptions.DEFAULT);
				// close client
				esClient.close();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to update index settings for index: {}", index, e);
			// throw exception to main method
			throw e;
		}

		return exists;
	}

	private void createIndex(String index, int shardValue, int replicaValue) throws Exception {
		CreateIndexRequest request = new CreateIndexRequest(index);
		String settingKey1 = "index.number_of_replicas";
		String settingKey2 = "index.number_of_shards";
		Settings settings = Settings.builder().put(settingKey1, shardValue).put(settingKey2, replicaValue).build();

		// set settings to request
		request.settings(settings);

		try {
			// set post request for ElasticSearch
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {
				// get rest client connection
				RestHighLevelClient esClient = esClientForEC2HostedElasticSearch(
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD));
				CreateIndexResponse createIndexResponse = esClient.indices().create(request, RequestOptions.DEFAULT);

				// check for index settings updated successfully
				if (Boolean.TRUE.equals(createIndexResponse.isAcknowledged()))
					LOGGER.info("Index Settings created for Index: {}", index);
				else
					LOGGER.info("Index Settings not created for Index: {} ", index);

				// close client
				esClient.close();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to create index settings for index: {}", index, e);
			// throw exception to main method
			throw e;
		}
	}

	/**
	 * @throws IOException
	 * @param pageable
	 */
	private void sendDataToElasticSearch(Pageable pageable, File file, FileWriter txtWriter, Date lastUpdatedDate)
			throws Exception {
		try {
			Boolean terminated = false;
			// fetch all the data of organization by pageNum and pageSize
			List<OrganizationElasticSearchPayload> organizationPayloadList = prepareDataForElasticSearch(pageable, file,
					txtWriter, lastUpdatedDate);
			// Send bulk indexes data to individual indexes
			// get bulkProcessor instance to add multiple IndexRequest
			BulkProcessor bulkProcessor = getBulkProcessor();

			for (OrganizationElasticSearchPayload payload : organizationPayloadList) {
				String id = "org_" + payload.getId().toString();
				// check for program
				if (Boolean.TRUE.equals(isProgram(payload)))
					id = "prog_" + payload.getId().toString();

				if (null != payload.getResources() && (!payload.getResources().isEmpty())) {
					// send organization and program resources
					sendOrgAndProgramResourcesToES(bulkProcessor, payload);
				}

				if (null != payload.getDatasets() && (!payload.getDatasets().isEmpty())) {
					// send organization and program DataSets
					sendOrgAndProgramDatasetsToES(bulkProcessor, payload);
				}

				if (null != payload.getFramework() && (!payload.getFramework().isEmpty())) {
					// send organization and program frameworks
					sendOrgAndProgramFrameworksToES(bulkProcessor, payload);
				}

				if (null != payload.getRegionServed() && (!payload.getRegionServed().isEmpty())) {
					// send organization and program region served
					// asynchronously
					sendOrgAndProgramRegionServedToES(bulkProcessor, payload);
				}

				if (null != payload.getNotes() && (!payload.getNotes().isEmpty())) {
					// send organization notes
					sendOrgNotesToES(bulkProcessor, payload);
				}
				// Creating Object of ObjectMapper define in JACKSON API
				ObjectMapper mapper = new ObjectMapper();
				// get Organization object as a JSON string
				String jsonStr = mapper.writeValueAsString(payload);
				// add data to bulk processor
				addDataToBulkProcessor(bulkProcessor, mapper, ORG_INDEX, id, jsonStr);
			} // end of loop

			// close the bulk processor
			if (null != bulkProcessor)
				terminated = bulkProcessor.awaitClose(5L, TimeUnit.MINUTES);
			if (Boolean.TRUE.equals(terminated))
				LOGGER.info("Bulk Processor Terminated Successfuly for current batch");
			else
				LOGGER.warn("Bulk Processor Terminated Unsuccessfuly for current batch");

		} catch (ElasticsearchException e) {
			if (e.status() == RestStatus.CONFLICT) {
				LOGGER.error("exception occoured due to conflict while sending post request to ElasticSearch", e);
			}
			// throw exception to main method
			throw e;
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.es.post"), e);
			// throw exception to main method
			throw e;
		}

	}

	/**
	 * @param bulkProcessor
	 * @param payload
	 * @throws IOException
	 */
	private void sendOrgNotesToES(BulkProcessor bulkProcessor, OrganizationElasticSearchPayload payload)
			throws IOException {
		for (OrganizationNoteElasticSearchPayload note : payload.getNotes()) {
			String noteId = "org_note_" + note.getId().toString();
			// check for program
			if (Boolean.TRUE.equals(isProgram(payload)))
				noteId = "prog_note_" + note.getId().toString();
			// copy organization and note properties to notePayload
			NotesElasticSearchPayload notePayload = new NotesElasticSearchPayload();
			notePayload.setNotes(note);
			BeanUtils.copyProperties(payload, notePayload);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper mapper = new ObjectMapper();
			// get notePayload object as a JSON string
			String jsonStr = mapper.writeValueAsString(notePayload);
			// add data to bulk processor
			addDataToBulkProcessor(bulkProcessor, mapper, NOTES_INDEX, noteId, jsonStr);
		}
	}

	/**
	 * @param bulkProcessor
	 * @param payload
	 * @throws IOException
	 */
	private void sendOrgAndProgramRegionServedToES(BulkProcessor bulkProcessor,
			OrganizationElasticSearchPayload payload) throws IOException {
		for (OrganizationRegionServedElasticSearchPayload regionServed : payload.getRegionServed()) {
			String regionServedId = "org_rs_" + regionServed.getId().toString();
			// check for program
			if (Boolean.TRUE.equals(isProgram(payload)))
				regionServedId = "prog_rs_" + regionServed.getId().toString();
			// copy organization and regionServed properties to
			// regionServedPayload
			RegionServedElasticSearchPayload regionServedPayload = new RegionServedElasticSearchPayload();
			regionServedPayload.setRegionServed(regionServed);
			BeanUtils.copyProperties(payload, regionServedPayload);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper mapper = new ObjectMapper();
			// get regionServedPayload object as a JSON string
			String jsonStr = mapper.writeValueAsString(regionServedPayload);
			// add data to bulk processor
			addDataToBulkProcessor(bulkProcessor, mapper, REGION_SERVED_INDEX, regionServedId, jsonStr);
		}
	}

	/**
	 * @param bulkProcessor
	 * @param payload
	 * @throws IOException
	 */
	private void sendOrgAndProgramFrameworksToES(BulkProcessor bulkProcessor, OrganizationElasticSearchPayload payload)
			throws IOException {
		for (OrganizationFrameworksPayload framework : payload.getFramework()) {
			String frameworkId = "org_fw_" + framework.getType() + "_" + framework.getId().toString();
			// check for program
			if (Boolean.TRUE.equals(isProgram(payload)))
				frameworkId = "prog_fw_" + framework.getType() + "_" + framework.getId().toString();
			// copy organization and framework properties to
			// frameworkPayload
			FrameworkElasticSearchPayload frameworkPayload = new FrameworkElasticSearchPayload();
			// set tagName by sdg and spi tag
			if (!StringUtils.isEmpty(framework.getType()) && framework.getType().equalsIgnoreCase(SPI_TYPE)) {
				framework.setTagName(framework.getIndicator());
			} else if ((!StringUtils.isEmpty(framework.getType()) && framework.getType().equalsIgnoreCase(SDG_TYPE))) {
				framework.setTagName(framework.getShortName());
			}
			frameworkPayload.setFramework(framework);
			BeanUtils.copyProperties(payload, frameworkPayload);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper mapper = new ObjectMapper();
			// get frameworkPayload object as a JSON string
			String jsonStr = mapper.writeValueAsString(frameworkPayload);
			// add data to bulk processor
			addDataToBulkProcessor(bulkProcessor, mapper, FRAMEWORK_INDEX, frameworkId, jsonStr);
		}
	}

	/**
	 * @param bulkProcessor
	 * @param payload
	 * @throws IOException
	 */
	private void sendOrgAndProgramDatasetsToES(BulkProcessor bulkProcessor, OrganizationElasticSearchPayload payload)
			throws IOException {
		for (OrganizationDataSetElasticSearchPayload dataset : payload.getDatasets()) {
			String datasetId = "org_ds_" + dataset.getId().toString();
			// check for program
			if (Boolean.TRUE.equals(isProgram(payload)))
				datasetId = "prog_ds_" + dataset.getId().toString();
			// copy organization and dataset properties to
			// datasetPayload
			DataSetElasticSearchPayload datasetPayload = new DataSetElasticSearchPayload();
			datasetPayload.setDataset(dataset);
			BeanUtils.copyProperties(payload, datasetPayload);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper mapper = new ObjectMapper();
			// get datasetPayload object as a JSON string
			String jsonStr = mapper.writeValueAsString(datasetPayload);
			// add data to bulk processor
			addDataToBulkProcessor(bulkProcessor, mapper, DATASET_INDEX, datasetId, jsonStr);
		}
	}

	/**
	 * @param bulkProcessor
	 * @param payload
	 * @throws IOException
	 */
	private void sendOrgAndProgramResourcesToES(BulkProcessor bulkProcessor, OrganizationElasticSearchPayload payload)
			throws IOException {
		for (OrganizationResourceElasticSearchPayload resource : payload.getResources()) {
			String resourceId = "org_res_" + resource.getId().toString();
			// check for program
			if (Boolean.TRUE.equals(isProgram(payload)))
				resourceId = "prog_res_" + resource.getId().toString();
			// copy organization and resource properties to
			// resourcePayload
			ResourceElasticSearchPayload resourcePayload = new ResourceElasticSearchPayload();
			resourcePayload.setResource(resource);
			BeanUtils.copyProperties(payload, resourcePayload);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper mapper = new ObjectMapper();
			// get resourcePayload object as a JSON string
			String jsonStr = mapper.writeValueAsString(resourcePayload);
			// add data to bulk processor
			addDataToBulkProcessor(bulkProcessor, mapper, RESOURCE_INDEX, resourceId, jsonStr);
		}
	}

	/**
	 * @return
	 * @throws Exception
	 */
	private BulkProcessor getBulkProcessor() throws Exception {
		try {
			// set post request for ElasticSearch
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {
				// get rest client connection
				RestHighLevelClient esClient = esClientForEC2HostedElasticSearch(
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD));

				BulkProcessor.Listener listener = new BulkProcessor.Listener() {
					@Override
					public void beforeBulk(long executionId, BulkRequest request) {
						int numberOfActions = request.numberOfActions();
						LOGGER.debug("Executing bulk [{}] with {} requests", executionId, numberOfActions);
					}

					@Override
					public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
						if (response.hasFailures()) {
							LOGGER.warn("Bulk [{}] executed with failures", executionId);
						} else {
							LOGGER.debug("Bulk [{}] completed in {} milliseconds", executionId,
									response.getTook().getMillis());
						}
					}

					@Override
					public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
						LOGGER.error("Failed to execute bulk", failure);
					}
				};

				BulkProcessor.Builder builder = BulkProcessor.builder(
						(request, bulkListener) -> esClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
						listener);
				// send to ElasticSearch when it has 10000 Index Request at a
				// time
				builder.setBulkActions(10000);
				// send to ElasticSearch when it has 5MB size 0f Index Request
				// at a time
				builder.setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB));
				builder.setConcurrentRequests(100);
				// set a flush interval flushing any BulkRequest pending if the
				// interval passes
				// to 30 Seconds
				builder.setFlushInterval(TimeValue.timeValueSeconds(30L));
				// set a constant back off policy that initially waits for 100
				// milliseconds and
				// retries up to 3 times
				builder.setBackoffPolicy(BackoffPolicy.constantBackoff(TimeValue.timeValueMillis(100L), 3));
				return builder.build();
			}

		} catch (Exception e) {
			LOGGER.error("Failed to create bulkProcessor ", e);
			// throw exception to main method
			throw e;
		}

		return null;
	}

	/**
	 * @param payload
	 * @param id
	 * @return
	 */
	private Boolean isProgram(OrganizationElasticSearchPayload payload) {
		Boolean isProgram = false;
		if ((!StringUtils.isEmpty(payload.getProgramOrOrgType()))
				&& payload.getProgramOrOrgType().equalsIgnoreCase(OrganizationConstants.PROGRAM))
			isProgram = true;
		return isProgram;
	}

	/**
	 * @param bulkProcessor
	 * @param mapper
	 * @param id
	 * @param jsonStr
	 * @throws IOException
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 */
	private void addDataToBulkProcessor(BulkProcessor bulkProcessor, ObjectMapper mapper, String index, String id,
			String jsonStr) throws IOException {
		if (null != bulkProcessor) {
			// Create the document as a hash map from JSON string
			@SuppressWarnings("unchecked")
			Map<String, String> document = mapper.readValue(jsonStr, Map.class);
			// Form the indexing request, send it, and print the
			// response
			IndexRequest request = new IndexRequest(index).id(id).source(document);
			// Add individual index request to bulk processor
			bulkProcessor.add(request);
		}
	}

	private List<OrganizationElasticSearchPayload> prepareDataForElasticSearch(Pageable pageable, File file,
			FileWriter txtWriter, Date lastUpdatedDate) throws Exception {
		List<OrganizationElasticSearchPayload> organizationPayloadList = new ArrayList<>();
		List<Organization> organizationList = new ArrayList<>();

		try {
			/*
			 * find all the organizations to send into ElasticSearch if lastUpdatedDate is
			 * not found else find all the organizations from lastUpdatedDate
			 */
			if (null != pageable) {
				if (lastUpdatedDate == null) {
					organizationList = organizationRepository.findAllOrganizations(pageable);
				} else {
					organizationList = organizationRepository.findAllOrganizationsFromLastUpdatedDate(pageable,
							lastUpdatedDate);
				}
			}

			if (null != organizationList) {
				// set Organization Map
				Map<Long, Organization> organizationMap = organizationList.stream()
						.collect(Collectors.toMap(Organization::getId, Organization -> Organization));

				// using for-each loop for iteration over Map.entrySet()
				for (Map.Entry<Long, Organization> organizationFromMap : organizationMap.entrySet()) {
					Organization parentOrganization = null;
					Organization rootParentOrganization = null;
					Organization organization = organizationFromMap.getValue();
					// check for parent Organization
					if (null != organization.getParentId()) {
						// find parentOrganization first in map if not found
						// then make DB call
						parentOrganization = organizationMap.get(organization.getParentId());

						if (parentOrganization == null)
							parentOrganization = organizationRepository.findOrgById(organization.getParentId());

					} else {
						// if no parent found than make org itself as parent
						// according to beth.roberts requirement
						parentOrganization = organization;
					}

					// check for root parent Organization
					if (null != organization.getRootParentId()) {
						// find rootParentOrganization first in map if not
						// found
						// then make DB call
						rootParentOrganization = organizationMap.get(organization.getRootParentId());

						if (rootParentOrganization == null)
							rootParentOrganization = organizationRepository.findOrgById(organization.getRootParentId());
					} else {
						// if no root parent found than make org itself as root
						// parent according to beth.roberts requirement
						rootParentOrganization = organization;
					}

					// check for all organization to push the data into elastic
					prepareDataByTagStatus(organizationPayloadList, file, txtWriter, lastUpdatedDate, organizationMap,
							organization, parentOrganization, rootParentOrganization);

					/*
					 * Commented due to new requirement by jens // check for root organization to
					 * push the data into elastic // search if (parentOrganization == null &&
					 * rootParentOrganization == null) { String tagStatus =
					 * organizationFromMap.getValue().getTagStatus();
					 * 
					 * if (!StringUtils.isEmpty(tagStatus) &&
					 * tagStatus.equals(OrganizationConstants.COMPLETE_TAG)) {
					 * prepareDataByTagStatus(organizationPayloadList, file, txtWriter,
					 * lastUpdatedDate, organizationMap, organizationFromMap, parentOrganization,
					 * rootParentOrganization); } // check for child organization to push the data
					 * into // elastic search } else if (null != parentOrganization && null !=
					 * rootParentOrganization) { prepareDataByTagStatus(organizationPayloadList,
					 * file, txtWriter, lastUpdatedDate, organizationMap, organizationFromMap,
					 * parentOrganization, rootParentOrganization); }
					 */

				} // end of loop

			} // end of if

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.exception.es.post"), e);
			throw e;
		}
		return organizationPayloadList;
	}

	/**
	 * @param organizationPayloadList
	 * @param file
	 * @param txtWriter
	 * @param lastUpdatedDate
	 * @param organizationMap
	 * @param organization
	 * @throws IOException
	 */
	private void prepareDataByTagStatus(List<OrganizationElasticSearchPayload> organizationPayloadList, File file,
			FileWriter txtWriter, Date lastUpdatedDate, Map<Long, Organization> organizationMap,
			Organization organization, Organization parentOrganization, Organization rootParentOrganization)
			throws Exception {
		Date currentUpdatedDate = organization.getUpdatedAt();

		if (lastUpdatedDate == null && organization.getUpdatedAt() != null) {
			lastUpdatedDate = organization.getUpdatedAt();
			if (file.createNewFile()) {
				LOGGER.info("creating elastic search log File: {}", file.getName());
			} else {
				LOGGER.info("clearing content from existing  elastic search log File: {}", file.getName());
				PrintWriter writer = new PrintWriter(file);
				writer.print("");
				writer.close();
			}
			// write last Updated Date
			txtWriter.write(lastUpdatedDate.toString());
			// flush the changes into txtWriter
			txtWriter.flush();
		}
		if (null != currentUpdatedDate && null != lastUpdatedDate
				&& currentUpdatedDate.compareTo(lastUpdatedDate) > 0) {
			lastUpdatedDate = currentUpdatedDate;
			if (file.createNewFile()) {
				LOGGER.info("creating elastic search log File: {}", file.getName());
			} else {
				LOGGER.info("clearing content from existing  elastic search log File: {}", file.getName());
				PrintWriter writer = new PrintWriter(file);
				writer.print("");
				writer.close();
			}
			// write last Updated Date
			txtWriter.write(lastUpdatedDate.toString());
			// flush the changes into txtWriter
			txtWriter.flush();
		}

		if (null != organization) {
			OrganizationElasticSearchPayload organizationPayload = new OrganizationElasticSearchPayload();
			// copy organization values to organizationPayload
			BeanUtils.copyProperties(organization, organizationPayload);
			// copy remaining organization values to
			// organizationPayload
			organizationPayload.setProgramOrOrgName(organization.getName());
			organizationPayload.setProgramOrOrgDescription(organization.getDescription());
			organizationPayload.setProgramOrOrgType(organization.getType());

			if (null != organization.getNaicsCode())
				organizationPayload.setNaics_code(organization.getNaicsCode().getCode());
			if (null != organization.getNteeCode())
				organizationPayload.setNtee_code(organization.getNteeCode().getCode());
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			// check for parent Organization
			if (null != parentOrganization) {
				organizationPayload.setParentOrgId(parentOrganization.getId());
				organizationPayload.setParentOrgName(parentOrganization.getName());
				organizationPayload.setParentOrgDescription(parentOrganization.getDescription());
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)) {
					organizationPayload.setParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + parentOrganization.getId());
				}
			}
			// check for root parent Organization
			if (null != rootParentOrganization) {
				organizationPayload.setTopParentOrgId(rootParentOrganization.getId());
				organizationPayload.setTopParentOrgName(rootParentOrganization.getName());
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)) {
					organizationPayload.setTopParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ rootParentOrganization.getId());
				}
			}
			// set adminUrl for organization
			if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)) {
				organizationPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId());
			}
			setOrganizationAddress(organization, organizationPayload);
			setOrganizationNotes(organization, organizationPayload);
			setOrganizationDataSets(organization, organizationPayload);
			setOrganizationResources(organization, organizationPayload);
			setOrganizationRegionServed(organization, organizationPayload);
			setOrganizationSpiData(organization, organizationPayload);
			setOrganizationSdgData(organization, organizationPayload);
			// set all the connectedOrganizations of an organization
			setConnectedOrganizations(organization, organizationMap, organizationPayload);
			// add organizationPayload to organizationPayloadList
			organizationPayloadList.add(organizationPayload);
			//// add all organization programs to organizationPayloadList
			organizationPayloadList.addAll(getOrganizationPrograms(organization, organizationMap, organizationPayload));
		}
	}

	/**
	 * set FrontEnd Routes for WINWIN
	 */
	private void setWinWinRoutesMap() throws Exception {
		winwinRoutesMap = new HashMap<>();
		List<WinWinRoutesMapping> activeRoutes = winWinRoutesMappingRepository.findAllActiveRoutes();
		if (null != activeRoutes)
			winwinRoutesMap = activeRoutes.stream()
					.collect(Collectors.toMap(WinWinRoutesMapping::getKey, WinWinRoutesMapping::getValue));
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setConnectedOrganizations(Organization organization, Map<Long, Organization> organizationMap,
			OrganizationElasticSearchPayload organizationPayload) throws Exception {
		List<String> connectedOrganizations = new ArrayList<>();
		// Add parent organization name
		if (null != organization.getParentId())
			connectedOrganizations = getParentOrgNames(organization, organizationMap, connectedOrganizations);
		// Add leaf organization Name at Last
		connectedOrganizations.add(organization.getName());
		// set connected organization list to organizationPayload
		organizationPayload.setConnectedOrganizations(connectedOrganizations);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setProgramConnectedOrganizations(Program program, Organization organization,
			Map<Long, Organization> organizationMap, OrganizationElasticSearchPayload programPayload) throws Exception {
		List<String> connectedOrganizations = new ArrayList<>();
		// Add organization's parent organizations name
		if (null != organization.getParentId())
			connectedOrganizations = getParentOrgNames(organization, organizationMap, connectedOrganizations);
		// Add program's parent org name
		connectedOrganizations.add(organization.getName());
		// Add program Name at Last
		connectedOrganizations.add(program.getName());
		// set connected organization list to programPayload
		programPayload.setConnectedOrganizations(connectedOrganizations);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationMap
	 * @param connectedOrganizations
	 * @return
	 */
	private List<String> getParentOrgNames(Organization organization, Map<Long, Organization> organizationMap,
			List<String> connectedOrganizations) {
		Organization parentOrganization = null;
		// check for parent Organization
		if (null != organization.getParentId()) {
			// find parentOrganization first in map if not found
			// then make DB call
			parentOrganization = organizationMap.get(organization.getParentId());

			if (parentOrganization == null)
				parentOrganization = organizationRepository.findOrgById(organization.getParentId());
			if (null != parentOrganization) {
				if (null != parentOrganization.getParentId()) {
					connectedOrganizations = getParentOrgNames(parentOrganization, organizationMap,
							connectedOrganizations);
					connectedOrganizations.add(parentOrganization.getName());
				} else {
					connectedOrganizations.add(parentOrganization.getName());
				}

			}
		}
		return connectedOrganizations;
	}// end of method

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private List<OrganizationElasticSearchPayload> getOrganizationPrograms(Organization organization,
			Map<Long, Organization> organizationMap, OrganizationElasticSearchPayload organizationPayload)
			throws Exception {
		// fetch all programs of an organization
		List<Program> programList = programRepository.findAllProgramList(organization.getId());
		List<OrganizationElasticSearchPayload> programPayloadList = new ArrayList<>();

		if (null != programList) {
			// set Organization Map
			Map<Long, Program> programMap = programList.stream()
					.collect(Collectors.toMap(Program::getId, Program -> Program));

			// using for-each loop for iteration over Map.entrySet()
			for (Map.Entry<Long, Program> programFromMap : programMap.entrySet()) {
				Program program = programFromMap.getValue();
				OrganizationElasticSearchPayload programPayload = new OrganizationElasticSearchPayload();
				// copy program values to programPayload
				BeanUtils.copyProperties(program, programPayload);
				// copy remaining program values to programPayload
				programPayload.setProgramOrOrgName(program.getName());
				programPayload.setProgramOrOrgDescription(program.getDescription());
				programPayload.setProgramOrOrgType(OrganizationConstants.PROGRAM);

				if (null != organization) {
					programPayload.setParentOrgId(organization.getId());
					programPayload.setParentOrgName(organization.getName());
					programPayload.setParentOrgDescription(organization.getDescription());
					programPayload.setSector(organization.getSector());
					programPayload.setSectorLevel(organization.getSectorLevel());
					programPayload.setSectorLevelName(organization.getSectorLevelName());

					if (null != organizationPayload)
						programPayload.setAddress(organizationPayload.getAddress());
					// check for root parent Organization
					if (null != organization.getRootParentId()) {
						programPayload.setTopParentOrgId(organization.getRootParentId());
						// find root parentOrganization first in map if not
						// found
						// then make DB call
						Organization rootParent = organizationMap.get(organization.getRootParentId());
						if (rootParent == null)
							rootParent = organizationRepository.findOrgById(organization.getRootParentId());
						if (null != rootParent)
							programPayload.setTopParentOrgName(rootParent.getName());
					} else if (null != organization.getParentId()) {
						// find root parentOrganization first in map if not
						// found
						// then make DB call
						Organization rootParent = organizationMap.get(organization.getParentId());
						if (rootParent == null)
							rootParent = organizationRepository.findOrgById(organization.getParentId());

						if (null != rootParent) {
							programPayload.setTopParentOrgId(rootParent.getId());
							programPayload.setTopParentOrgName(rootParent.getName());
						}
					} else {
						// set programs parent as root parent
						programPayload.setTopParentOrgId(organization.getId());
						programPayload.setTopParentOrgName(organization.getName());
					}
				}
				setProgramDataSets(program, programPayload);
				setProgramResources(program, programPayload);
				setProgramRegionServed(program, programPayload);
				setProgramSpiData(program, programPayload);
				setProgramSdgData(program, programPayload);
				// set all the setProgramConnectedOrganizations of an
				// organization
				setProgramConnectedOrganizations(program, organization, organizationMap, programPayload);

				// set adminUrl for organization programs
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)) {
					programPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.PROGRAMS) + program.getId());

					programPayload.setParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId());

					programPayload.setParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId());

					if (null != organization.getRootParentId())
						programPayload.setTopParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
								+ organization.getRootParentId());
					else
						programPayload.setTopParentOrgUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
								+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId());
				}
				// set programPayload into programPayloadList
				programPayloadList.add(programPayload);

			}

		} // end of if

		// set programPayloadList to add in the organizationPayloadList
		return programPayloadList;
	}// end of method

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramSdgData(Program program, OrganizationElasticSearchPayload programPayload) throws Exception {
		// fetch all sdgDataMapping of an program
		List<ProgramSdgData> programSdgDataMappingList = programSdgDataMapRepository
				.getProgramSdgMapDataByOrgId(program.getId());
		List<OrganizationFrameworksPayload> sdgDataFrameworks = new ArrayList<>();

		if (null != programSdgDataMappingList) {
			for (ProgramSdgData sdgDataMapping : programSdgDataMappingList) {
				OrganizationFrameworksPayload sdgDataFrameworkPayload = new OrganizationFrameworksPayload();
				// copy sdgDataMapping values to sdgDataFrameworkPayload
				BeanUtils.copyProperties(sdgDataMapping, sdgDataFrameworkPayload);
				// copy remaining sdgDataMapping values to
				// sdgDataFrameworkPayload
				setProgramSdgMapping(sdgDataMapping, sdgDataFrameworkPayload);
				// set adminUrl for programs SDG TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.SDG_TAGS)
						&& null != program.getOrganization()) {
					sdgDataFrameworkPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ program.getOrganization().getId() + winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
							+ program.getId() + winwinRoutesMap.get(OrganizationConstants.SDG_TAGS));
				}
				// set sdgDataFrameworkPayload to sdgDataFrameworks
				sdgDataFrameworks.add(sdgDataFrameworkPayload);
			}
		}
		// set sdgDataFrameworks to programPayload
		if (programPayload.getFramework() == null)
			programPayload.setFramework(sdgDataFrameworks);
		else
			programPayload.getFramework().addAll(sdgDataFrameworks);
	}

	/**
	 * @param sdgDataMapping
	 * @param sdgDataFrameworkPayload
	 */
	private void setProgramSdgMapping(ProgramSdgData sdgDataMapping,
			OrganizationFrameworksPayload sdgDataFrameworkPayload) {
		if (null != sdgDataMapping.getSdgData()) {
			sdgDataFrameworkPayload.setName(sdgDataMapping.getSdgData().getGoalName());

			if (null != sdgDataMapping.getSdgData().getGoalCode())
				sdgDataFrameworkPayload.setCode(sdgDataMapping.getSdgData().getGoalCode().toString());

			sdgDataFrameworkPayload.setShortName(
					sdgDataMapping.getSdgData().getShortNameCode() + " " + sdgDataMapping.getSdgData().getShortName());
			sdgDataFrameworkPayload.setShortNameCode(sdgDataMapping.getSdgData().getShortNameCode());
			sdgDataFrameworkPayload.setType(SDG_TYPE);
			sdgDataFrameworkPayload.setTagName(sdgDataMapping.getSdgData().getShortName());
		}
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramSpiData(Program program, OrganizationElasticSearchPayload programPayload) throws Exception {
		// fetch all spiDataMapping of an program
		List<ProgramSpiData> programSpiDataMappingList = programSpiDataMapRepository
				.getProgramSpiMapDataByOrgId(program.getId());
		List<OrganizationFrameworksPayload> spiDataFrameworks = new ArrayList<>();

		if (null != programSpiDataMappingList) {
			for (ProgramSpiData spiDataMapping : programSpiDataMappingList) {
				OrganizationFrameworksPayload spiDataFrameworkPayload = new OrganizationFrameworksPayload();
				// copy spiDataMapping values to spiDataFrameworkPayload
				BeanUtils.copyProperties(spiDataMapping, spiDataFrameworkPayload);
				// copy remaining spiDataMapping values to
				// spiDataFrameworkPayload
				setProgramSpiMapping(spiDataMapping, spiDataFrameworkPayload);
				// set adminUrl for programs SPI TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.SPI_TAGS)
						&& null != program.getOrganization()) {
					spiDataFrameworkPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ program.getOrganization().getId() + winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
							+ program.getId() + winwinRoutesMap.get(OrganizationConstants.SPI_TAGS));
				}
				// set spiDataFrameworkPayload to
				// spiDataFrameworks
				spiDataFrameworks.add(spiDataFrameworkPayload);
			}
		}
		// set spiDataFrameworks to programPayload
		if (programPayload.getFramework() == null)
			programPayload.setFramework(spiDataFrameworks);
		else
			programPayload.getFramework().addAll(spiDataFrameworks);
	}

	/**
	 * @param spiDataMapping
	 * @param spiDataFrameworkPayload
	 */
	private void setProgramSpiMapping(ProgramSpiData spiDataMapping,
			OrganizationFrameworksPayload spiDataFrameworkPayload) {
		if (null != spiDataMapping.getSpiData()) {
			spiDataFrameworkPayload.setDimension(spiDataMapping.getSpiData().getDimensionName());
			spiDataFrameworkPayload.setComponent(spiDataMapping.getSpiData().getComponentName());
			spiDataFrameworkPayload.setIndicator(spiDataMapping.getSpiData().getIndicatorName());
			spiDataFrameworkPayload.setType(SPI_TYPE);
			spiDataFrameworkPayload.setTagName(spiDataMapping.getSpiData().getIndicatorName());
		}
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramRegionServed(Program program, OrganizationElasticSearchPayload programPayload)
			throws Exception {
		// fetch all regionServed of an program
		List<ProgramRegionServed> programRegionServedList = programRegionServedRepository
				.findAllActiveProgramRegions(program.getId());
		List<OrganizationRegionServedElasticSearchPayload> regionServedPayloadList = new ArrayList<>();

		if (null != programRegionServedList) {
			for (ProgramRegionServed regionServed : programRegionServedList) {
				OrganizationRegionServedElasticSearchPayload regionServedPayload = new OrganizationRegionServedElasticSearchPayload();
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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.REGIONS_SERVED)
						&& null != program.getOrganization()) {
					regionServedPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ program.getOrganization().getId() + winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
							+ program.getId() + winwinRoutesMap.get(OrganizationConstants.REGIONS_SERVED));
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
	private void setProgramResources(Program program, OrganizationElasticSearchPayload programPayload)
			throws Exception {
		// fetch all active resources of an program
		List<ProgramResource> programResourceList = programResourceRepository
				.findAllActiveProgramResources(program.getId());
		List<OrganizationResourceElasticSearchPayload> resourcePayloadList = new ArrayList<>();
		List<String> namesofResources = new ArrayList<>();

		if (null != programResourceList) {
			for (ProgramResource resource : programResourceList) {
				OrganizationResourceElasticSearchPayload resourcePayload = new OrganizationResourceElasticSearchPayload();
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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.RESOURCES)
						&& null != program.getOrganization()) {
					resourcePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ program.getOrganization().getId() + winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
							+ program.getId() + winwinRoutesMap.get(OrganizationConstants.RESOURCES));
				}
				// set resourcePayload to resourcePayloadList
				resourcePayloadList.add(resourcePayload);
				// set Resource Name to namesofResources
				namesofResources.add(resourcePayload.getName());
			}
		}
		// set resourcePayloadList to programPayload
		programPayload.setResources(resourcePayloadList);
		// set namesofResources to programPayload
		programPayload.setNamesOfResources(namesofResources);
	}

	/**
	 * @param programFromMap
	 * @param programPayload
	 */
	private void setProgramDataSets(Program program, OrganizationElasticSearchPayload programPayload) throws Exception {
		// fetch all active datasets of an program
		List<ProgramDataSet> programDataSetList = programDataSetRepository
				.findAllActiveProgramDataSets(program.getId());
		List<OrganizationDataSetElasticSearchPayload> dataSetPayloadList = new ArrayList<>();
		List<String> namesofDatasets = new ArrayList<>();

		if (null != programDataSetList) {
			for (ProgramDataSet dataset : programDataSetList) {
				OrganizationDataSetElasticSearchPayload dataSetPayload = new OrganizationDataSetElasticSearchPayload();
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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.PROGRAMS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.DATASETS)
						&& null != program.getOrganization()) {
					dataSetPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS)
							+ program.getOrganization().getId() + winwinRoutesMap.get(OrganizationConstants.PROGRAMS)
							+ program.getId() + winwinRoutesMap.get(OrganizationConstants.DATASETS));
				}
				// set dataSetPayload to dataSetPayloadList
				dataSetPayloadList.add(dataSetPayload);
				// set DataSet Name to namesofDatasets
				namesofDatasets.add(dataSetPayload.getName());
			}
		}
		// set dataSetPayloadList to programPayload
		programPayload.setDatasets(dataSetPayloadList);
		// set namesofDatasets to programPayload
		programPayload.setNamesOfDatasets(namesofDatasets);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationSdgData(Organization organization, OrganizationElasticSearchPayload organizationPayload)
			throws Exception {
		// fetch all sdgDataMapping of an organization
		List<OrganizationSdgData> organizationSdgDataMappingList = orgSdgDataMapRepository
				.getOrgSdgMapDataByOrgId(organization.getId());
		List<OrganizationFrameworksPayload> sdgDataFrameworks = new ArrayList<>();

		if (null != organizationSdgDataMappingList) {
			for (OrganizationSdgData sdgDataMapping : organizationSdgDataMappingList) {
				OrganizationFrameworksPayload sdgDataFrameworkPayload = new OrganizationFrameworksPayload();
				// copy sdgDataMapping values to sdgDataFrameworkPayload
				BeanUtils.copyProperties(sdgDataMapping, sdgDataFrameworkPayload);
				// copy remaining sdgDataMapping values to
				// sdgDataMappingPayload
				setOrgSdgMapping(sdgDataMapping, sdgDataFrameworkPayload);
				// set adminUrl for organization SDG TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.SDG_TAGS)) {
					sdgDataFrameworkPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.SDG_TAGS));
				}
				// set sdgDataFrameworkPayload to sdgDataFrameworks
				sdgDataFrameworks.add(sdgDataFrameworkPayload);
			}
		}
		// set sdgDataFrameworks to organizationPayload
		if (organizationPayload.getFramework() == null)
			organizationPayload.setFramework(sdgDataFrameworks);
		else
			organizationPayload.getFramework().addAll(sdgDataFrameworks);

	}

	/**
	 * @param sdgDataMapping
	 * @param sdgDataFrameworkPayload
	 */
	private void setOrgSdgMapping(OrganizationSdgData sdgDataMapping,
			OrganizationFrameworksPayload sdgDataFrameworkPayload) {
		if (null != sdgDataMapping.getSdgData()) {
			sdgDataFrameworkPayload.setName(sdgDataMapping.getSdgData().getGoalName());

			if (null != sdgDataMapping.getSdgData().getGoalCode())
				sdgDataFrameworkPayload.setCode(sdgDataMapping.getSdgData().getGoalCode().toString());

			sdgDataFrameworkPayload.setShortName(
					sdgDataMapping.getSdgData().getShortNameCode() + " " + sdgDataMapping.getSdgData().getShortName());
			sdgDataFrameworkPayload.setShortNameCode(sdgDataMapping.getSdgData().getShortNameCode());
			sdgDataFrameworkPayload.setType(SDG_TYPE);
			sdgDataFrameworkPayload.setTagName(sdgDataMapping.getSdgData().getShortName());
		}
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationSpiData(Organization organization, OrganizationElasticSearchPayload organizationPayload)
			throws Exception {
		// fetch all spiDataMapping of an organization
		List<OrganizationSpiData> organizationSpiDataMappingList = orgSpiDataMapRepository
				.getOrgSpiMapDataByOrgId(organization.getId());
		List<OrganizationFrameworksPayload> spiDataFrameworks = new ArrayList<>();

		if (null != organizationSpiDataMappingList) {
			for (OrganizationSpiData spiDataMapping : organizationSpiDataMappingList) {
				OrganizationFrameworksPayload spiDataFrameworkPayload = new OrganizationFrameworksPayload();
				// copy spiDataMapping values to spiDataFrameworkPayload
				BeanUtils.copyProperties(spiDataMapping, spiDataFrameworkPayload);
				// copy remaining spiDataMapping values to
				// spiDataMappingPayload
				setOrgSpiMapping(spiDataMapping, spiDataFrameworkPayload);
				// set adminUrl for organization SPI TAGS
				if (winwinRoutesMap == null) {
					// set winWin routes map
					setWinWinRoutesMap();
				}
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.SPI_TAGS)) {
					spiDataFrameworkPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.SPI_TAGS));
				}
				// set spiDataFrameworkPayload to spiDataFrameworks
				spiDataFrameworks.add(spiDataFrameworkPayload);
			}
		}
		// set spiDataFrameworks to organizationPayload
		if (organizationPayload.getFramework() == null)
			organizationPayload.setFramework(spiDataFrameworks);
		else
			organizationPayload.getFramework().addAll(spiDataFrameworks);
	}

	/**
	 * @param spiDataMapping
	 * @param spiDataFrameworkPayload
	 */
	private void setOrgSpiMapping(OrganizationSpiData spiDataMapping,
			OrganizationFrameworksPayload spiDataFrameworkPayload) {
		if (null != spiDataMapping.getSpiData()) {
			spiDataFrameworkPayload.setDimension(spiDataMapping.getSpiData().getDimensionName());
			spiDataFrameworkPayload.setComponent(spiDataMapping.getSpiData().getComponentName());
			spiDataFrameworkPayload.setIndicator(spiDataMapping.getSpiData().getIndicatorName());
			spiDataFrameworkPayload.setType(SPI_TYPE);
			spiDataFrameworkPayload.setTagName(spiDataMapping.getSpiData().getIndicatorName());
		}
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationRegionServed(Organization organization,
			OrganizationElasticSearchPayload organizationPayload) throws Exception {
		// fetch all regionServed of an organization
		List<OrganizationRegionServed> organizationRegionServedList = organizationRegionServedRepository
				.findAllActiveOrgRegions(organization.getId());
		List<OrganizationRegionServedElasticSearchPayload> regionServedPayloadList = new ArrayList<>();

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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.REGIONS_SERVED)) {
					regionServedPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.REGIONS_SERVED));
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
	private void setOrganizationResources(Organization organization,
			OrganizationElasticSearchPayload organizationPayload) throws Exception {
		// fetch all active resources of an organization
		List<OrganizationResource> organizationResourceList = organizationResourceRepository
				.findAllActiveOrgResources(organization.getId());
		List<OrganizationResourceElasticSearchPayload> resourcePayloadList = new ArrayList<>();
		List<String> namesofResources = new ArrayList<>();

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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.RESOURCES)) {
					resourcePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.RESOURCES));
				}
				// set resourcePayload to resourcePayloadList
				resourcePayloadList.add(resourcePayload);
				// set resource name to namesofResources
				namesofResources.add(resourcePayload.getName());
			}
		}
		// set resourcePayloadList to organizationPayload
		organizationPayload.setResources(resourcePayloadList);
		// set namesofResources to organizationPayload
		organizationPayload.setNamesOfResources(namesofResources);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationDataSets(Organization organization,
			OrganizationElasticSearchPayload organizationPayload) throws Exception {
		// fetch all active dataSets of an organization
		List<OrganizationDataSet> organizationDataSetList = organizationDataSetRepository
				.findAllActiveOrgDataSets(organization.getId());
		List<OrganizationDataSetElasticSearchPayload> dataSetPayloadList = new ArrayList<>();
		List<String> namesofDatasets = new ArrayList<>();

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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.DATASETS)) {
					dataSetPayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.DATASETS));
				}
				// set dataSetPayload to dataSetPayloadList
				dataSetPayloadList.add(dataSetPayload);
				// set DataSet Name to namesofDatasets
				namesofDatasets.add(dataSetPayload.getName());
			}
		}
		// set dataSetPayloadList to organizationPayload
		organizationPayload.setDatasets(dataSetPayloadList);
		// set namesofDatasets to organizationPayload
		organizationPayload.setNamesOfDatasets(namesofDatasets);
	}

	/**
	 * @param organizationFromMap
	 * @param organizationPayload
	 */
	private void setOrganizationNotes(Organization organization, OrganizationElasticSearchPayload organizationPayload)
			throws Exception {
		// fetch all notes of an organization
		List<OrganizationNote> organizationNoteList = organizationNoteRepository
				.findAllOrgNotesList(organization.getId());
		List<OrganizationNoteElasticSearchPayload> notePayloadList = new ArrayList<>();

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
				if (null != winwinRoutesMap && winwinRoutesMap.containsKey(OrganizationConstants.BASE_URL)
						&& winwinRoutesMap.containsKey(OrganizationConstants.ORGANIZATIONS)
						&& winwinRoutesMap.containsKey(OrganizationConstants.NOTES)) {
					notePayload.setAdminUrl(winwinRoutesMap.get(OrganizationConstants.BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.ORGANIZATIONS) + organization.getId()
							+ winwinRoutesMap.get(OrganizationConstants.NOTES));
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
	private void setOrganizationAddress(Organization organization,
			OrganizationElasticSearchPayload organizationPayload) {
		if (null != organization.getAddress()) {
			AddressElasticSearchPayload addressPayload = new AddressElasticSearchPayload();
			// copy organization address values to addressPayload
			BeanUtils.copyProperties(organization.getAddress(), addressPayload);
			// set addressPayload to organizationPayload
			organizationPayload.setAddress(addressPayload);
		}
	}

	// Adds the interceptor to the ES REST client
	public static RestHighLevelClient esClientForAWSHostedElasticSearch(String serviceName, String region) {
		AWS4Signer signer = new AWS4Signer();
		signer.setServiceName(serviceName);
		signer.setRegionName(region);
		HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(serviceName, signer,
				envCredentialsProvider);

		// Added .setMaxRetryTimeoutMillis(6000000) to avoid listener timeout
		// exception
		// use .setMaxRetryTimeoutMillis(6000000) when elasticSearch version <
		// 7.0
		// Added .setConnectTimeout(6000000).setSocketTimeout(6000000)) to avoid
		// socket and connection timeout exception
		return new RestHighLevelClient(RestClient.builder(HttpHost.create(ES_ENDPOINT)).setRequestConfigCallback(
				requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(6000000).setSocketTimeout(6000000))
				.setHttpClientConfigCallback(hacb -> hacb.addInterceptorLast(interceptor)));
	}

	// Adds the interceptor to the ES REST client
	public static RestHighLevelClient esClientForEC2HostedElasticSearch(String userName, String password) {
		String encodedBytes = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
		Integer port = new Integer(ES_ENDPOINT_PORT);
		String scheme = ES_ENDPOINT_SCHEME;

		Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),
				new BasicHeader("Authorization", "Basic " + encodedBytes) };

		// Added .setMaxRetryTimeoutMillis(600000000) to avoid listener timeout
		// exception
		// use .setMaxRetryTimeoutMillis(6000000) when elasticSearch version <
		// 7.0
		// Added .setConnectTimeout(600000000).setSocketTimeout(600000000)) to
		// avoid
		// socket and connection timeout exception
		// Added Failure Listener when node fails
		return new RestHighLevelClient(RestClient.builder(new HttpHost(ES_ENDPOINT, port, scheme))
				.setDefaultHeaders(headers).setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
						.setConnectTimeout(600000000).setSocketTimeout(600000000))
				.setFailureListener(new RestClient.FailureListener() {
					@Override
					public void onFailure(Node node) {
						if (null != node)
							LOGGER.error("Elastic Search Node: {} has been failed while sending index request",
									node.getName());
					}
				}));
	}

}
