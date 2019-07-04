/**
 * 
 */
package com.winwin.winwin.service;

import java.util.Date;
import java.util.List;

import com.winwin.winwin.entity.Organization;
import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 *
 */
public interface SlackNotificationSenderService {
	public void sendSlackNotification(List<Organization> organizations, UserPayload user, Date date);

}
