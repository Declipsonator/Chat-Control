package me.declipsonator.chatcontrol;

import me.declipsonator.chatcontrol.command.FilterCommand;
import me.declipsonator.chatcontrol.command.MuteCommand;
import me.declipsonator.chatcontrol.util.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;


public class ChatControl implements ModInitializer {
    public static final Logger LOG = LogManager.getLogger("Chat Control");
    public static final Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("chatcontrol.json");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Chat Control");
        Config.loadConfig();

        Runtime.getRuntime().addShutdownHook(new Thread(Config::saveConfig));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            FilterCommand.register(dispatcher);
            MuteCommand.register(dispatcher);
        });

    }

}
