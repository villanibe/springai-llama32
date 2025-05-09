package dev.villani.ai.infra.config.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatConfig {

    @Value("classpath:/prompts/conversational.st")
    private Resource defaultSystemPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultSystem(defaultSystemPrompt)
                .defaultAdvisors(
                        //new MessageChatMemoryAdvisor(chatMemory()),
                        new PromptChatMemoryAdvisor(chatMemory),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    @Bean(name = "inMemory")
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }
}
