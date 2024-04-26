package net.cubespace.dynmap.multiserver.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author teruteru128 (teruterubouzu1024+dynmap@gmail.com)
 */
public class ConfigFactory {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static final Logger logger = LoggerFactory.getLogger(ConfigFactory.class);

    public static ServerConfig getConfig() throws IOException {
        final var filePath = Path.of("").resolve("config/main.yml");
        logger.debug("config/main.yml path is \"{}\"", filePath);
        if (Files.exists(filePath)) {
            logger.debug("main.yml is found");
            // load
            return load(filePath);
        } else {
            logger.debug("main.yml is not found");
            // create directory
            Files.createDirectories(filePath.getParent());
            // save initial config file
            var config = new ServerConfig();
            try(var outputStream = Files.newOutputStream(filePath)) {
                mapper.writeValue(outputStream, config);
                logger.debug("main.yml created");
            }
            return config;
        }
    }

    public static ServerConfig load(Path filePath) throws IOException {
        try(var inputStream = Files.newInputStream(filePath)) {
            return mapper.readValue(inputStream, ServerConfig.class);
        } finally {
            logger.debug("main.yml is loaded");
        }
    }
}
