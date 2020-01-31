package com.winwin.winwin.service.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.winwin.winwin.service.AwsS3ObjectService;

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
public class AwsS3ObjectServiceImpl implements AwsS3ObjectService {
	private static final Logger logger = LoggerFactory.getLogger(AwsS3ObjectServiceImpl.class);

	String clientRegion = System.getenv("AWS_REGION");

	@Value("${aws.s3.bucket.name}")
	String bucketName;

	@Value("${aws.s3.bucket.naics.key.name}")
	String naicsAwsKey;

	@Value("${aws.s3.bucket.ntee.key.name}")
	String nteeAwsKey;

	private static final String AWS_BUCKET_NAME = "winwin-public-bucket-dev";
	private static final String AWS_BUCKET_UPLOAD_FILE = "bulk_upload_result_1.csv";

	EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();

	/**
	 * Get a File Object from Specified S3 Bucket
	 * 
	 * @param key
	 */
	@Override
	public S3Object getS3Object(String key) throws Exception {
		S3Object s3Object = null;
		AmazonS3 s3Client = getS3Client();
		logger.info("Downloading an object");
		s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		return s3Object;
	}

	/**
	 * Get a File Object from Specified S3 Bucket
	 * 
	 * @param bucketName
	 * @param key
	 */
	@Override
	public S3Object getS3Object(String bucketName, String key) throws Exception {
		S3Object s3Object = null;
		AmazonS3 s3Client = getS3Client();
		logger.info("Downloading an object");
		s3Object = s3Client.getObject(new GetObjectRequest(bucketName, key));
		return s3Object;
	}

	/**
	 * Push a File Object to Specified S3 Bucket and Return the File Object
	 * Again
	 * 
	 * @param file
	 */
	@Override
	public S3Object putS3ObjectWithResult(File file) throws Exception {
		AmazonS3 s3Client = getS3Client();
		logger.info("Pushing an object to S3");
		s3Client.putObject(new PutObjectRequest(AWS_BUCKET_NAME, AWS_BUCKET_UPLOAD_FILE, file));
		return getS3Object(AWS_BUCKET_NAME, AWS_BUCKET_UPLOAD_FILE);
	}

	/**
	 * Push a File Object to Specified S3 Bucket
	 * 
	 * @param file
	 */
	@Override
	public PutObjectResult putS3Object(File file) throws Exception {
		PutObjectResult putObjectResult = null;
		AmazonS3 s3Client = getS3Client();
		logger.info("Pushing an object to S3");
		putObjectResult = s3Client.putObject(new PutObjectRequest(AWS_BUCKET_NAME, AWS_BUCKET_UPLOAD_FILE, file));
		return putObjectResult;
	}

	/**
	 * Returns AWS S3 Client Object
	 * 
	 * @return
	 */
	private AmazonS3 getS3Client() {
		return AmazonS3ClientBuilder.standard().withRegion(clientRegion).withCredentials(envCredentialsProvider)
				.build();
	}

}
