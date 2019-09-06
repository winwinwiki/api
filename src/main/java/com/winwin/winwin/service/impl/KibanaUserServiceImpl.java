package com.winwin.winwin.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.winwin.winwin.constants.OrganizationConstants;
import com.winwin.winwin.entity.WinWinRoutesMapping;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.KibanaUserPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;
import com.winwin.winwin.repository.WinWinRoutesMappingRepository;
import com.winwin.winwin.service.KibanaUserService;

import io.micrometer.core.instrument.util.StringUtils;

@Service
public class KibanaUserServiceImpl implements KibanaUserService {

	@Autowired
	private WinWinRoutesMappingRepository winWinRoutesMappingRepository;

	private static final Logger LOGGER = LoggerFactory.getLogger(KibanaUserServiceImpl.class);

	private Map<String, String> winwinRoutesMap = null;

	@Override
	public void createInternalKibanaUser(UserSignInPayload payload, ExceptionResponse response) throws Exception {

		sendPostRequestToKibana(payload);

	}

	@Override
	public UserPayload getUserInfo(String userName, ExceptionResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateUserInfo(UserPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<UserPayload> getUserList(ExceptionResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUserStatus(String userName, ExceptionResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetUserPassword(UserPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resendConfirmationCode(UserPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void confirmResetPassword(UserSignInPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void changePassword(UserSignInPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public AuthenticationResultType userSignIn(UserSignInPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteUser(UserPayload payload, ExceptionResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserPayload getLoggedInUser(String accessToken, ExceptionResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserPayload getCurrentUserDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resendUserInvitation(UserPayload payload, ExceptionResponse response) throws UserException {
		// TODO Auto-generated method stub

	}

	/**
	 * Send Post Request to KIBANA
	 * 
	 * @param message
	 */

	private void sendPostRequestToKibana(UserSignInPayload payload) throws Exception {
		// set post request for KIBANA
		if (winwinRoutesMap == null) {
			// set winWin routes map
			setWinWinRoutesMap();
		}

		if (!winwinRoutesMap.isEmpty() && winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_USER_API_URL)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLES)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_NAME)
				&& winwinRoutesMap.containsKey(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)) {

			KibanaUserPayload kibanaUserPayload = new KibanaUserPayload();
			// kibanaUserPayload.setUserName(payload.getUserName());
			kibanaUserPayload.setPassword(payload.getNewPassword());
			kibanaUserPayload.setBackend_roles(winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_BACKEND_ROLES));

			CloseableHttpClient client = HttpClients.createDefault();
			String url = winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName();

			HttpPut httpPut = new HttpPut(url);
			// Creating Object of ObjectMapper define in JACKSON API
			ObjectMapper objectMapper = new ObjectMapper();
			// get KibanaUserPayload object as a JSON string
			String jsonStr = objectMapper.writeValueAsString(kibanaUserPayload);

			String encoding = Base64.getEncoder()
					.encodeToString((winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_NAME) + ":"
							+ winwinRoutesMap.get(OrganizationConstants.KIBANA_ADMIN_USER_PASSWORD)).getBytes());

			StringEntity entity = new StringEntity(jsonStr);
			httpPut.setEntity(entity);
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Content-type", "application/json");
			httpPut.setHeader("Authorization", "Basic " + encoding);

			client.execute(httpPut);

			HttpResponse response = client.execute(httpPut);
			HttpEntity httpEntity = response.getEntity();
			if (entity != null) {
				try (InputStream instream = httpEntity.getContent()) {
					System.out.println(instream);
				}
			}

			client.close();

			List<NameValuePair> params = new ArrayList<NameValuePair>();

			if (!StringUtils.isEmpty(kibanaUserPayload.getPassword()))
				params.add(new BasicNameValuePair("password", kibanaUserPayload.getPassword()));

			if (!StringUtils.isEmpty(kibanaUserPayload.getBackend_roles()))
				params.add(new BasicNameValuePair("backend_roles", kibanaUserPayload.getBackend_roles()));

			URL obj = new URL(winwinRoutesMap.get(OrganizationConstants.KIBANA_BACKEND_BASE_URL)
					+ winwinRoutesMap.get(OrganizationConstants.KIBANA_USER_API_URL) + payload.getUserName());

			HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
			postConnection.setRequestProperty("Authorization", "Basic " + encoding);
			postConnection.setRequestProperty("Content-Type", "application/json");
			postConnection.setReadTimeout(10000);
			postConnection.setConnectTimeout(15000);
			postConnection.setRequestMethod("PUT");
			postConnection.setDoInput(true);
			postConnection.setDoOutput(true);

			OutputStream os = postConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.write(getQuery(params));
			writer.flush();
			writer.close();
			os.close();
			postConnection.connect();

			// read the InputStream and print it
			String result;
			BufferedInputStream bis = new BufferedInputStream(postConnection.getInputStream());
			ByteArrayOutputStream buf = new ByteArrayOutputStream();
			int result2 = bis.read();
			while (result2 != -1) {
				buf.write((byte) result2);
				result2 = bis.read();
			}
			result = buf.toString();
			LOGGER.info(result);
		}

	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append(",");

			result.append("\"" + pair.getName() + "\"");
			result.append(":");
			result.append("\"" + pair.getValue() + "\"");
		}

		return result.toString();
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
