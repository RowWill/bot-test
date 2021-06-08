package uk.co.corasoftware.mongo;

import lombok.Builder;

@Builder(buildMethodName = "flatten")
public class MongoConnectionBuilder {

	private static final String AT = "@";
	private static final String SEPARATOR = "://";
	private static final String COLON = ":";
	private static final String PROTOCOL = "mongodb";

	private String username;
	private String password;
	private String address;

	public String create() {
		return PROTOCOL + SEPARATOR + username + COLON + password + AT + address;
	}

	public MongoConnectionBuilder and() {
		return this;
	}
}
