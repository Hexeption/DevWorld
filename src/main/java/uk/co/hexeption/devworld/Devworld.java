package uk.co.hexeption.devworld;

import java.io.File;
import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;

@Environment(EnvType.CLIENT)
public class Devworld implements ModInitializer {

    public static Devworld INSTANCE;

    private String worldName = "DevWorld";

    @Override
    public void onInitialize() {
        INSTANCE = new Devworld();
    }

    public void createWorld() {

        MinecraftClient.getInstance().openScreen(null);

        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.fromString("minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone;minecraft:desert;");

        LevelInfo levelInfo = new LevelInfo(worldName, 0, GameMode.CREATIVE, false, false, Difficulty.NORMAL,
            LevelGeneratorType.FLAT.loadOptions(flatChunkGeneratorConfig.toDynamic(NbtOps.INSTANCE)));

        File gameDir = MinecraftClient.getInstance().runDirectory;
        LevelStorage levelStorage = new LevelStorage(gameDir.toPath().resolve("saves"), gameDir.toPath().resolve("backups"), null);
        try {
            Session session = levelStorage.createSession(worldName);

            CompoundTag worldData = new CompoundTag();

            // Spawn Location
            worldData.putInt("SpawnX", 0);
            worldData.putInt("SpawnY", 55);
            worldData.putInt("SpawnZ", 0);

            worldData.putInt("Difficulty", 2);

            // World Generator
            worldData.putString("generatorName", "flat");
            worldData.put("generatorOptions", flatChunkGeneratorConfig.toDynamic(NbtOps.INSTANCE).getValue());

            // Cheat Mode
            worldData.putInt("GameType", GameMode.CREATIVE.getId());
            worldData.putBoolean("allowCommands", true);

            // World Initialized
            worldData.putBoolean("initialized", true);

            // Set Day
            worldData.putLong("Time", 6000);
            worldData.putLong("DayTime", 6000);

            // Game Rules
            CompoundTag gamerules = new CompoundTag();
            gamerules.putString("doWeatherCycle", "false");
            gamerules.putString("doDaylightCycle", "false");
            worldData.put("GameRules", gamerules);

            // Save World
            LevelProperties levelProperties = new LevelProperties(worldData, MinecraftClient.getInstance().getDataFixer(), 16, null);

            session.method_27426(levelProperties, worldData);
            session.save(worldName);
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start the World
        MinecraftClient.getInstance().startIntegratedServer(worldName, levelInfo);
    }

    public void deleteWorld() {
        LevelStorage levelStorage = MinecraftClient.getInstance().getLevelStorage();
        try {
            Session session = levelStorage.createSession(worldName);
            session.deleteSessionLock();
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean loadWorld() {
        if (MinecraftClient.getInstance().getLevelStorage().levelExists(worldName)) {
            MinecraftClient.getInstance().startIntegratedServer(worldName, null);
            return true;
        }
        return false;
    }

    public boolean saveExist() {
        return MinecraftClient.getInstance().getLevelStorage().levelExists(worldName);
    }
}
