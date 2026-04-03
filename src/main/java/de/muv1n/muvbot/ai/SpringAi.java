package de.muv1n.muvbot.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class SpringAi {

    private final ChatClient chatClient;

    public SpringAi(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String prompt(String message) {
        return chatClient
                .prompt()
                .user(message)
                .call()
                .content();
    }
}


