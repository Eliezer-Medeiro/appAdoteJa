package br.appAdoteJa.appAdoteJa.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration // 1. Diz ao Spring que esta é uma classe de configuração
public class CloudinaryConfig {

    // 2. Lê os valores do seu application.properties
    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    @Bean // 3. Diz ao Spring: "Crie este objeto (um 'Bean') para eu injetar em outros lugares"
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true"); // Corrigido
    
        return new Cloudinary(config);
    }s
}
