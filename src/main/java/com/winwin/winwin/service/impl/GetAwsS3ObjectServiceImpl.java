package com.winwin.winwin.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.winwin.winwin.service.GetAwsS3ObjectService;
import com.winwin.winwin.util.CsvUtils;

/**
 * @author ArvindKhatik
 *
 */
// @PropertySource("application.properties"
@Component
public class GetAwsS3ObjectServiceImpl implements GetAwsS3ObjectService {
	private static final Logger logger = LoggerFactory.getLogger(CsvUtils.class);

	String clientRegion = System.getenv("AWS_REGION2");

	@Value("${aws.s3.bucket.name}")
	String bucketName;

	@Value("${aws.s3.bucket.key.name}")
	String key;

	EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();

	@Override
	public S3Object getS3Object() {

		S3Object s3Object = null;

		try {
			AmazonS3 s3Client = getS3Client();

			logger.info("Downloading an object");
			s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));

		} catch (AmazonServiceException e) {
			logger.error("", e);
		} catch (SdkClientException e) {
			logger.error("", e);
		}

		return s3Object;
	}

	/**
	 * @return
	 */
	private AmazonS3 getS3Client() {
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
				.withCredentials(envCredentialsProvider).build();
		return s3Client;
	}

}
