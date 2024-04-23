package net.cubespace.dynmap.multiserver.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author teruteru128 (teruterubouzu1024+dynmap@gmail.com)
 */
public class ConfigFactory {
    private static final Path FILE_PATH = Paths.get("config/main.yml");
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    public static Main getConfig() throws IOException {
        if (Files.exists(FILE_PATH)) {
            // load
            return load();
        } else {
            // create directory
            Files.createDirectories(FILE_PATH.getParent());
            // save initial config file
            var config = new Main();
            try(var writer = Files.newBufferedWriter(FILE_PATH)) {
                mapper.writeValue(writer, config);
            }
            return config;
        }
    }

    public static Main load() throws IOException {
        try(var reader = Files.newBufferedReader(FILE_PATH)) {
            return mapper.readValue(reader, Main.class);
        }
    }
}
