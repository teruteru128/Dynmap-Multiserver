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

    public static Main getConfig() throws IOException {
        final var filePath = Path.of("").resolve("config/main.yml");
        if (Files.exists(filePath)) {
            // load
            return load(filePath);
        } else {
            // create directory
            Files.createDirectories(filePath.getParent());
            // save initial config file
            var config = new Main();
            try(var writer = Files.newBufferedWriter(filePath)) {
                mapper.writeValue(writer, config);
            }
            return config;
        }
    }

    public static Main load(Path filePath) throws IOException {
        try(var reader = Files.newBufferedReader(filePath)) {
            return mapper.readValue(reader, Main.class);
        }
    }
}
