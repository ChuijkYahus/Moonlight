package net.mehvahdjukaar.moonlight.api.platform.configs.forge;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigSpec;
import net.mehvahdjukaar.moonlight.api.platform.configs.ConfigType;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ConfigFileTypeHandler;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigEvent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ConfigSpecWrapper extends ConfigSpec {

    private static final Method SET_CONFIG_DATA = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);
    private static final Method SETUP_CONFIG_FILE = ObfuscationReflectionHelper.findMethod(ConfigFileTypeHandler.class,
            "setupConfigFile", ModConfig.class, Path.class, ConfigFormat.class);

    private final ModConfigSpec spec;

    private final ModConfig modConfig;
    private final ModContainer modContainer;

    private final Map<ModConfigSpec.ConfigValue<?>, Object> requireRestartValues;
    private final List<ConfigBuilderImpl.SpecialValue<?,?>> specialValues;

    public ConfigSpecWrapper(ResourceLocation name, ModConfigSpec spec, ConfigType type, boolean synced,
                             @Nullable Runnable onChange, List<ModConfigSpec.ConfigValue<?>> requireRestart,
                             List<ConfigBuilderImpl.SpecialValue<?,?>> specialValues) {
        super(name.getNamespace(), name.getNamespace() + "-" + name.getPath() + ".toml",
                FMLPaths.CONFIGDIR.get(), type, synced, onChange);
        this.spec = spec;
        this.specialValues = specialValues;

        ModConfig.Type t = this.getConfigType() == ConfigType.COMMON ? ModConfig.Type.COMMON : ModConfig.Type.CLIENT;

        this.modContainer = ModLoadingContext.get().getActiveContainer();
        this.modConfig = new ModConfig(t, spec, modContainer, this.getFileName());

        var bus = modContainer.getEventBus();
        if (onChange != null || this.isSynced() || !specialValues.isEmpty()) bus.addListener(this::onConfigChange);
        if (this.isSynced()) {

            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedIn);
            NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        }
        //for event
        ConfigSpec.addTrackedSpec(this);

        if (!requireRestart.isEmpty()) {
            loadFromFile(); //Early load if this has world reload ones as we need to get their current values. Isn't there a better way?
        }
        this.requireRestartValues = requireRestart.stream().collect(Collectors.toMap(e -> e, ModConfigSpec.ConfigValue::get));

    }

    @Override
    public Component getName() {
        return Component.literal(getFileName());
    }

    @Override
    public Path getFullPath() {
        return FMLPaths.CONFIGDIR.get().resolve(this.getFileName());
        // return modConfig.getFullPath();
    }

    @Override
    public void register() {
        ModContainer modContainer = ModLoadingContext.get().getActiveContainer();
        modContainer.addConfig(this.modConfig);
    }

    @Override
    public void loadFromFile() {
        //same stuff that forge config tracker does
        try {
            CommentedFileConfig configData = readConfig(ConfigFileTypeHandler.TOML, FMLPaths.CONFIGDIR.get(), modConfig);
            SET_CONFIG_DATA.setAccessible(true);
            SET_CONFIG_DATA.invoke(modConfig, configData);
            IConfigEvent.loading(modConfig).post();
            modConfig.save();
        } catch (Exception e) {
            throw new ConfigLoadingException(modConfig, e);
        }
    }

    //We need this, so we don't add a second file watcher. Same as handler::reader
    private CommentedFileConfig readConfig(ConfigFileTypeHandler handler, Path configBasePath, ModConfig c) {
        Path configPath = configBasePath.resolve(c.getFileName());
        CommentedFileConfig configData = CommentedFileConfig.builder(configPath).sync().
                preserveInsertionOrder().
                autosave().
                onFileNotFound((newfile, configFormat) -> {
                    try {
                        return (Boolean) SETUP_CONFIG_FILE.invoke(handler, c, newfile, configFormat);
                    } catch (Exception e) {
                        throw new ConfigLoadingException(c, e);
                    }
                }).
                writingMode(WritingMode.REPLACE).
                build();
        configData.load();
        return configData;
    }

    private static class ConfigLoadingException extends RuntimeException {
        public ConfigLoadingException(ModConfig config, Exception cause) {
            super("Failed loading config file " + config.getFileName() + " of type " + config.getType() + " for modid " + config.getModId() + ". Try deleting it", cause);
        }
    }

    public ModConfigSpec getSpec() {
        return spec;
    }

    @Nullable
    public ModConfig getModConfig() {
        return modConfig;
    }

    public ModConfig.Type getModConfigType() {
        return this.getConfigType() == ConfigType.CLIENT ? ModConfig.Type.CLIENT : ModConfig.Type.COMMON;
    }

    @Override
    public boolean isLoaded() {
        return spec.isLoaded();
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen makeScreen(Screen parent, @Nullable ResourceLocation background) {
        var container = ModList.get().getModContainerById(this.getModId());
        if (container.isPresent()) {
            var factory = container.get().getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class);
            if (factory.isPresent()) return factory.get().screenFunction().apply(Minecraft.getInstance(), parent);
        }
        return null;
    }

    @Override
    public boolean hasConfigScreen() {
        return ModList.get().getModContainerById(this.getModId())
                .map(container -> container.getCustomExtension(ConfigScreenHandler.ConfigScreenFactory.class)
                        .isPresent()).orElse(false);
    }

    @EventCalled
    private void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            //send this configuration to connected clients
            syncConfigsToPlayer(serverPlayer);
        }
    }

    @EventCalled
    protected void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) {
            onRefresh();
        }
    }

    @EventCalled
    protected void onConfigChange(ModConfigEvent event) {
        if (event.getConfig().getSpec() == this.getSpec()) {
            //send this configuration to connected clients if on server
            if (this.isSynced() && PlatHelper.getPhysicalSide().isServer()) sendSyncedConfigsToAllPlayers();
            onRefresh();
            specialValues.forEach(ConfigBuilderImpl.SpecialValue::clearCache);
        }
    }

    @Override
    public void loadFromBytes(InputStream stream) {
        try { //this should work the same as below and internaly calls refresh
            var b = stream.readAllBytes();
            this.modConfig.acceptSyncedConfig(b);
        } catch (Exception e) {
            Moonlight.LOGGER.warn("Failed to sync config file {}:", this.getFileName(), e);
        }

        //using this isntead so we dont fire the config changes event otherwise this will loop
        //this.getSpec().setConfig(TomlFormat.instance().createParser().parse(stream));
        //this.onRefresh();
    }


    public boolean requiresGameRestart(ModConfigSpec.ConfigValue<?> value) {
        var v = requireRestartValues.get(value);
        if (v == null) return false;
        else return v != value.get();
    }


}
