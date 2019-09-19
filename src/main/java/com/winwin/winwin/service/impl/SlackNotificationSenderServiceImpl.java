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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.payload.OrganizationBulkFailedPayload;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.SlackNotificationSenderService;

import io.micrometer.core.instrument.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@Service
@Getter
@Setter
public class SlackNotificationSenderServiceImpl implements SlackNotificationSenderService {

	@Autowired
	protected CustomMessageSource customMessageSource;
	@Autowired
	private AwsS3ObjectServiceImpl awsS3ObjectServiceImpl;

	@Value("${slack.channel}")
	String SLACK_CHANNEL;

	private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationSenderServiceImpl.class);

	/**
	 * The below method sendSlackNotification accepts a list of failed and
	 * successfully created organizations in bulk upload operation and creates
	 * an attachment with all the organizations along with there status and send
	 * notification to the channel set in Environment Variables. This method
	 * requires environment, application.properties variables,as
	 * SLACK_CHANNEL,SLACK_AUTH_TOKEN and SLACK_UPLOAD_FILE_API_URL which will
	 * be created through Slack for the particular channel
	 */
	@Override
	public void sendSlackNotification(List<Organization> successOrganizationsList,
			List<OrganizationBulkFailedPayload> failedOrganizationsList, UserPayload user, Date date) {
		try {
			// list of failed and success organizations
			StringBuilder successOrganizations = new StringBuilder();
			StringBuilder failedOrganizations = new StringBuilder();
			// append success organizations
			for (Organization organization : successOrganizationsList) {
				if (null != organization.getId()) {
					successOrganizations.append(organization.getId().toString());
					successOrganizations.append(",");
					successOrganizations.append("\"");
					successOrganizations.append(organization.getName());
					successOrganizations.append("\"");
					successOrganizations.append(",");
					successOrganizations.append("SUCCESS");
					successOrganizations.append("\n");
				}
			} // end of for loop

			// append failed organizations
			for (OrganizationBulkFailedPayload failedOrg : failedOrganizationsList) {
				failedOrganizations.append("");
				failedOrganizations.append(",");
				failedOrganizations.append("\"");
				if (null != failedOrg.getFailedOrganization()) {
					failedOrganizations.append(failedOrg.getFailedOrganization().getName());
					failedOrganizations.append("\"");
					failedOrganizations.append(",");
					failedOrganizations.append("FAILED");
					failedOrganizations.append("\"");
					failedOrganizations.append(",");
					failedOrganizations.append(failedOrg.getFailedMessage());
				}

				failedOrganizations.append("\n");
			} // end of for loop

			String formattedDte = new SimpleDateFormat("yyyyMMddHHmmss").format(date);
			// write list of success and failed organizations into .csv
			File file = new File("organization_bulk_upload_result_" + formattedDte + ".csv");
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
			csvWriter.append("# Below are the List of uploaded organizations").append("\n").append("Organization Id")
					.append(",").append("Organization Name").append(",").append("Organization Status").append(",")
					.append("Comment").append("\n");
			csvWriter.append(successOrganizations);
			csvWriter.append(failedOrganizations);
			csvWriter.flush();
			csvWriter.close();

			String fileContent = FileUtils.readFileToString(file, "UTF-8");
			SlackMessage slackMessage = SlackMessage.builder().filetype("csv").filename(file.getName())
					.username("WinWinUploadNotifier").content(fileContent)
					.initial_comment("WinWinWiki editor bulk upload status file, created by: "
							+ user.getUserDisplayName() + " at " + date)
					.channels(SLACK_CHANNEL).build();
			LOGGER.info("SLACK_CHANNEL_NAME " + SLACK_CHANNEL);
			// send post request to slack channel
			sendPostRequest(slackMessage);
			try {
				LOGGER.info("deleting existing Bulk Upload file: " + file.getName());
				file.delete();
				LOGGER.info("Bulk Upload file  " + file.getName() + " has been successfully deleted");
			} catch (Exception e) {
				LOGGER.error("failed to delete file: " + file.getName(), e);
			}
			int numOfOrganizations = successOrganizationsList.size() + failedOrganizationsList.size();
			LOGGER.info("org service createOrganizations() ended with number of organizations - " + numOfOrganizations
					+ " created by: " + user.getUserDisplayName());

		} catch (Exception e) {
			LOGGER.error("exception occured while sending notification", e);
		}

	}

	/**
	 * Sends File Notifications to Slack on the channel specified in
	 * SlackMessage
	 * 
	 * @param message
	 */
	private void sendPostRequest(SlackMessage message) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (!StringUtils.isEmpty(message.getFiletype()))
			params.add(new BasicNameValuePair("filetype", message.getFiletype()));

		if (!StringUtils.isEmpty(message.getFilename()))
			params.add(new BasicNameValuePair("filename", message.getFilename()));

		if (!StringUtils.isEmpty(message.getUsername()))
			params.add(new BasicNameValuePair("username", message.getUsername()));

		if (!StringUtils.isEmpty(message.getContent()))
			params.add(new BasicNameValuePair("content", message.getContent()));

		if (!StringUtils.isEmpty(message.getInitial_comment()))
			params.add(new BasicNameValuePair("initial_comment", message.getInitial_comment()));

		if (!StringUtils.isEmpty(message.getChannels()))
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
			LOGGER.info("SLACK_CHANNEL_NAME " + message.getChannels());

		} catch (MalformedURLException e) {
			LOGGER.error("exception occured while sending notification", e);
		} catch (ProtocolException e) {
			LOGGER.error("exception occured while sending notification", e);
		} catch (IOException e) {
			LOGGER.error("exception occured while sending notification", e);
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

	/**
	 * Sends Message Notifications to Slack on the channel specified in
	 * SlackMessage
	 * 
	 * @param message
	 */
	@Override
	public void sendSlackMessageNotification(SlackMessage message) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();

		if (!StringUtils.isEmpty(message.getUsername()))
			params.add(new BasicNameValuePair("username", message.getUsername()));

		if (!StringUtils.isEmpty(message.getText()))
			params.add(new BasicNameValuePair("text", message.getText()));

		if (!StringUtils.isEmpty(message.getChannel()))
			params.add(new BasicNameValuePair("channel", message.getChannel()));

		if (!StringUtils.isEmpty(message.getAs_user()))
			params.add(new BasicNameValuePair("as_user", message.getAs_user()));

		try {
			URL obj = new URL(System.getenv("SLACK_CHAT_POST_MESSAGE_API_URL"));
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
			LOGGER.info("SLACK_CHANNEL_NAME " + message.getChannels());

		} catch (MalformedURLException e) {
			LOGGER.error("exception occured while sending notification", e);
		} catch (ProtocolException e) {
			LOGGER.error("exception occured while sending notification", e);
		} catch (IOException e) {
			LOGGER.error("exception occured while sending notification", e);
		}
	}

}
