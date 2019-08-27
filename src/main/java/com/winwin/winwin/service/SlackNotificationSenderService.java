/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.entity.SlackMessage;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface SlackNotificationSenderService {
	public void sendSlackNotification(List<Organization> successOrganizations, List<Organization> failedOrganizations,
			UserPayload user, Date date);

	public void sendSlackMessageNotification(SlackMessage message);

}
