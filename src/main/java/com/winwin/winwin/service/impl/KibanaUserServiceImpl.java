package com.winwin.winwin.service.impl;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.entity.WinWinRoutesMapping;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.payload.KibanaUserPayload;
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

	/**
	 * Create internal user in elastic search
	 * 
	 * @param payload
	 * @param response
	 */
	@Override
	public void createInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception {
		// send post request to elastic search for internal user creation
		sendPostRequestToKibana(payload);

	}

	@Override
	public void deleteInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception {
		// send delete request to elastic search for internal user deletion
		sendDeleteRequestToKibana(payload);

	}

	/**
	 * get internal user role from elastic search
	 * 
	 * @param userName
	 */
	@Override
	public String getInternalKibanaUserRole(String userName) throws Exception {
		Boolean isAdmin = false;
		Boolean isDataSeeder = false;
		Boolean isReader = false;
		String winwinUserRole = "";

		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)) {

			CloseableHttpClient client = HttpClients.createDefault();
			String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + userName;

			String encoding = Base64.getEncoder()
					.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
							+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)).getBytes());

			// Prepare HttpGet Request
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Content-type", "application/json");
			httpGet.setHeader("Authorization", "Basic " + encoding);

			LOGGER.info(" Sending Internal User Request to elastic search to get user details of : " + userName);
			HttpResponse response = client.execute(httpGet);
			String responseJSON = EntityUtils.toString(response.getEntity(), "UTF-8");
			LOGGER.info(" Internal User Request has been successfully sent to elastic search to get user details of : "
					+ userName);
			LOGGER.info(" Response: " + responseJSON);

			// Parse JSON and FETCH user's search_guard_roles and backend_roles
			final JSONObject jsonObj = new JSONObject(responseJSON);
			final JSONArray searchGuardRoles = jsonObj.getJSONObject(userName).getJSONArray("search_guard_roles");
			final JSONArray backendRoles = jsonObj.getJSONObject(userName).getJSONArray("backend_roles");

			// Convert JSON Array to List
			for (int i = 0; i < searchGuardRoles.length(); i++) {
				if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_SEARCH_GUARD_ROLE))) {
					isAdmin = true;
				} else if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_SEARCH_GUARD_ROLE))) {
					isDataSeeder = true;
				} else if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_SEARCH_GUARD_ROLE))) {
					isReader = true;
				}
			}

			// Convert JSON Array to List
			for (int i = 0; i < backendRoles.length(); i++) {
				if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE))) {
					isAdmin = true;
				} else if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE))) {
					isDataSeeder = true;
				} else if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE1))) {
					isReader = true;
				} else if (searchGuardRoles.getString(i)
						.equals(winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE2))) {
					isReader = true;
				}
			}

			// close the CloseableHttpClient
			client.close();

		}

		// return WINWIN user role mapped to search guard roles
		if (isAdmin)
			winwinUserRole = UserConstants.ROLE_ADMIN;
		else if (isDataSeeder)
			winwinUserRole = UserConstants.ROLE_DATASEEDER;
		else if (isReader)
			winwinUserRole = UserConstants.ROLE_READER;

		return winwinUserRole;

	}

	/**
	 * Send Post Request to elastic search for internal user creation
	 * 
	 * @param payload
	 */

	private void sendPostRequestToKibana(UserSignInPayload payload) throws Exception {
		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)) {

			KibanaUserPayload kibanaUserPayload = new KibanaUserPayload();

			// check for ADMIN user
			if (payload.getRole().equals(UserConstants.ROLE_ADMIN)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_SEARCH_GUARD_ROLE)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE)) {
				String searchGuardRoles[] = {
						winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_SEARCH_GUARD_ROLE) };
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserPayload, searchGuardRoles, backendRoles);

				// check for DATASEEDER user
			} else if (payload.getRole().equals(UserConstants.ROLE_DATASEEDER)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_DATASEEDER_SEARCH_GUARD_ROLE)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE)) {
				String searchGuardRoles[] = {
						winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_SEARCH_GUARD_ROLE) };
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserPayload, searchGuardRoles, backendRoles);

				// check for READER user
			} else if (payload.getRole().equals(UserConstants.ROLE_READER)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_READER_SEARCH_GUARD_ROLE)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_READER_BACKEND_ROLE1)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_READER_BACKEND_ROLE2)) {
				String searchGuardRoles[] = {
						winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_SEARCH_GUARD_ROLE) };
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE1),
						winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE2) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserPayload, searchGuardRoles, backendRoles);

			}

			if (!StringUtils.isEmpty(payload.getUserName())) {
				CloseableHttpClient client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();
				// Creating Object of ObjectMapper define in JACKSON API
				ObjectMapper objectMapper = new ObjectMapper();
				// get KibanaUserPayload object as a JSON string
				String jsonStr = null;
				// created KibanaUserRolePayload to only update the roles in
				// Elastic
				// Search Internal User DB
				if (!StringUtils.isEmpty(kibanaUserPayload.getPassword())) {
					// get KibanaUserPayload object as a JSON string
					jsonStr = objectMapper.writeValueAsString(kibanaUserPayload);
				} else {
					KibanaUserRolePayload kibanaUserRolePayload = new KibanaUserRolePayload();
					kibanaUserRolePayload.setBackend_roles(kibanaUserPayload.getBackend_roles());
					kibanaUserRolePayload.setSearch_guard_roles(kibanaUserPayload.getSearch_guard_roles());
					// get KibanaUserPayload object as a JSON string
					jsonStr = objectMapper.writeValueAsString(kibanaUserRolePayload);
				}
				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)).getBytes());
				StringEntity entity = new StringEntity(jsonStr);
				HttpPut httpPut = new HttpPut(url);
				httpPut.setEntity(entity);
				httpPut.setHeader("Accept", "application/json");
				httpPut.setHeader("Content-type", "application/json");
				httpPut.setHeader("Authorization", "Basic " + encoding);
				LOGGER.info(
						" Sending Internal User Creation Request to elastic search for user: " + payload.getUserName());
				HttpResponse response = client.execute(httpPut);
				String responseJSON = EntityUtils.toString(response.getEntity(), "UTF-8");
				LOGGER.info(" Internal User Creation Request has been successfully sent to elastic search for user: "
						+ payload.getUserName());
				LOGGER.info(" Response: " + responseJSON);
				// close the CloseableHttpClient
				client.close();
			}

		} // end of if (!winwinRoutesMap.isEmpty()

	}

	/**
	 * Send Delete Request to elastic search for internal user deletion
	 * 
	 * @param payload
	 */

	private void sendDeleteRequestToKibana(UserSignInPayload payload) throws Exception {
		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)) {

			if (!StringUtils.isEmpty(payload.getUserName())) {
				CloseableHttpClient client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();
				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)).getBytes());
				HttpDelete httpDelete = new HttpDelete(url);
				httpDelete.setHeader("Accept", "application/json");
				httpDelete.setHeader("Content-type", "application/json");
				httpDelete.setHeader("Authorization", "Basic " + encoding);
				LOGGER.info(
						" Sending Internal User Deletion Request to elastic search for user: " + payload.getUserName());
				HttpResponse response = client.execute(httpDelete);
				String responseJSON = EntityUtils.toString(response.getEntity(), "UTF-8");
				LOGGER.info(" Internal User Deletion Request has been successfully sent to elastic search for user: "
						+ payload.getUserName());
				LOGGER.info(" Response: " + responseJSON);
				// close the CloseableHttpClient
				client.close();
			}

		} // end of if (!winwinRoutesMap.isEmpty()

	}

	/**
	 * @param payload
	 * @param kibanaUserPayload
	 * @param searchGuardRoles
	 * @param backendRoles
	 */
	private void setKibanaUserPayload(UserSignInPayload payload, KibanaUserPayload kibanaUserPayload,
			String[] searchGuardRoles, String[] backendRoles) {
		if (null != payload)
			kibanaUserPayload.setPassword(payload.getNewPassword());

		kibanaUserPayload.setSearch_guard_roles(searchGuardRoles);
		kibanaUserPayload.setBackend_roles(backendRoles);
	}

	/**
	 * set FrontEnd Routes for WINWIN
	 */
	private void setWinWinRoutesMap() throws Exception {
		winwinRoutesMap = new HashMap<String, String>();
		List<WinWinRoutesMapping> activeRoutes = winWinRoutesMappingRepository.findAllActiveRoutes();
		if (null != activeRoutes)
			winwinRoutesMap = activeRoutes.stream()
					.collect(Collectors.toMap(WinWinRoutesMapping::getKey, WinWinRoutesMapping::getValue));
	}

}
