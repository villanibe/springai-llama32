package dev.villani.ai.utils;

import org.springframework.util.StringUtils;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_CONVERSATION_ID;

public final class ChatUtils {

    /* Naive approach to create a conversation id , use only for studies */
    public static String getConversationId(String userName) {
        return StringUtils.hasText(userName) ? userName : DEFAULT_CHAT_MEMORY_CONVERSATION_ID;
    }
}
