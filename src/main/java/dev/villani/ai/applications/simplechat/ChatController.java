package dev.villani.ai.applications.simplechat;

import dev.villani.ai.shared.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static dev.villani.ai.utils.ChatConstants.USER_NAME_HEADER;
import static dev.villani.ai.utils.ChatUtils.getConversationId;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.*;



@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("api/chat")
public class ChatController {

    public static final String LLM_RESPONSE_EVENT = "llmResponse";

    private final ChatClient chatClient;

    @GetMapping
    public String processQuestion(@RequestParam("question") String question,
                                  @RequestHeader(value = USER_NAME_HEADER, required = false) String userName) {
        String conversationId = getConversationId(userName);
        return chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .user(question)
                .call()
                .content();
    }

    @GetMapping(value = "/streaming", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponseDTO> processQuestionWithStreaming(@RequestParam(value = "question", defaultValue = "Tell me a dog joke") String question,
                                                              @RequestHeader(value = USER_NAME_HEADER, required = false) String userName) {
        String conversationId = getConversationId(userName);
        return chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .user(question)
                .stream()
                .content()
                .map(value -> new ChatResponseDTO(value))
                .doOnSubscribe(subscription -> log.debug("SSE-Streaming started."))
                .doOnNext(value -> log.debug("SSE-Value sent: {}", value))
                .doOnError(error -> log.error("SSE-Error: {}", error.getMessage()))
                .doOnComplete(() -> log.debug("SSE-Streaming completed."));
    }

    @GetMapping(value = "/v2/streaming")
    public Flux<ServerSentEvent<ChatResponseDTO>> processQuestionWithStreamingSSEObject(
            @RequestParam(value = "question", defaultValue = "Tell me a cat joke") String question,
            @RequestHeader(value = USER_NAME_HEADER, required = false) String userName) {

        String conversationId = getConversationId(userName);
        AtomicInteger counter = new AtomicInteger(1);

        return chatClient.prompt()
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .user(question)
                .stream()
                .content()
                .map(buildLLMResponseEvent(counter))
                .doOnSubscribe(subscription -> log.debug("SSE-Streaming started."))
                .doOnNext(value -> log.debug("SSE-Value sent: {}", value))
                .doOnError(error -> log.error("SSE-Error: {}", error.getMessage()))
                .doOnComplete(() -> log.debug("SSE-Streaming complete."));

    }

    private static Function<String, ServerSentEvent<ChatResponseDTO>> buildLLMResponseEvent(final AtomicInteger counter) {
        return llmResponseSequence -> ServerSentEvent.<ChatResponseDTO>builder()
                .id(String.valueOf(counter.getAndIncrement()))
                .event(LLM_RESPONSE_EVENT)
                .data(new ChatResponseDTO(llmResponseSequence))
                .build();
    }

}
