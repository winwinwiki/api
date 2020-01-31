/**
 * 
 */
package com.winwin.winwin.service;

import com.winwin.winwin.payload.UserPayload;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
public interface WinWinElasticSearchService {

	public void sendPostRequestToElasticSearch(UserPayload user);

}
