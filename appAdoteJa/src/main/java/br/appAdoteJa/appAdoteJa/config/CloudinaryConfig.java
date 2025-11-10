package br.appAdoteJa.appAdoteJa.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud_name}")
    private String cloudName;

    @Value("${cloudinary.api_key}")
    private String apiKey;

    @Value("${cloudinary.api_secret}")
    private String apiSecret;

    /**
     * Este método @Bean cria o objeto Cloudinary.
     * Ele lê as chaves do application.properties e retorna um objeto
     * que o Spring pode injetar (@Autowired) em outros lugares,
     * como no seu AnimalController.
     */
    @Bean
    public Cloudinary cloudinary() {
        // Usamos Map<String, Object> para aceitar
        // tanto Strings (apiKey) quanto booleans (secure=true)
        Map<String, Object> config = new HashMap<>();
        
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", true);
        
        return new Cloudinary(config);
    }
}
