package de.muv1n.muvbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MuvBotApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();

        setIfPresent(dotenv, "DB_URL");
        setIfPresent(dotenv, "DB_USER");
        setIfPresent(dotenv, "DB_PASS");
        setIfPresent(dotenv, "OPENAI_API_KEY");
        setIfPresent(dotenv, "DISCORD_BOT_TOKEN");
        setIfPresent(dotenv, "DISCORD_CLIENT_ID");
        setIfPresent(dotenv, "DISCORD_CLIENT_SECRET");
        setIfPresent(dotenv, "DISCORD_REDIRECT_URI");

        SpringApplication.run(MuvBotApplication.class, args);
    }

    private static void setIfPresent(Dotenv dotenv, String key) {
        String value = dotenv.get(key, null);
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
}
