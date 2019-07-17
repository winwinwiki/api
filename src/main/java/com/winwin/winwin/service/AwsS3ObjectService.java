/**
 * 
 */
package com.winwin.winwin.service;

import java.io.File;

import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

/**
 * @author ArvindKhatik
 *
 */
public interface AwsS3ObjectService {
	public S3Object getS3Object(String key) throws Exception;

	public S3Object getS3Object(String bucketName, String key) throws Exception;

	public S3Object putS3ObjectWithResult(File file) throws Exception;

	public PutObjectResult putS3Object(File file) throws Exception;

}
