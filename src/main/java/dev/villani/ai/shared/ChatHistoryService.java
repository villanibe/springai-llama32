package dev.villani.ai.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;

import static dev.villani.ai.utils.ChatUtils.getConversationId;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.DEFAULT_CHAT_MEMORY_RESPONSE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatHistoryService {

    private final ChatMemory chatMemory;

    public List<Message> getChatHistory(final String userId) {
        String conversationId = getConversationId(userId);
        log.info("Retrieving chat history for user: {}", userId);
        return chatMemory.get(conversationId, DEFAULT_CHAT_MEMORY_RESPONSE_SIZE)
                .stream()
                .toList();

    }

    public void clearHistory(final String userId) {
        String conversationId = getConversationId(userId);
        log.info("Deleting chat history for user: {}", userId);
        chatMemory.clear(conversationId);
    }
}
