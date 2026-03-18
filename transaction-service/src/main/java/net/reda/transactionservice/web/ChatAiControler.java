package net.reda.transactionservice.web;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatAiControler {

    private final ChatLanguageModel chatLanguageModel;

    public ChatAiControler(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String question){
        return chatLanguageModel.chat(question);
    }
}
