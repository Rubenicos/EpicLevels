package com.songoda.epiclevels;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.commands.CommandManager;
import com.songoda.core.compatibility.MajorServerVersion;
import com.songoda.core.configuration.Config;
import com.songoda.core.database.DatabaseConnector;
import com.songoda.core.gui.GuiManager;
import com.songoda.core.hooks.EconomyManager;
import com.songoda.epiclevels.boost.BoostManager;
import com.songoda.epiclevels.commands.CommandAddExp;
import com.songoda.epiclevels.commands.CommandBoost;
import com.songoda.epiclevels.commands.CommandEpicLevels;
import com.songoda.epiclevels.commands.CommandGlobalBoost;
import com.songoda.epiclevels.commands.CommandHelp;
import com.songoda.epiclevels.commands.CommandReload;
import com.songoda.epiclevels.commands.CommandRemoveBoost;
import com.songoda.epiclevels.commands.CommandRemoveGlobalBoost;
import com.songoda.epiclevels.commands.CommandReset;
import com.songoda.epiclevels.commands.CommandSettings;
import com.songoda.epiclevels.commands.CommandShow;
import com.songoda.epiclevels.commands.CommandTakeExp;
import com.songoda.epiclevels.database.DataHelper;
import com.songoda.epiclevels.database.migrations._1_InitialMigration;
import com.songoda.epiclevels.database.migrations._2_MakeBoostTableAutoIncrementMigration;
import com.songoda.epiclevels.killstreaks.KillstreakManager;
import com.songoda.epiclevels.levels.LevelManager;
import com.songoda.epiclevels.listeners.DeathListeners;
import com.songoda.epiclevels.listeners.LoginListeners;
import com.songoda.epiclevels.managers.EntityManager;
import com.songoda.epiclevels.placeholder.PlaceholderManager;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.players.PlayerManager;
import com.songoda.epiclevels.settings.Settings;
import com.songoda.epiclevels.tasks.BoostTask;
import com.songoda.epiclevels.tasks.ModifierTask;
import com.songoda.third_party.com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

import java.util.Arrays;
import java.util.List;

public class EpicLevels extends SongodaPlugin {
    private final GuiManager guiManager = new GuiManager(this);
    private PlayerManager playerManager;
    private CommandManager commandManager;
    private LevelManager levelManager;
    private KillstreakManager killstreakManager;
    private EntityManager entityManager;
    private BoostManager boostManager;

    private DataHelper dataHelper;

    /**
     * @deprecated Use {@link #getPlugin(Class)} instead
     */
    @Deprecated
    public static EpicLevels getInstance() {
        return getPlugin(EpicLevels.class);
    }

    @Override
    public void onPluginLoad() {
    }

    @Override
    public void onPluginDisable() {
        this.dataHelper.getUpdater().onDisable();
        this.dataHelper.bulkUpdatePlayers(this.playerManager.getPlayers());
        this.dataHelper.bulkUpdateBoosts(this.boostManager.getBoosts().values());

        if (this.boostManager.getGlobalBoost() != null) {
            this.dataHelper.updateBoost(this.boostManager.getGlobalBoost());
        }
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 44, XMaterial.NETHER_STAR);

        // Load Economy
        EconomyManager.load();

        // Setup Config
        Settings.setupConfig();
        this.setLocale(Settings.LANGUGE_MODE.getString(), false);

        PluginManager pluginManager = Bukkit.getPluginManager();

        // Set Economy preference
        EconomyManager.getManager().setPreferredHook(Settings.ECONOMY.getString());

        // Listener Registration
        this.guiManager.init();
        pluginManager.registerEvents(new DeathListeners(this), this);
        pluginManager.registerEvents(new LoginListeners(this), this);

        // Load Commands
        this.commandManager = new CommandManager(this);
        this.commandManager.addCommand(new CommandEpicLevels(this.guiManager))
                .addSubCommands(
                        new CommandAddExp(this),
                        new CommandBoost(this),
                        new CommandGlobalBoost(this),
                        new CommandHelp(this),
                        new CommandReload(this),
                        new CommandRemoveBoost(this),
                        new CommandRemoveGlobalBoost(this),
                        new CommandReset(this),
                        new CommandSettings(this.guiManager, this),
                        new CommandShow(this),
                        new CommandTakeExp(this)
                );

        // Load Managers
        this.playerManager = new PlayerManager(this);
        this.levelManager = new LevelManager(this);
        this.killstreakManager = new KillstreakManager(this);
        this.entityManager = new EntityManager(this);
        this.boostManager = new BoostManager(this);

        // Loading levels
        this.levelManager.load(this);

        // Loading kill streaks
        this.killstreakManager.load(this);

        // Start Tasks
        if (MajorServerVersion.isServerVersionAtLeast(MajorServerVersion.V1_9)) {
            ModifierTask.startTask(this);
        }
        BoostTask.startTask(this);

        // Register Placeholders
        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderManager(this).register();
        }
    }

    @Override
    public synchronized void onDataLoad() {
        if (this.dataHelper != null) {
            return;
        }
        initDatabase(new _1_InitialMigration(), new _2_MakeBoostTableAutoIncrementMigration());
        this.dataHelper = new DataHelper(this);

        if (Settings.LOAD_ALL_PLAYERS.getBoolean(false)) {
            this.dataHelper.getPlayers((player) -> this.playerManager.addPlayers(player));
        }
        this.dataHelper.getBoosts((uuidBoostMap -> this.boostManager.addBoosts(uuidBoostMap)));
    }

    @Override
    public void onConfigReload() {
        this.setLocale(getConfig().getString("System.Language Mode"), true);
        this.locale.reloadMessages();

        EPlayer.reload();

        // Loading levels
        this.levelManager.load(this);
        // Loading kill streaks
        this.killstreakManager.load(this);
        // Loading entities.
        this.entityManager.reload();

        this.levelManager.load(this);
    }

    @Override
    public List<Config> getExtraConfig() {
        return Arrays.asList(this.levelManager.getLevelsConfig(), this.killstreakManager.getKillstreaksConfig());
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    public LevelManager getLevelManager() {
        return this.levelManager;
    }

    public KillstreakManager getKillstreakManager() {
        return this.killstreakManager;
    }

    public BoostManager getBoostManager() {
        return this.boostManager;
    }

    public DatabaseConnector getDatabaseConnector() {
        return super.getDataManager().getDatabaseConnector();
    }

    public DataHelper getDataHelper() {
        if (this.dataHelper == null) {
            onDataLoad();
        }
        return this.dataHelper;
    }

    public GuiManager getGuiManager() {
        return this.guiManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }
}
