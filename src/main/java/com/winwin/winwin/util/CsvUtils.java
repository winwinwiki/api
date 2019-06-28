package com.winwin.winwin.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.exception.ExceptionResponse;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class CsvUtils {
	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger log = LoggerFactory.getLogger(CsvUtils.class);

	private static final CsvMapper mapper = new CsvMapper();

	public <T> List<T> read(Class<T> clazz, MultipartFile file, ExceptionResponse response) {

		List<T> list = new ArrayList<>();
		try {
			CsvSchema schema = mapper.schemaFor(clazz).withHeader().withColumnReordering(true);
			ObjectReader reader = mapper.readerFor(clazz).with(schema);
			list = reader.<T> readValues(file.getInputStream()).readAll();
		} catch (Exception e) {
			log.error(customMessageSource.getMessage("csv.error"), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setException(e);
		}
		return list;
	}

	public <T> List<T> read(Class<T> clazz, InputStream stream, ExceptionResponse response) {

		List<T> list = null;
		try {
			CsvSchema schema = mapper.schemaFor(clazz).withHeader().withColumnReordering(true);
			ObjectReader reader = mapper.readerFor(clazz).with(schema);
			list = reader.<T> readValues(stream).readAll();
		} catch (Exception e) {
			log.error(customMessageSource.getMessage("csv.error"), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setException(e);
		}
		return list;
	}

	public <T> List<T> read(Class<T> clazz, String csv, ExceptionResponse response) {

		List<T> list = null;
		try {
			CsvSchema schema = mapper.schemaFor(clazz).withHeader().withColumnReordering(true);
			ObjectReader reader = mapper.readerFor(clazz).with(schema);
			list = reader.<T> readValues(csv).readAll();
		} catch (Exception e) {
			log.error(customMessageSource.getMessage("csv.error"), e);
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			response.setException(e);
		}
		return list;
	}
}