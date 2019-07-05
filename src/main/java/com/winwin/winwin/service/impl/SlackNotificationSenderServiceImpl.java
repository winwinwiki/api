package com.winwin.winwin.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.SlackNotificationSenderService;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class SlackNotificationSenderServiceImpl implements SlackNotificationSenderService {

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(SlackNotificationSenderServiceImpl.class);

	/**
	 * The below method sendSlackNotification accepts a list of failed and
	 * successfully created organizations in bulk upload operation and creates
	 * an attachment with all the organizations with along with there status and
	 * send notification to the channel set in Environment Variables. This
	 * method needs a channel name and a WEBHOOK_URL which will be created
	 * through Slack for the particular channel
	 */
	@Override
	public void sendSlackNotification(List<Organization> organizations, UserPayload user, Date date) {
		try {
			String url = System.getenv("SLACK_WEBHOOK_URL");

			if (url == null) {
				LOGGER.error("Environment variable SLACK_WEBHOOK_URL must be defined");
			}

			// create attachment for slack notification
			Attachment attachment = Attachment.builder()
					// .text("bulk_upload_status.text")
					// .authorName(user.getUserDisplayName())
					.filename("bulk_upload_status.text").color("#36a64f")
					.fallback("Required plain-text summary of the attachment.")
					// .title("Organizations Status")
					.footer("footer").fields(new ArrayList<>()).mrkdwnIn(new ArrayList<>()).build();

			// list of failed and success organizations
			List<Field> fieldsForSuccessOrganizations = new ArrayList<Field>();
			attachment.getFields();
			List<Field> fieldsForFailedOrganizations = new ArrayList<Field>();
			attachment.getFields();

			for (Organization organization : organizations) {
				if (null != organization.getId()) {
					{
						Field field = Field.builder().title("Organization Id").value(organization.getId().toString())
								.valueShortEnough(false).build();
						fieldsForSuccessOrganizations.add(field);
					}
					{
						Field field = Field.builder().title("Organization Name").value(organization.getName())
								.valueShortEnough(false).build();
						fieldsForSuccessOrganizations.add(field);
					}
					{
						Field field = Field.builder().title("Organization Status").value("SUCCESS")
								.valueShortEnough(false).build();
						fieldsForSuccessOrganizations.add(field);
					}
				} else {
					{
						Field field = Field.builder().title("Organization Name").value(organization.getName())
								.valueShortEnough(false).build();
						fieldsForFailedOrganizations.add(field);
					}
					{
						Field field = Field.builder().title("Organization Status").value("FAILED")
								.valueShortEnough(false).build();
						fieldsForFailedOrganizations.add(field);
					}
				}

			}

			// Add list of failed and success organizations into the attachment
			attachment.getFields().addAll(fieldsForSuccessOrganizations);
			attachment.getFields().addAll(fieldsForFailedOrganizations);
			attachment.getMrkdwnIn().add("text");

			Payload payload = Payload.builder().channel(System.getenv("SLACK_CHANNEL_NAME"))
					.username("WinWinUploadNotifier").attachments(new ArrayList<>())
					.text("WinWinWiki editor bulk upload status, created by: " + user.getUserDisplayName() + " at "
							+ date)
					.build();

			payload.getAttachments().add(attachment);

			// Get slack instance and send a payload on the mentioned url
			Slack slack = Slack.getInstance();
			@SuppressWarnings("unused")
			WebhookResponse response = slack.send(url, payload);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
