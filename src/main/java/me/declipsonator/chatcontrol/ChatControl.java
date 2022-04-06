package me.declipsonator.chatcontrol;

import me.declipsonator.chatcontrol.command.FilterCommand;
import me.declipsonator.chatcontrol.command.MuteCommand;
import me.declipsonator.chatcontrol.util.CharArgumentType;
import me.declipsonator.chatcontrol.util.Config;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChatControl implements ModInitializer {
    public static final Logger LOG = LogManager.getLogger("Chat Control");
    public static final Path configFilePath = FabricLoader.getInstance().getConfigDir().resolve("chatcontrol.json");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Chat Control");
        Config.loadConfig();

        Runtime.getRuntime().addShutdownHook(new Thread(Config::saveConfig));
        ArgumentTypes.register("char", CharArgumentType.class, new ConstantArgumentSerializer(CharArgumentType::character));
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            FilterCommand.register(dispatcher);
            MuteCommand.register(dispatcher);
        });

    }

}
