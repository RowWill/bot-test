package uk.co.corasoftware.jelastic;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jelastic.api.environment.Control;
import com.jelastic.api.users.Authentication;
import com.jelastic.api.users.response.AuthenticationResponse;

import net.dv8tion.jda.api.entities.User;

public class JelasticWrapper {

	private static final String HOSTER_URL = "";
	private static final String USER_EMAIL = "";
	private static final String USER_PASSWORD = "";
	private static final String ENV_NAME = "";

	private static Authentication authenticationService;
	private static Control environmentService;

	private static JSONArray nodes = new JSONArray();

	int counter = 1;

	static {
		authenticationService = new Authentication();
		authenticationService.setServerUrl(HOSTER_URL + "/1.0/");

		environmentService = new Control();
		environmentService.setServerUrl(HOSTER_URL + "/1.0/");

		// @formatter:off
		JSONObject nodejsNode = new JSONObject()
				.put("count", 1)
				.put("diskLimit", 5)
				.put("displayName", "Base-Node-Server")
				.put("extip", 0)
				.put("extipv6", 0)
				.put("fixedCloudlets", 1)
				.put("flexibleCloudlets", 4)
				.put("nodeGroup", "cp")
				.put("nodeType", "nodejs")
				.put("restartDelay", 30)
				.put("scalingMode", "STATEFUL")
				.put("tag", "14.11.0-supervisor");
		// @formatter:on
		nodes.put(nodejsNode);
	}

	public void createNodejsNode(User user) {
		AuthenticationResponse authenticationResponse = authenticationService.signin(USER_EMAIL, USER_PASSWORD);
		if (!authenticationResponse.isOK()) {
			System.exit(authenticationResponse.getResult());
		}

		final String session = authenticationResponse.getSession();

		// @formatter:off
		JSONObject env = new JSONObject()
				.put("ishaenabled", false)
				.put("region", "fr-2")
				.put("shortdomain", ENV_NAME)
				.put("sslstate", true);

		JSONObject nodejsNode = new JSONObject()
				.put("id", user.getIdLong())
				.put("count", 1)
				.put("diskLimit", 5)
				.put("displayName", user.getName() +"-NodeJS Server")
				.put("extip", 0)
				.put("extipv6", 0)
				.put("fixedCloudlets", 1)
				.put("flexibleCloudlets", 4)
				.put("nodeGroup", "cp" + ++counter)
				.put("nodeType", "nodejs")
				.put("restartDelay", 30)
				.put("scalingMode", "STATEFUL")
				.put("tag", "14.11.0-supervisor");
				// @formatter:on

		nodes.put(nodejsNode);
		environmentService.changeTopology(ENV_NAME, session, "", env.toString(),
				nodes.toString());
	}

}
