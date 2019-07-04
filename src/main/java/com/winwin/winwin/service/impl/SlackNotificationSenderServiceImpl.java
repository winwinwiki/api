/**
 * 
 */
package com.winwin.winwin.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.api.model.Field;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.service.SlackNotificationSenderService;

/**
 * @author ArvindKhatik
 *
 */
@Service
public class SlackNotificationSenderServiceImpl implements SlackNotificationSenderService {

	/**
	 * 
	 */
	public SlackNotificationSenderServiceImpl() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void sendSlackNotification(List<Organization> organizations, UserPayload user, Date date) {
		try {
			String url = System.getenv("SLACK_WEBHOOK_URL");
			if (url == null) {
				throw new IllegalStateException("Environment variable SLACK_WEBHOOK_URL must be defined");
			}

			Attachment attachment = Attachment.builder()
					// .text("bulk_upload_status.text")
					// .authorName(user.getUserDisplayName())
					.filename("bulk_upload_status.text").color("#36a64f")
					.fallback("Required plain-text summary of the attachment.")
					// .title("Organizations Status")
					.footer("footer").fields(new ArrayList<>()).mrkdwnIn(new ArrayList<>()).build();

			for (Organization organization : organizations) {
				{
					Field field = Field.builder().title("Organization Id").value(organization.getId().toString())
							.valueShortEnough(false).build();
					attachment.getFields().add(field);
				}
				{
					Field field = Field.builder().title("Organization Name").value(organization.getName())
							.valueShortEnough(false).build();
					attachment.getFields().add(field);
				}
				{
					Field field = Field.builder().title("Organization Status").value("SUCCESS").valueShortEnough(false)
							.build();
					attachment.getFields().add(field);
				}
			}

			attachment.getMrkdwnIn().add("text");
			Payload payload = Payload.builder().channel(System.getenv("SLACK_CHANNEL_NAME"))
					.username("WinWinUploadNotifier").attachments(new ArrayList<>())
					.text("WinWinWiki editor bulk upload status, created by: " + user.getUserDisplayName() + " at "
							+ date)
					.build();

			payload.getAttachments().add(attachment);

			Slack slack = Slack.getInstance();
			WebhookResponse response = slack.send(url, payload);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
