package com.winwin.winwin.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class CsvUtils {
	private static final Logger log = LoggerFactory.getLogger(CsvUtils.class);

	private static final CsvMapper mapper = new CsvMapper();

	public static <T> List<T> read(Class<T> clazz, MultipartFile file) {
		List<T> list = new ArrayList<>();
		try {
			CsvSchema schema = mapper.schemaFor(clazz).withHeader().withColumnReordering(true);
			ObjectReader reader = mapper.readerFor(clazz).with(schema);
			list = reader.<T>readValues(file.getInputStream()).readAll();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return list;
	}
}