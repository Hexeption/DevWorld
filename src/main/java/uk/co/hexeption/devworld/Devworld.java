package uk.co.hexeption.devworld;

import static net.minecraft.world.gen.GeneratorOptions.method_28608;

import com.google.common.collect.Maps;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.util.Util;
import net.minecraft.util.registry.RegistryTracker;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.chunk.StructuresConfig;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.level.storage.SaveVersionInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Devworld implements ModInitializer {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Devworld INSTANCE;

    private String worldName = "DevWorld";

    @Override
    public void onInitialize() {
        INSTANCE = new Devworld();
    }

    public void createWorld() {

        MinecraftClient.getInstance().openScreen(null);

        GeneratorOptions DEVWORLD_GENERATOR = new GeneratorOptions(0L, false, false, method_28608(DimensionType.method_28517(0L), new FlatChunkGenerator(getDevWorldGeneratorConfig())));

        GameRules gameRules = new GameRules();
        gameRules.get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
        gameRules.get(GameRules.DO_WEATHER_CYCLE).set(false, null);
        gameRules.get(GameRules.field_19390).set(false, null);

        LevelInfo levelInfo = new LevelInfo(worldName, GameMode.CREATIVE, false, Difficulty.NORMAL, false, gameRules, DataPackSettings.SAFE_MODE);

        File gameDir = MinecraftClient.getInstance().runDirectory;
        LevelStorage levelStorage = new LevelStorage(gameDir.toPath().resolve("saves"), gameDir.toPath().resolve("backups"), null);
        LevelProperties levelProperties = null;
        try {
            Session session = levelStorage.createSession(worldName);

            CompoundTag worldData = new CompoundTag();

            worldData.putInt("Difficulty", 2);

            // World Generator
            worldData.putString("generatorName", "flat");
            DataResult tagDataResult = GeneratorOptions.CODEC.encodeStart(NbtOps.INSTANCE, DEVWORLD_GENERATOR);
            tagDataResult.resultOrPartial(Util.method_29188("WorldGenSettings: ", LOGGER::error)).ifPresent((tag) -> worldData.put("WorldGenSettings", (Tag) tag));

            // Cheat Mode
            worldData.putInt("GameType", GameMode.CREATIVE.getId());
            worldData.putBoolean("allowCommands", true);

            // World Initialized
            worldData.putBoolean("initialized", true);

            // Set Day
            worldData.putLong("Time", 6000);
            worldData.putLong("DayTime", 6000);

            worldData.put("GameRules", gameRules.toNbt());

            Dynamic dynamic = new Dynamic(NbtOps.INSTANCE, worldData);
            SaveVersionInfo lv = SaveVersionInfo.fromDynamic(dynamic);

            levelInfo = LevelInfo.method_28383(dynamic, DataPackSettings.SAFE_MODE); // Sets Cheat mode enabled

            levelProperties = new LevelProperties(levelInfo, DEVWORLD_GENERATOR, Lifecycle.stable())
                .method_29029(dynamic, MinecraftClient.getInstance().getDataFixer(), 16, null, levelInfo, lv, DEVWORLD_GENERATOR, Lifecycle.stable());

            // Spawn Location
            levelProperties.setSpawnX(0);
            levelProperties.setSpawnY(55);
            levelProperties.setSpawnZ(0);

            session.method_27426(RegistryTracker.create(), levelProperties, worldData);
            session.save(worldName);
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start the World
        MinecraftClient.getInstance().method_29607(worldName, levelProperties.getLevelInfo(), RegistryTracker.create(), DEVWORLD_GENERATOR);
    }

    private FlatChunkGeneratorConfig getDevWorldGeneratorConfig() {
        FlatChunkGeneratorConfig flatChunkGeneratorConfig = new FlatChunkGeneratorConfig(new StructuresConfig(Optional.of(StructuresConfig.DEFAULT_STRONGHOLD), Maps.newHashMap()));
        flatChunkGeneratorConfig.setBiome(Biomes.PLAINS);
        flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(1, Blocks.BEDROCK));
        flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(3, Blocks.STONE));
        flatChunkGeneratorConfig.getLayers().add(new FlatChunkGeneratorLayer(52, Blocks.SANDSTONE));
        flatChunkGeneratorConfig.updateLayerBlocks();
        return flatChunkGeneratorConfig;
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
            MinecraftClient.getInstance().startIntegratedServer(worldName);
            return true;
        }
        return false;
    }

    public boolean saveExist() {
        return MinecraftClient.getInstance().getLevelStorage().levelExists(worldName);
    }
}
