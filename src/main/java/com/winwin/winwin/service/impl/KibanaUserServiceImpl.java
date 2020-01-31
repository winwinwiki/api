package com.winwin.winwin.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.WinWinRoutesMapping;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.KibanaChangePasswordPayload;
import com.winwin.winwin.payload.KibanaUserRequestPayload;
import com.winwin.winwin.payload.KibanaUserResponsePayload;
import com.winwin.winwin.payload.KibanaUserRolePayload;
import com.winwin.winwin.payload.UserSignInPayload;
import com.winwin.winwin.repository.WinWinRoutesMappingRepository;
import com.winwin.winwin.service.KibanaUserService;

import io.micrometer.core.instrument.util.StringUtils;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class KibanaUserServiceImpl implements KibanaUserService {

	@Autowired
	private WinWinRoutesMappingRepository winWinRoutesMappingRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(KibanaUserServiceImpl.class);
	private Map<String, String> winwinRoutesMap = null;
	private static final String ES_ENDPOINT = System.getenv("AWS_ES_ENDPOINT");
	private static final String ES_ENDPOINT_PORT = System.getenv("AWS_ES_ENDPOINT_PORT");
	private static final String ES_ENDPOINT_SCHEME = System.getenv("AWS_ES_ENDPOINT_SCHEME");
	private static final String SECURITY_INDEX = System.getenv("AWS_ES_SECURITY_INDEX");
	private static final String RESPONSE_MSZ = " Response: {}";
	private static final String BASIC_AUTH = "Basic ";

	/**
	 * Create internal user in elastic search internal DB
	 * 
	 * @param payload
	 * @param response
	 */
	@Override
	public void createInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception {
		// send post request to elastic search for internal user creation
		sendPostRequestToKibana(payload);

	}

	/**
	 * delete internal user from elastic search internal DB
	 * 
	 * @param payload
	 * @param response
	 */
	@Override
	public void deleteInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception {
		// send delete request to elastic search for internal user deletion
		sendDeleteRequestToKibana(payload);

	}

	/**
	 * Change internal user password in elastic search internal DB
	 * 
	 * @param payload
	 * @param response
	 */
	@Override
	public void changePasswordForInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response)
			throws Exception {
		// send password change request to elastic search for internal user
		sendPasswordChangeRequestToKibana(payload);

	}

	/**
	 * get internal user details from elastic search
	 * 
	 * @param userName
	 * @return
	 */
	@Override
	public KibanaUserResponsePayload getInternalKibanaUserDetails(String userName) throws Exception {
		KibanaUserResponsePayload resPayload = new KibanaUserResponsePayload();
		CloseableHttpClient client = null;
		try {
			// set post request for KIBANA
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {

				client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + userName;

				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());

				// Prepare HttpGet Request
				HttpGet httpGet = new HttpGet(url);
				httpGet.setHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpGet.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpGet.setHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encoding);

				LOGGER.info(" Sending Internal User Request to elastic search to get user details of : {}", userName);
				// update Index Settings before performing User API Operation
				if (!StringUtils.isEmpty(SECURITY_INDEX))
					updateIndexSettings(SECURITY_INDEX);

				HttpResponse response = client.execute(httpGet);
				String responseJSON = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.toString());
				LOGGER.info(
						" Internal User Request has been successfully sent to elastic search to get user details of : {}",
						userName);
				LOGGER.info(RESPONSE_MSZ, responseJSON);

				// set KibanaUserResponsePayload
				setResponsePayload(userName, resPayload, responseJSON);
			}
		} finally {
			// close the CloseableHttpClient
			if (null != client)
				client.close();
		}
		return resPayload;

	}

	/**
	 * @param userName
	 * @param resPayload
	 * @param responseJSON
	 * @throws Exception
	 */
	private void setResponsePayload(String userName, KibanaUserResponsePayload resPayload, String responseJSON) {
		try {
			// Parse JSON and FETCH user's backend_roles
			final JSONObject jsonObj = new JSONObject(responseJSON);
			final JSONArray backendRoles = jsonObj.getJSONObject(userName).getJSONArray("roles");
			String username = jsonObj.getJSONObject(userName).getString("username");
			String email = jsonObj.getJSONObject(userName).getString("email");
			String fullName = jsonObj.getJSONObject(userName).getString("full_name");
			Boolean enabled = jsonObj.getJSONObject(userName).getBoolean("enabled");
			ArrayList<String> roleslist = new ArrayList<>();
			for (int i = 0; i < backendRoles.length(); i++) {
				roleslist.add(backendRoles.getString(i));
			}
			// Convert JSON Array to List
			String[] roles = roleslist.toArray(new String[0]);
			resPayload.setUsername(username);
			resPayload.setRoles(roles);
			resPayload.setFull_name(fullName);
			resPayload.setEmail(email);
			resPayload.setEnabled(enabled);
		} catch (Exception e) {
			LOGGER.error("exception occured while setting kibana response payload", e);
		}
	}

	/**
	 * get WINWIN COGNITO user role, mapped to elastic search internal user role
	 * 
	 * @param userName
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getKibanaUserCognitoRole(String userName) throws Exception {
		Boolean isAdmin = false;
		Boolean isDataSeeder = false;
		Boolean isReader = false;
		String winwinUserRole = "";

		KibanaUserResponsePayload kibanaUserResPayload = getInternalKibanaUserDetails(userName);

		if (null != kibanaUserResPayload) {
			String[] roles = kibanaUserResPayload.getRoles();

			for (int i = 0; i < roles.length; i++) {
				if (roles[i].equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE))) {
					isAdmin = true;
				} else if (roles[i].equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE))) {
					isDataSeeder = true;
				} else if (roles[i].equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE))) {
					isReader = true;
				}
			}
		}
		// return WINWIN user role mapped to search guard roles
		if (Boolean.TRUE.equals(isAdmin))
			winwinUserRole = UserConstants.ROLE_ADMIN;
		else if (Boolean.TRUE.equals(isDataSeeder))
			winwinUserRole = UserConstants.ROLE_DATASEEDER;
		else if (Boolean.TRUE.equals(isReader))
			winwinUserRole = UserConstants.ROLE_READER;

		return winwinUserRole;

	}

	/**
	 * Send Post Request to elastic search for internal user creation
	 * 
	 * @param payload
	 */

	private void sendPostRequestToKibana(UserSignInPayload payload) throws IOException {
		CloseableHttpClient client = null;
		try {
			// set post request for KIBANA
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {

				KibanaUserRequestPayload kibanaUserReqPayload = new KibanaUserRequestPayload();
				// set KibanaUserpayload by roles
				setKibanauserPayloadByRoles(payload, kibanaUserReqPayload);

				if (!StringUtils.isEmpty(payload.getUserName())) {
					client = HttpClients.createDefault();
					String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
							+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();
					// Creating Object of ObjectMapper define in JACKSON API
					ObjectMapper objectMapper = new ObjectMapper();
					// get KibanaUserPayload object as a JSON string
					String jsonStr = null;
					// created KibanaUserRolePayload to only update the roles in
					// Elastic
					// Search Internal User DB
					if (!StringUtils.isEmpty(kibanaUserReqPayload.getPassword())) {
						// get KibanaUserPayload object as a JSON string
						jsonStr = objectMapper.writeValueAsString(kibanaUserReqPayload);
					} else {
						KibanaUserRolePayload kibanaUserRolePayload = new KibanaUserRolePayload();
						kibanaUserRolePayload.setFull_name(payload.getFullName());
						kibanaUserRolePayload.setRoles(kibanaUserReqPayload.getRoles());
						kibanaUserRolePayload.setEmail(payload.getUserName());
						// get KibanaUserPayload object as a JSON string
						jsonStr = objectMapper.writeValueAsString(kibanaUserRolePayload);
					}
					String encoding = Base64.getEncoder()
							.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
									+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD))
											.getBytes());
					StringEntity entity = new StringEntity(jsonStr);
					HttpPut httpPut = new HttpPut(url);
					httpPut.setEntity(entity);
					httpPut.setHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
					httpPut.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
					httpPut.setHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encoding);
					LOGGER.info(" Sending Internal User Creation Request to elastic search for user: {}",
							payload.getUserName());

					// update Index Settings before performing User API
					// Operation
					if (!StringUtils.isEmpty(SECURITY_INDEX))
						updateIndexSettings(SECURITY_INDEX);

					HttpResponse response = client.execute(httpPut);
					String responseJSON = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.toString());
					LOGGER.info(
							" Internal User Creation Request has been successfully sent to elastic search for user: {}",
							payload.getUserName());
					LOGGER.info(RESPONSE_MSZ, responseJSON);
				}
			}
		} finally {
			// close the CloseableHttpClient
			if (null != client)
				client.close();
		}

	}

	/**
	 * @param payload
	 * @param kibanaUserReqPayload
	 */
	private void setKibanauserPayloadByRoles(UserSignInPayload payload, KibanaUserRequestPayload kibanaUserReqPayload) {
		// check for ADMIN user
		if (payload.getRole().equals(UserConstants.ROLE_ADMIN)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE)) {
			String[] backendRoles = { winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE) };

			// set KibanaUserPayload
			setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

			// check for DATASEEDER user
		} else if (payload.getRole().equals(UserConstants.ROLE_DATASEEDER)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE)) {
			String[] backendRoles = { winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE) };

			// set KibanaUserPayload
			setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

			// check for READER user
		} else if (payload.getRole().equals(UserConstants.ROLE_READER)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_READER_BACKEND_ROLE)) {
			String[] backendRoles = { winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE) };

			// set KibanaUserPayload
			setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

		}
	}

	/**
	 * Send Delete Request to elastic search for internal user deletion
	 * 
	 * @param payload
	 * @throws IOException
	 */

	private void sendDeleteRequestToKibana(UserSignInPayload payload) throws IOException {
		CloseableHttpClient client = null;
		try {
			// set post request for KIBANA
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)
					&& (!StringUtils.isEmpty(payload.getUserName()))) {

				client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();
				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());
				HttpDelete httpDelete = new HttpDelete(url);
				httpDelete.setHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpDelete.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpDelete.setHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encoding);
				LOGGER.info(" Sending Internal User Deletion Request to elastic search for user: {}",
						payload.getUserName());
				// update Index Settings before performing User API Operation
				if (!StringUtils.isEmpty(SECURITY_INDEX))
					updateIndexSettings(SECURITY_INDEX);

				HttpResponse response = client.execute(httpDelete);
				String responseJSON = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.toString());
				LOGGER.info(" Internal User Deletion Request has been successfully sent to elastic search for user: {}",
						payload.getUserName());
				LOGGER.info(RESPONSE_MSZ, responseJSON);

			}
		} finally {
			// close the CloseableHttpClient
			if (null != client)
				client.close();
		}

	}

	/**
	 * Send Password Change Request to elastic search for internal user
	 * 
	 * @param payload
	 * @throws IOException
	 * @throws ParseException
	 */

	private void sendPasswordChangeRequestToKibana(UserSignInPayload payload) throws IOException {
		CloseableHttpClient client = null;
		try {
			// set post request for KIBANA
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_CHANGE_PASS_API_URL)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)
					&& (!StringUtils.isEmpty(payload.getUserName()))) {

				client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName()
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_CHANGE_PASS_API_URL);
				// Creating Object of ObjectMapper define in JACKSON API
				ObjectMapper objectMapper = new ObjectMapper();
				// get KibanaUserPayload object as a JSON string
				String jsonStr = null;
				// created KibanaChangePasswordPayload to only update the
				// password in ElasticSearch Internal User DB
				if (!StringUtils.isEmpty(payload.getNewPassword())) {
					KibanaChangePasswordPayload kibanaChangePassPayload = new KibanaChangePasswordPayload();
					kibanaChangePassPayload.setPassword(payload.getNewPassword());
					// get kibanaChangePassPayload object as a JSON string
					jsonStr = objectMapper.writeValueAsString(kibanaChangePassPayload);
				}

				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());
				StringEntity entity = new StringEntity(jsonStr);
				HttpPut httpPut = new HttpPut(url);
				httpPut.setEntity(entity);
				httpPut.setHeader(HttpHeaders.ACCEPT, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpPut.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE);
				httpPut.setHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encoding);
				LOGGER.info(" Sending Internal User Change Password Request to elastic search for user: {}",
						payload.getUserName());

				// update Index Settings before performing User API Operation
				if (!StringUtils.isEmpty(SECURITY_INDEX))
					updateIndexSettings(SECURITY_INDEX);

				HttpResponse response = client.execute(httpPut);
				String responseJSON = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8.toString());
				LOGGER.info(
						" Internal User Change Password Request has been successfully sent to elastic search for user: {}",
						payload.getUserName());
				LOGGER.info(RESPONSE_MSZ, responseJSON);
			}
		} finally {
			// close the CloseableHttpClient
			if (null != client)
				client.close();
		}

	}

	private void updateIndexSettings(String index) throws IOException {
		RestHighLevelClient esClient = null;
		try {
			UpdateSettingsRequest request = new UpdateSettingsRequest(index);
			String settingKey1 = "index.blocks.read_only_allow_delete";
			Settings settings = Settings.builder().put(settingKey1, false).build();

			// set settings to request
			request.settings(settings);

			// set post request for ElasticSearch
			if (winwinRoutesMap == null) {
				// set winWin routes map
				setWinWinRoutesMap();
			}
			if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {
				// get rest client connection
				esClient = esClientForEC2HostedElasticSearch(
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD));
				AcknowledgedResponse updateSettingsResponse = esClient.indices().putSettings(request,
						RequestOptions.DEFAULT);

				// check for index settings updated successfully
				if (Boolean.TRUE.equals(updateSettingsResponse.isAcknowledged()))
					LOGGER.info("Index Settings updated for Index: {}", index);
				else
					LOGGER.info("Index Settings not updated for Index: {}", index);
			}

		} catch (IOException e) {
			LOGGER.error("Failed to update index settings for index: {}", index, e);
		} finally {
			// close the CloseableHttpClient
			if (null != esClient)
				esClient.close();
		}
	}

	// Adds the interceptor to the ES REST client
	private static RestHighLevelClient esClientForEC2HostedElasticSearch(String userName, String password) {
		String encodedBytes = Base64.getEncoder().encodeToString((userName + ":" + password).getBytes());
		Integer port = new Integer(ES_ENDPOINT_PORT);
		String scheme = ES_ENDPOINT_SCHEME;

		Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE),
				new BasicHeader(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encodedBytes) };

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

	/**
	 * @param payload
	 * @param kibanaUserPayload
	 * @param backendRoles
	 */
	private void setKibanaUserPayload(UserSignInPayload payload, KibanaUserRequestPayload kibanaUserPayload,
			String[] backendRoles) {
		if (null != payload) {
			kibanaUserPayload.setPassword(payload.getNewPassword());
			kibanaUserPayload.setFull_name(payload.getFullName());
			kibanaUserPayload.setEmail(payload.getUserName());
		}
		kibanaUserPayload.setRoles(backendRoles);

	}

	/**
	 * set FrontEnd Routes for WINWIN
	 */
	private void setWinWinRoutesMap() {
		winwinRoutesMap = new HashMap<>();
		List<WinWinRoutesMapping> activeRoutes = winWinRoutesMappingRepository.findAllActiveRoutes();
		if (null != activeRoutes)
			winwinRoutesMap = activeRoutes.stream()
					.collect(Collectors.toMap(WinWinRoutesMapping::getKey, WinWinRoutesMapping::getValue));
	}

}
