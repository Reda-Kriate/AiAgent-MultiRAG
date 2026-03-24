package net.reda.transactionservice.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

@AiService
public interface TransactionAiAgent {
    @SystemMessage("""
            You are a good assistant that can answer the user's question using provided tools and provided data.
            If your response contains the path of the images source in the context, use this format to present images in a list of items like: 
             - SOURCE_IMAGE(Path1)=>
             - SOURCE_IMAGE(Path2)=> 
            """)
    Flux<String> chat(String question);
}
