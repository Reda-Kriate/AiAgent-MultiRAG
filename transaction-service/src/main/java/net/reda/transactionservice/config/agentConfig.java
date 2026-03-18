package net.reda.transactionservice.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.Tokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class agentConfig {

    @Bean
    public ChatMemoryProvider chatMemoryProvider(Tokenizer tokenizer){
        return chatId -> MessageWindowChatMemory.withMaxMessages(10);
    }
}
