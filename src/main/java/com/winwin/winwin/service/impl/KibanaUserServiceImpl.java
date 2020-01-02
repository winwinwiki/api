package com.winwin.winwin.service.impl;

import java.util.ArrayList;
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
		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {

			CloseableHttpClient client = HttpClients.createDefault();
			String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + userName;

			String encoding = Base64.getEncoder()
					.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
							+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());

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

			// Parse JSON and FETCH user's backend_roles
			final JSONObject jsonObj = new JSONObject(responseJSON);
			final JSONArray backendRoles = jsonObj.getJSONObject(userName).getJSONArray("roles");
			String username = jsonObj.getJSONObject(userName).getString("username");
			String email = jsonObj.getJSONObject(userName).getString("email");
			String fullName = jsonObj.getJSONObject(userName).getString("full_name");
			Boolean enabled = jsonObj.getJSONObject(userName).getBoolean("enabled");

			ArrayList<String> roleslist = new ArrayList<String>();
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

			// close the CloseableHttpClient
			client.close();

		}

		return resPayload;

	}

	/**
	 * get WINWIN COGNITO user role, mapped to elastic search internal user role
	 * 
	 * @param userName
	 * @return
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

	private void sendPostRequestToKibana(UserSignInPayload payload) throws Exception {
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

			// check for ADMIN user
			if (payload.getRole().equals(UserConstants.ROLE_ADMIN)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE)) {
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLE) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

				// check for DATASEEDER user
			} else if (payload.getRole().equals(UserConstants.ROLE_DATASEEDER)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE)) {
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_DATASEEDER_BACKEND_ROLE) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

				// check for READER user
			} else if (payload.getRole().equals(UserConstants.ROLE_READER)
					&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_READER_BACKEND_ROLE)) {
				String backendRoles[] = { winwinRoutesMap.get(OrganizationConstants.KIBANA_READER_BACKEND_ROLE) };

				// set KibanaUserPayload
				setKibanaUserPayload(payload, kibanaUserReqPayload, backendRoles);

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
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());
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
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {

			if (!StringUtils.isEmpty(payload.getUserName())) {
				CloseableHttpClient client = HttpClients.createDefault();
				String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
						+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();
				String encoding = Base64.getEncoder()
						.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
								+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)).getBytes());
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
	 * Send Password Change Request to elastic search for internal user
	 * 
	 * @param payload
	 */

	private void sendPasswordChangeRequestToKibana(UserSignInPayload payload) throws Exception {
		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_CHANGE_PASS_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASS_WORD)) {

			if (!StringUtils.isEmpty(payload.getUserName())) {
				CloseableHttpClient client = HttpClients.createDefault();
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
				httpPut.setHeader("Accept", "application/json");
				httpPut.setHeader("Content-type", "application/json");
				httpPut.setHeader("Authorization", "Basic " + encoding);
				LOGGER.info(" Sending Internal User Change Password Request to elastic search for user: "
						+ payload.getUserName());
				HttpResponse response = client.execute(httpPut);
				String responseJSON = EntityUtils.toString(response.getEntity(), "UTF-8");
				LOGGER.info(
						" Internal User Change Password Request has been successfully sent to elastic search for user: "
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
