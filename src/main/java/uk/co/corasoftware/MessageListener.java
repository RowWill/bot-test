package uk.co.corasoftware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.corasoftware.jelastic.JelasticWrapper;
import uk.co.corasoftware.mongo.MongoWrapper;
import uk.co.corasoftware.mongo.PropertiesLoader;
import uk.co.corasoftware.mongo.Property;

public class MessageListener extends ListenerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(MessageListener.class);

	private Map<User, List<String>> rewarded = new HashMap<>();
	private List<String> messageQueue = new ArrayList<>();
	private User userToReward;

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		User author = event.getAuthor();
		Message message = event.getMessage();
		MessageChannel channel = event.getChannel();

		boolean bot = author.isBot();

		if (message.getContentDisplay().contains("!buy") && author.getId().equals("276038780648030211")) {
			try {
				User user = message.getMentionedUsers().get(0);

				MessageBuilder messageBuilder = new MessageBuilder();
				messageBuilder.mention(user);
				messageBuilder.append("**Congratulations ").append(user.getAsMention()).append("!**")
						.append("\nYou bought a tocken!\nReact with one of the following to pick your product:\n")
						.append("**1 x MongoDB Database : :m:**")
						.append("\n**1 x Node.js Server : :regional_indicator_n:**");

				Message rewardMessage = messageBuilder.build();
				channel.sendMessage(rewardMessage).queue(msg -> {
					msg.addReaction(getProperty(Property.REWARD_EMOTE_MONGODB)).queue();
					msg.addReaction(getProperty(Property.REWARD_EMOTE_NODEJS)).queue();

					messageQueue.add(msg.getId());
				});

				messageBuilder.clear();
				userToReward = user;
			} catch (IndexOutOfBoundsException ex) {
			}
		}

	}

	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		if (messageQueue.contains(event.getMessageId())) {
			if (event.getReactionEmote().toString().equals("RE:" + getProperty(Property.REWARD_EMOTE_MONGODB))
					&& event.getUser().equals(userToReward)) {
				LOG.info("User reacted to MongoDB");
				LOG.info("Generating MongoDB for User: {}", event.getUser());
				generateMongoDB(event.getUser());
				messageQueue.remove(event.getMessageId());
			} else if (event.getReactionEmote().toString().equals("RE:" + getProperty(Property.REWARD_EMOTE_NODEJS))
					&& event.getUser().equals(userToReward)) {
				event.getChannel().sendMessage(
						"You selected a Node.js server\nThe bot will take several minutes to configure the environment, please be patient.\n"
								+ "Details on how to connect and deploy to your new server will be messaged to you shortly.")
						.queue();
				generateNodejsNode(event.getUser());
				messageQueue.remove(event.getMessageId());
			}
		}
	}

	private void generateNodejsNode(User user) {
		new JelasticWrapper().createNodejsNode(user);
	}

	private void generateMongoDB(User user) {
		MongoWrapper wrapper = new MongoWrapper();
		List<String> rewards = rewarded.get(user);

		String password = generateRandomSpecialCharacters(8);
		String username = user.getName().replace(" ", "");
		LOG.info("Trimmed username: {}", username);

		if (rewards == null) {
			rewards = new ArrayList<>();
			rewarded.put(user, new ArrayList<>());
		}
		if (!rewards.contains("mongo")) {
			boolean success = wrapper.createNewUser(username, password);

			if (success) {
				StringBuilder sb = new StringBuilder();
				// @formatter:off
				sb.append("You successfully claimed a MongoDB database! Your details are as follows:\n")
						.append("Username: " + user.getName())
						.append("\nPassword: " + password)
						.append("\nDatabase address: 185.44.64.93\n")
						.append("Database name: " + username)
						.append("\nCollection name: " + username)
						.append("\n")
						.append("\nExample connection String: \"mongodb://" + username + ":" + password
								+ "@185.44.64.93\"")
						.append("\n\nFor help connecting your application to MongoDB refer to the docs here:\nhttps://docs.mongodb.com/drivers/");
				// @formatter:on
				user.openPrivateChannel().complete().sendMessage(sb.toString()).queue();
				rewards.add("mongo");
				rewarded.put(user, rewards);
			}
		}
	}

	private String generateRandomSpecialCharacters(int length) {
		char[] pair1 = { 'a', 'z' };
		char[] pair2 = { 'A', 'Z' };
		RandomStringGenerator pwdGenerator = new RandomStringGenerator.Builder()
				.withinRange(pair1, pair2).build();
		return pwdGenerator.generate(length);
	}

	private String getProperty(String key) {
		return PropertiesLoader.getPropertyValue(key);
	}
}
