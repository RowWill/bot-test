package uk.co.corasoftware.mongo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * Implemented before spring-core was added.
 * TODO switch to Spring
 */

public class PropertiesLoader {

	private PropertiesLoader() {
	}

	public static String getPropertyValue(String key) {
		InputStream inputStream = null;

		try {
			Properties prop = new Properties();
			String propFileName = "application.properties";

			inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException(
						String.format("Property file [%s] not found on classpath", propFileName));
			}

			return prop.getProperty(key);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}
}
