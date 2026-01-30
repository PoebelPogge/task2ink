package pge.solutions.task2ink.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pge.solutions.task2ink.dto.AppConfig;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
@Slf4j
public class AppConfigLoader {

    @Bean
    public AppConfig appConfig(ObjectMapper objectMapper) throws IOException{
        File configFile = new File("./config.json");
        if(!configFile.exists()){
            throw new FileNotFoundException("Configuration file missing, check if config.json exists");
        }
        return objectMapper.readValue(configFile,AppConfig.class);
    }
}
