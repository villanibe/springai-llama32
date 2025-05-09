package dev.villani.ai.shared;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static dev.villani.ai.utils.ChatConstants.USER_NAME_HEADER;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chat-history")
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping
    protected ResponseEntity<List<Message>> getChatHistory(@RequestHeader(value = USER_NAME_HEADER) String userId) {
        return ResponseEntity.ok(chatHistoryService.getChatHistory(userId));

    }

    @DeleteMapping
    protected ResponseEntity<Void> clearHistory(@RequestHeader(value = USER_NAME_HEADER) String userId) {
        chatHistoryService.clearHistory(userId);
        return ResponseEntity.noContent().build();
    }
}
