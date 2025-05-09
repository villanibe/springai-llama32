package dev.villani.ai.applications.chefassistant;

import dev.villani.ai.shared.ChatHistoryController;
import dev.villani.ai.shared.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.villani.ai.utils.ChatConstants.USER_NAME_HEADER;
import static dev.villani.ai.utils.ChatUtils.getConversationId;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/chef-assistant")
public class ChefAssistantController {

    public record CountryCuisines(String country, String recipeName, List<String> instructions) {}

    private final ChatClient chatClient;

    @Value("classpath:/prompts/chef_assistant.st")
    private Resource systemPrompt;

    @GetMapping
    public CountryCuisines processRecipeQuestion(@RequestParam(value = "country") String country,
                                                 @RequestParam(value = "language") String language,
                                                 @RequestHeader(value = USER_NAME_HEADER, required = false) String userName) {

        String conversationId = getConversationId(userName);
        Prompt prompt = new PromptTemplate(systemPrompt)
                .create( Map.of(
                        "country", country,
                        "language", language
                ));

        return chatClient.prompt(prompt)
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .entity(CountryCuisines.class);
    }

}
