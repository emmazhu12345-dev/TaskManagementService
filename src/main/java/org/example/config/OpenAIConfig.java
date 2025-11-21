package org.example.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Bean
    public OpenAIClient openAIClient() {
        // Configures using OPENAI_API_KEY / OPENAI_ORG_ID / OPENAI_PROJECT_ID environment variables
        return OpenAIOkHttpClient.fromEnv();
    }
}
