package net.cubespace.dynmap.multiserver.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author teruteru128 (teruterubouzu1024+dynmap@gmail.com)
 */
public class ConfigFactory {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static ServerConfig getConfig() throws IOException {
        final var filePath = Path.of("").resolve("config/main.yml");
        if (Files.exists(filePath)) {
            // load
            return load(filePath);
        } else {
            // create directory
            Files.createDirectories(filePath.getParent());
            // save initial config file
            var config = new ServerConfig();
            try(var outputStream = Files.newOutputStream(filePath)) {
                mapper.writeValue(outputStream, config);
            }
            return config;
        }
    }

    public static ServerConfig load(Path filePath) throws IOException {
        try(var inputStream = Files.newInputStream(filePath)) {
            return mapper.readValue(inputStream, ServerConfig.class);
        }
    }
}
