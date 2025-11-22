package org.example.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient() {
        // Read API key from environment explicitly
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not set in environment variables");
        }

        // You can also set organization or project here if needed.
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}
