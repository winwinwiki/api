/**
 * 
 */
package com.winwin.winwin.service;

import com.amazonaws.services.s3.model.S3Object;

/**
 * @author ArvindKhatik
 *
 */
public interface GetAwsS3ObjectService {
	public S3Object getS3Object(String key) throws Exception;

}
