package net.cubespace.dynmap.multiserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.cubespace.dynmap.multiserver.Config.Dynmap;
import net.cubespace.dynmap.multiserver.GSON.*;
import net.cubespace.dynmap.multiserver.util.AbstractFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public abstract class AbstractDynmapServer implements DynmapServer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDynmapServer.class);
    private final Integer updateInterval;
    private final Gson gson = new Gson();
    private DynmapConfig dynmapConfig;

    //This dynmaps worlds
    private final HashMap<String, DynmapWorldConfig> dynmapWorldConfigs = new HashMap<>();

    private final ArrayList<String> alreadyPlayers = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
	private final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1, r -> {
		var t = new Thread(r);
		t.setDaemon(true);
		return t;
	});
    private AbstractFile dynmapConfigFile;

    private class DynmapServerUpdater implements Runnable {

        public DynmapServerUpdater() {
        }

        public void run() {
            try {
                loadWorlds();
            } catch (DynmapInitException e) {
                logger.warn("Error in getting new Worlds", e);
            }

            players.clear();
            alreadyPlayers.clear();

            for (Map.Entry<String, DynmapWorldConfig> dynmapWorldConfig : new HashMap<>(dynmapWorldConfigs).entrySet()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(getFile("standalone" + File.separator + "dynmap_" + dynmapWorldConfig.getKey() + ".json").getInputStream()))) {
                    dynmapWorldConfig.setValue(gson.fromJson(reader, DynmapWorldConfig.class));
                    dynmapWorldConfigs.put(dynmapWorldConfig.getKey(), dynmapWorldConfig.getValue());

                    for (Player player : dynmapWorldConfig.getValue().getPlayers()) {
                        if (!alreadyPlayers.contains(player.getName())) {
                            alreadyPlayers.add(player.getName());
                            players.add(player);
                        }
                    }
                } catch (IOException e) {
                    logger.warn("Could not update Dynmap World", e);
                }
            }
        }
    }

    public AbstractDynmapServer(Dynmap config) {
        updateInterval = config.UpdateInterval;
    }

    @Override
    public void initialize() throws DynmapInitException {
        //Check if the installation is correct
        preCheck();

        //Read the Dynmap config
        readDynmapConfig();

        //Check if all Worlds in the Config are there
        loadWorlds();

        service.scheduleWithFixedDelay(new DynmapServerUpdater(), 0, updateInterval, TimeUnit.SECONDS);
    }

    private void loadWorlds() throws DynmapInitException {
        for (DynmapWorld world : dynmapConfig.getWorlds()) {
			logger.trace("Checking World {}", world.getName());

            AbstractFile dynmapWorldConfig = null;
            try {
                dynmapWorldConfig = getFile("standalone/dynmap_" + world.getName() + ".json");
            } catch (IOException e) {
                throw new DynmapInitException(e);
            }

            if (!dynmapWorldConfig.exists()) {
                throw new DynmapInitException("World " + world.getName() + " has no config in Dynmap");
            }

            if (!dynmapWorldConfigs.containsKey(world.getName())) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(dynmapWorldConfig.getInputStream()))) {
                    DynmapWorldConfig dynmapWorldConfig1 = gson.fromJson(reader, DynmapWorldConfig.class);
                    dynmapWorldConfigs.put(world.getName(), dynmapWorldConfig1);

					logger.debug("Loaded World {}", world.getName());
                } catch(JsonSyntaxException e) {
					logger.error("JsonSyntaxException {} has broken", dynmapWorldConfig.getName(), e);
                    throw e;
                } catch (FileNotFoundException e) {
                    logger.error("Error in reading in the Worldfile", e);
                } catch (IOException e) {
                    logger.error("Error in closing in the Worldfile", e);
                }
            }
        }
    }

    private void readDynmapConfig() throws DynmapInitException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader((dynmapConfigFile.getInputStream())))) {
            Gson gsonDymapConfig = new GsonBuilder().registerTypeAdapter(Component.class, new ComponentDeserializer()).create();
            dynmapConfig = gsonDymapConfig.fromJson(reader, DynmapConfig.class);
            Main.updateCoreVersion(dynmapConfig.getCoreversion());
            Main.updateDynmapVersion(dynmapConfig.getDynmapversion());
        } catch (IOException e) {
            throw new DynmapInitException("Could not parse dynmap_config.json", e);
        }
    }

    private void preCheck() throws DynmapInitException {
        /**
         * The Dynmap must contain:
         * standalone/dynmap_config.json
         */
        try {
            dynmapConfigFile = getFile("standalone/dynmap_config.json");
        } catch (IOException e) {
            throw new DynmapInitException("Fail get config file", e);
        }
        logger.debug("dynmapConfigFile is {}", dynmapConfigFile);

        if (!dynmapConfigFile.exists()) {
            try {
                Thread.sleep(200);
                if (!dynmapConfigFile.exists()) {
                    throw new DynmapInitException("dynmap_config.json in the standalone Folder is missing. Be sure you started the Dynmap");
                }
            } catch (InterruptedException e) {
                throw new DynmapInitException("dynmap_config.json in the standalone Folder is missing. Be sure you started the Dynmap");
            }
        }
    }

    @Override
    public Collection<DynmapWorld> getWorlds() {
        return dynmapConfig.getWorlds();
    }

    @Override
    public DynmapWorldConfig getWorldConfig(String name) {
        return dynmapWorldConfigs.get(name);
    }

    @Override
    public DynmapConfig getDynmapConfig() {
        return dynmapConfig;
    }

    public Collection<Component> getComponents() {
        return dynmapConfig.getComponents();
    }

    public Collection<Player> getPlayers() {
        return players;
    }
}
