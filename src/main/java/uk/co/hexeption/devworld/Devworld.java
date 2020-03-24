package uk.co.hexeption.devworld;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelGeneratorType;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

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

        LevelInfo levelInfo = new LevelInfo(0, GameMode.CREATIVE, false, false, LevelGeneratorType.FLAT);

        FlatChunkGeneratorConfig flatChunkGeneratorConfig = FlatChunkGeneratorConfig.fromString("minecraft:bedrock,3*minecraft:stone,52*minecraft:sandstone;minecraft:desert;");
        CompoundTag worldGenTag = (CompoundTag) flatChunkGeneratorConfig.toDynamic(NbtOps.INSTANCE).getValue();

        levelInfo.setGeneratorOptions((JsonElement) Dynamic.convert(NbtOps.INSTANCE, JsonOps.INSTANCE, worldGenTag));

        File gameDir = MinecraftClient.getInstance().runDirectory;
        LevelStorage levelStorage = new LevelStorage(gameDir.toPath().resolve("saves"), gameDir.toPath().resolve("backups"), null);
        WorldSaveHandler worldSaveHandler = levelStorage.createSaveHandler(worldName, null);

        CompoundTag worldData = new CompoundTag();

        // Spawn Location
        worldData.putInt("SpawnX", 0);
        worldData.putInt("SpawnY", 60);
        worldData.putInt("SpawnZ", 0);

        // World Generator
        worldData.putString("generatorName", "flat");
        worldData.put("generatorOptions", worldGenTag);

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
        LevelProperties levelProperties = new LevelProperties(worldData, null, 14, null);
        worldSaveHandler.saveWorld(levelProperties);

        // Start the World
        MinecraftClient.getInstance().startIntegratedServer(worldName, worldName, levelInfo);

    }

    public void deleteWorld() {
        LevelStorage levelStorage = MinecraftClient.getInstance().getLevelStorage();
        levelStorage.deleteLevel(worldName);
    }

    public boolean loadWorld() {
        if (MinecraftClient.getInstance().getLevelStorage().levelExists(worldName)) {
            MinecraftClient.getInstance().startIntegratedServer(worldName, worldName, null);
            return true;
        }
        return false;
    }

    public boolean saveExist() {
        return MinecraftClient.getInstance().getLevelStorage().getLevelProperties(worldName) != null;
    }
}
