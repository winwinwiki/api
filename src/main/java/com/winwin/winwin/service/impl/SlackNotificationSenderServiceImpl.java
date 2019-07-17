package com.winwin.winwin.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.SlackNotificationSenderService;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Service
public class SlackNotificationSenderServiceImpl implements SlackNotificationSenderService {

	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	AwsS3ObjectServiceImpl awsS3ObjectServiceImpl;

	private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationSenderServiceImpl.class);

	/**
	 * The below method sendSlackNotification accepts a list of failed and
	 * successfully created organizations in bulk upload operation and creates
	 * an attachment with all the organizations along with there status and send
	 * notification to the channel set in Environment Variables. This method
	 * requires environment variables as SLACK_CHANNEL_NAME,SLACK_AUTH_TOKEN and
	 * SLACK_UPLOAD_FILE_API_URL which will be created through Slack for the
	 * particular channel
	 */
	@Override
	public void sendSlackNotification(List<Organization> organizations, UserPayload user, Date date) {
		try {
			// list of failed and success organizations
			StringBuilder successOrganizations = new StringBuilder();
			successOrganizations.append("# Below are the List of Successfully uploaded organizations").append("\n")
					.append("Organization Id").append(",").append("Organization Name").append(",")
					.append("Organization Status").append("\n");

			StringBuilder failedOrganizations = new StringBuilder();
			failedOrganizations.append("# Below are the List of organizations that are failed to upload").append("\n")
					.append("Organization Name").append(",").append("Organization Status").append("\n");

			for (Organization organization : organizations) {
				if (null != organization.getId()) {
					successOrganizations.append(organization.getId().toString());
					successOrganizations.append(",");
					successOrganizations.append(organization.getName());
					successOrganizations.append(",");
					successOrganizations.append("SUCCESS");
					successOrganizations.append("\n");
				} else {
					failedOrganizations.append(organization.getName());
					failedOrganizations.append(",");
					failedOrganizations.append("FAILED");
					failedOrganizations.append("\n");
				}
			} // end of for loop

			successOrganizations.append("\n").append("\n").append("\n");

			// write list of success and failed organizations into .csv
			File file = new File("organization_bulk_upload_result.csv");
			// Create the file
			LOGGER.info("creating Bulk Upload File " + file.getName());
			if (file.createNewFile()) {
				LOGGER.info("Bulk Upload file " + file.getName() + " is successfully created!");
			} else {
				LOGGER.info("deleting existing Bulk Upload file: " + file.getName());
				file.delete();
				LOGGER.info("Bulk Upload file  " + file.getName() + " has been successfully deleted");

				LOGGER.info("creating again Bulk Upload File " + file.getName());
				file.createNewFile();
				LOGGER.info("Bulk Upload file " + file.getName() + " is successfully created again!");
			}

			FileWriter csvWriter = new FileWriter(file, true);
			csvWriter.append(successOrganizations);
			csvWriter.append(failedOrganizations);
			csvWriter.flush();
			csvWriter.close();

			String fileContent = FileUtils.readFileToString(file, "UTF-8");
			SlackMessage slackMessage = SlackMessage.builder().filetype("csv").filename(file.getName())
					.username("WinWinUploadNotifier").content(fileContent)
					.initial_comment("WinWinWiki editor bulk upload status file, created by: "
							+ user.getUserDisplayName() + " at " + date)
					.channels(System.getenv("SLACK_CHANNEL_NAME")).build();

			sendPostRequest(slackMessage);

		} catch (Exception e) {
			LOGGER.error("exception occured while sending notifications", e);
		}

	}

	/**
	 * Sends File Notifications to Slack on defined channel
	 * 
	 * @param message
	 */
	private void sendPostRequest(SlackMessage message) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("filetype", message.getFiletype()));
		params.add(new BasicNameValuePair("filename", message.getFilename()));
		params.add(new BasicNameValuePair("username", message.getUsername()));
		params.add(new BasicNameValuePair("content", message.getContent()));
		params.add(new BasicNameValuePair("initial_comment", message.getInitial_comment()));
		params.add(new BasicNameValuePair("channels", message.getChannels()));

		try {
			URL obj = new URL(System.getenv("SLACK_UPLOAD_FILE_API_URL"));
			HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
			postConnection.setRequestProperty("Authorization", "Bearer " + System.getenv("SLACK_AUTH_TOKEN"));
			postConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			postConnection.setReadTimeout(10000);
			postConnection.setConnectTimeout(15000);
			postConnection.setRequestMethod("POST");
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

		} catch (MalformedURLException e) {
			LOGGER.error("exception occured while sending notifications", e);
		} catch (ProtocolException e) {
			LOGGER.error("exception occured while sending notifications", e);
		} catch (IOException e) {
			LOGGER.error("exception occured while sending notifications", e);
		}
	}

	private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
		StringBuilder result = new StringBuilder();
		boolean first = true;

		for (NameValuePair pair : params) {
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
		}

		return result.toString();
	}

	@SuppressWarnings("unused")
	private void sendMessage(SlackMessage message) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(System.getenv("SLACK_UPLOAD_FILE_API_URL"));

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(message);
			StringEntity reqEntity1 = new StringEntity(json);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("filetype", message.getFiletype());
			builder.addTextBody("filename", message.getFilename());
			builder.addTextBody("username", message.getUsername());
			builder.addTextBody("content", message.getContent());
			builder.addTextBody("initial_comment", message.getInitial_comment());
			builder.addTextBody("channels", message.getChannels());

			HttpEntity reqEntity = builder.build();

			httpPost.setEntity(reqEntity);
			httpPost.setHeader("Authorization", "Bearer " + System.getenv("SLACK_AUTH_TOKEN"));
			httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");

			CloseableHttpResponse response = client.execute(httpPost);
			client.close();
		} catch (IOException e) {
			LOGGER.error("exception occured while sending notifications", e);
		}
	}

}
