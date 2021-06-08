package uk.co.corasoftware;

import javax.security.auth.login.LoginException;

import org.springframework.beans.factory.annotation.Value;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Application {

	@Value("${DISCORD_TOKEN}")
	private String token;

	public static void main(String[] args) {
		try {
			JDA jda = JDABuilder.createDefault("").addEventListeners(new MessageListener()).build();
			jda.awaitReady();
		} catch (LoginException | InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}
}
