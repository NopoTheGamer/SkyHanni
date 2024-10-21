package at.hannibal2.skyhanni.features.misc.update

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.api.event.HandleEvent
//#if FORGE
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.events.ConfigLoadEvent
import net.minecraftforge.common.MinecraftForge
//#endif
import at.hannibal2.skyhanni.config.features.About.UpdateStream
import at.hannibal2.skyhanni.events.SkyHanniTickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.APIUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.onToggle
import at.hannibal2.skyhanni.utils.DelayedRun
import com.google.gson.JsonElement
import io.github.notenoughupdates.moulconfig.observer.Property
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor
import moe.nea.libautoupdate.CurrentVersion
import moe.nea.libautoupdate.PotentialUpdate
import moe.nea.libautoupdate.UpdateContext
import moe.nea.libautoupdate.UpdateSource
import moe.nea.libautoupdate.UpdateTarget
import moe.nea.libautoupdate.UpdateUtils
import net.minecraft.client.Minecraft
import java.util.concurrent.CompletableFuture
import javax.net.ssl.HttpsURLConnection

@SkyHanniModule
object UpdateManager {

    private val logger = SkyHanniMod.logger
    private var _activePromise: CompletableFuture<*>? = null
    private var activePromise: CompletableFuture<*>?
        get() = _activePromise
        set(value) {
            _activePromise?.cancel(true)
            _activePromise = value
        }

    var updateState: UpdateState = UpdateState.NONE
        private set

    fun getNextVersion(): String? {
        return potentialUpdate?.update?.versionNumber?.asString
    }

    //#if FORGE
    @HandleEvent
    fun onConfigLoad(event: ConfigLoadEvent) {
        SkyHanniMod.feature.about.updateStream.onToggle {
            reset()
        }
    }
    //#endif

    @HandleEvent
    fun onTick(event: SkyHanniTickEvent) {
        Minecraft.getMinecraft().thePlayer ?: return
        //#if FORGE
        MinecraftForge.EVENT_BUS.unregister(this)
        if (config.autoUpdates || config.fullAutoUpdates)
            checkUpdate()
        //#endif
    }

    fun injectConfigProcessor(processor: MoulConfigProcessor<*>) {
        //#if FORGE
        processor.registerConfigEditor(ConfigVersionDisplay::class.java) { option, _ ->
            GuiOptionEditorUpdateCheck(option)
        }
        //#endif
    }

    fun isCurrentlyBeta(): Boolean {
        return SkyHanniMod.version.contains("beta", ignoreCase = true)
    }

    private val config get() = SkyHanniMod.feature.about

    fun reset() {
        updateState = UpdateState.NONE
        _activePromise = null
        potentialUpdate = null
        logger.warn("Reset update state")
    }

    fun checkUpdate(forceDownload: Boolean = false, forcedUpdateStream: UpdateStream = config.updateStream.get()) {
        var updateStream = forcedUpdateStream
        if (updateState != UpdateState.NONE) {
            logger.warn("Trying to perform update check while another update is already in progress")
            return
        }
        logger.warn("Starting update check")
        val currentStream = config.updateStream.get()
        if (currentStream != UpdateStream.BETA && (updateStream == UpdateStream.BETA || isCurrentlyBeta())) {
            config.updateStream = Property.of(UpdateStream.BETA)
            updateStream = UpdateStream.BETA
        }
        activePromise = context.checkUpdate(updateStream.stream)
            .thenAcceptAsync({
                logger.warn("Update check completed")
                if (updateState != UpdateState.NONE) {
                    logger.warn("This appears to be the second update check. Ignoring this one")
                    return@thenAcceptAsync
                }
                potentialUpdate = it
                if (it.isUpdateAvailable) {
                    updateState = UpdateState.AVAILABLE
                    if (config.fullAutoUpdates || forceDownload) {
                        //#if FORGE
                        ChatUtils.chat("§aSkyHanni found a new update: ${it.update.versionName}, starting to download now.")
                        //#endif
                        queueUpdate()
                    } else if (config.autoUpdates) {
                        //#if FORGE
                        ChatUtils.chatAndOpenConfig(
                            "§aSkyHanni found a new update: ${it.update.versionName}. " +
                                "Check §b/sh download update §afor more info.",
                            config::autoUpdates
                        )
                        //#endif
                    }
                } else if (forceDownload) {
                    //#if FORGE
                    ChatUtils.chat("§aSkyHanni didn't find a new update.")
                    //#endif
                }
            }, DelayedRun.onThread)
    }

    fun queueUpdate() {
        if (updateState != UpdateState.AVAILABLE) {
            logger.warn("Trying to enqueue an update while another one is already downloaded or none is present")
        }
        updateState = UpdateState.QUEUED
        activePromise = CompletableFuture.supplyAsync {
            logger.warn("Update download started")
            potentialUpdate!!.prepareUpdate()
        }.thenAcceptAsync({
            logger.warn("Update download completed, setting exit hook")
            updateState = UpdateState.DOWNLOADED
            potentialUpdate!!.executePreparedUpdate()
            //#if FORGE
            ChatUtils.chat("Download of update complete. ")
            ChatUtils.chat("§aThe update will be installed after your next restart.")
            //#endif
        }, DelayedRun.onThread)
    }

    private val context = UpdateContext(
        UpdateSource.githubUpdateSource("hannibal002", "SkyHanni"),
        UpdateTarget.deleteAndSaveInTheSameFolder(UpdateManager::class.java),
        object : CurrentVersion {
            val normalDelegate = CurrentVersion.ofTag(SkyHanniMod.version)
            override fun display(): String {
                //#if FORGE
                if (SkyHanniMod.feature.dev.debug.alwaysOutdated)
                    return "Force Outdated"
                //#endif
                return normalDelegate.display()
            }

            override fun isOlderThan(element: JsonElement): Boolean {
                //#if FORGE
                if (SkyHanniMod.feature.dev.debug.alwaysOutdated)
                    return true
                //#endif
                return normalDelegate.isOlderThan(element)
            }

            override fun toString(): String {
                return "ForceOutdateDelegate($normalDelegate)"
            }
        },
        SkyHanniMod.MODID,
    )

    init {
        context.cleanup()
        UpdateUtils.patchConnection {
            if (it is HttpsURLConnection) {
                APIUtils.patchHttpsRequest(it)
            }
        }
    }

    enum class UpdateState {
        AVAILABLE,
        QUEUED,
        DOWNLOADED,
        NONE
    }

    private var potentialUpdate: PotentialUpdate? = null

    fun updateCommand(args: Array<String>) {
        val currentStream = SkyHanniMod.feature.about.updateStream.get()
        val arg = args.firstOrNull() ?: "current"
        val updateStream = when {
            arg.equals("(?i)(?:full|release)s?".toRegex()) -> UpdateStream.RELEASES
            arg.equals("(?i)(?:beta|latest)s?".toRegex()) -> UpdateStream.BETA
            else -> currentStream
        }

        val switchingToBeta = updateStream == UpdateStream.BETA && (currentStream != UpdateStream.BETA || !UpdateManager.isCurrentlyBeta())
        if (switchingToBeta) {
            //#if FORGE
            ChatUtils.clickableChat(
                "Are you sure you want to switch to beta? These versions may be less stable.",
                onClick = {
                    UpdateManager.checkUpdate(true, updateStream)
                },
                "§eClick to confirm!",
                oneTimeClick = true,
            )
            //#endif
        } else {
            UpdateManager.checkUpdate(true, updateStream)
        }
    }
}
