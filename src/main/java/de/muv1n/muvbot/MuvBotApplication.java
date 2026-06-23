package de.muv1n.muvbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MuvBotApplication {

    private static final Logger logger = LoggerFactory.getLogger(MuvBotApplication.class);

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
        logger.info("\n\nDatabase configuration: jdbcUrl={}, user={}, password={}\n\n",
                isConfigured("DB_USER"),
                isConfigured("DB_URL"),
                isConfigured("DB_PASS"));

        SpringApplication.run(MuvBotApplication.class, args);
    }

    private static void setIfPresent(Dotenv dotenv, String key) {
        String value = dotenv.get(key, null);
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
    }
    private static String isConfigured(String key) {
        String value = System.getProperty(key);
        return value;
    }
}
