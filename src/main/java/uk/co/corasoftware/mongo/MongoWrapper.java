package uk.co.corasoftware.mongo;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.MongoClientImpl;

public class MongoWrapper {

	private static final Logger LOG = LoggerFactory.getLogger(MongoWrapper.class);

	public boolean createNewUser(String username, String password) {
		// @formatter:off
		String connectionURL;
		try {
			connectionURL = MongoConnectionBuilder.builder()
					.username(getProperty(Property.MONGODB_DATABASE_USERNAME))
					.password(getProperty(Property.MONGODB_DATABASE_PASSWORD))
					.address(getProperty(Property.MONGODB_DATABASE_ADDRESS))
					.flatten().create();
		} catch (IOException e) {
			return false;
		}
		// @formatter:on

		MongoClient client = new MongoClientImpl(
				MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionURL)).build(), null);

		try {
			MongoDatabase db = createNewDb(client, username);

			if (db == null) {
				return false;
			}

			LOG.info("Creating database user [{}]", username);

			db.runCommand(new BasicDBObject("createUser", username).append("pwd", password).append("roles",
					Collections.singletonList(new BasicDBObject("role", "readWrite").append("db", username))));

			LOG.info("User [{}] created", username);
		} catch (MongoCommandException ex) {
			LOG.info("User [{}] or database [{}] already exists! Aborting...", username, username);
			LOG.debug("Cause: {}", ex);
		}
		client.close();

		return true;
	}

	private MongoDatabase createNewDb(MongoClient client, String name) {
		MongoDatabase db;
		try {
			db = client.getDatabase(name);
			db.createCollection(name);
			LOG.info("Database [{}] created", name);
		} catch (IllegalArgumentException ex) {
			LOG.info("Database [{}] creation failed!", name);
			LOG.debug("Cause: {}", ex);
			return null;
		}

		return db;
	}

	private String getProperty(String key) throws IOException {
		return PropertiesLoader.getPropertyValue(key);
	}
}
