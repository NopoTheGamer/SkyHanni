package at.hannibal2.skyhanni


import at.hannibal2.skyhanni.api.event.HandleEvent
import at.hannibal2.skyhanni.api.event.SkyHanniEvents
import at.hannibal2.skyhanni.skyhannimodule.LoadedModules
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.events.WorldChangeEvent
//#if FORGE
import at.hannibal2.skyhanni.config.ConfigFileType
import at.hannibal2.skyhanni.config.ConfigManager
import at.hannibal2.skyhanni.config.Features
import at.hannibal2.skyhanni.config.SackData
import at.hannibal2.skyhanni.config.commands.CommandRegistrationEvent
import at.hannibal2.skyhanni.data.OtherInventoryData
import at.hannibal2.skyhanni.data.jsonobjects.local.FriendsJson
import at.hannibal2.skyhanni.data.jsonobjects.local.JacobContestsJson
import at.hannibal2.skyhanni.data.jsonobjects.local.KnownFeaturesJson
import at.hannibal2.skyhanni.data.jsonobjects.local.VisualWordsJson
import at.hannibal2.skyhanni.data.repo.RepoManager
import at.hannibal2.skyhanni.events.utils.PreInitFinishedEvent
import at.hannibal2.skyhanni.features.nether.reputationhelper.CrimsonIsleReputationHelper
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.test.hotswap.HotswapSupport
import at.hannibal2.skyhanni.utils.MinecraftConsoleFilter.Companion.initLogging
import at.hannibal2.skyhanni.utils.NEUVersionCheck.checkIfNeuIsLoaded
//#endif
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
//#if FABRIC
//$$ import net.fabricmc.api.ModInitializer;
//#endif
//#if FORGE
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(
    modid = SkyHanniMod.MODID,
    clientSideOnly = true,
    useMetadata = true,
    guiFactory = "at.hannibal2.skyhanni.config.ConfigGuiForgeInterop",
    version = "@MOD_VERSION@",
)
class SkyHanniMod {
//#elseif FABRIC
//$$ class SkyHanniMod : ModInitializer {
//#endif

    //#if FORGE
    @Mod.EventHandler
    fun onPreInitForge(event: FMLPreInitializationEvent?) {
        onPreInit()
    }

    @Mod.EventHandler
    fun onInitForge(event: FMLInitializationEvent?) {
        onInit()
    }

    //#elseif FABRIC
    //$$ override fun onInitialize() {
    //$$     onPreInit()
    //$$     onInit()
    //$$     println("SkyHanni initialized")
    //$$ }
    //#endif

    fun onPreInit() {
        //#if FORGE
        checkIfNeuIsLoaded()

        HotswapSupport.load()
        //#endif

        loadModule(this)
        LoadedModules.modules.forEach { loadModule(it) }
        SkyHanniEvents.init(modules)
        //#if FORGE

        loadModule(CrimsonIsleReputationHelper(this))


        CommandRegistrationEvent.post()

        PreInitFinishedEvent.post()
        //#endif
    }

    fun onInit() {
        //#if FORGE
        configManager = ConfigManager()
        configManager.firstLoad()
        initLogging()
        Runtime.getRuntime().addShutdownHook(
            Thread { configManager.saveConfig(ConfigFileType.FEATURES, "shutdown-hook") },
        )
        repo = RepoManager(ConfigManager.configDirectory)
        loadModule(repo)
        try {
            repo.loadRepoInformation()
        } catch (e: Exception) {
            Exception("Error reading repo data", e).printStackTrace()
        }
        loadedClasses.clear()
        //#endif
    }

    private val loadedClasses = mutableSetOf<String>()

    fun loadModule(obj: Any) {
        if (!loadedClasses.add(obj.javaClass.name)) throw IllegalStateException("Module ${obj.javaClass.name} is already loaded")
        modules.add(obj)
        //#if FORGE
        MinecraftForge.EVENT_BUS.register(obj)
        //#endif
    }

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        //println("Skyhanni Tick Event!!")
        //#if FORGE
        screenToOpen?.let {
            screenTicks++
            if (screenTicks == 5) {
                Minecraft.getMinecraft().thePlayer?.closeScreen()
                OtherInventoryData.close()
                Minecraft.getMinecraft().displayGuiScreen(it)
                screenTicks = 0
                screenToOpen = null
            }
        }
        //#endif
    }

    @HandleEvent
    fun onWorld(event: WorldChangeEvent) {
        println("World Change Event!!")
    }

    companion object {

        const val MODID = "skyhanni"

        @JvmStatic
        val version: String get() = "@MOD_VERSION@"

        val modules: MutableList<Any> = ArrayList()
        //#if FORGE
        @JvmField
        var feature: Features = Features()
        lateinit var sackData: SackData
        lateinit var friendsData: FriendsJson
        lateinit var knownFeaturesData: KnownFeaturesJson
        lateinit var jacobContestsData: JacobContestsJson

        lateinit var visualWordsData: VisualWordsJson
        lateinit var repo: RepoManager
        lateinit var configManager: ConfigManager
        val logger: Logger = LogManager.getLogger("SkyHanni")

        fun getLogger(name: String): Logger {
            return LogManager.getLogger("SkyHanni.$name")
        }
        private val globalJob: Job = Job(null)
        val coroutineScope = CoroutineScope(
            CoroutineName("SkyHanni") + SupervisorJob(globalJob),
        )
        var screenToOpen: GuiScreen? = null
        private var screenTicks = 0
        fun consoleLog(message: String) {
            logger.log(Level.INFO, message)
        }

        fun launchCoroutine(function: suspend () -> Unit) {
            coroutineScope.launch {
                try {
                    function()
                } catch (ex: Exception) {
                    ErrorManager.logErrorWithData(ex, "Asynchronous exception caught")
                }
            }
        }
        //#endif
    }
}
