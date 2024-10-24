package at.hannibal2.skyhanni.config.commands

import at.hannibal2.skyhanni.api.event.SkyHanniEvent
import at.hannibal2.skyhanni.config.commands.Commands.commands
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback


object CommandRegistrationEvent : SkyHanniEvent() {
    fun register(name: String, block: CommandBuilder.() -> Unit) {
        val info = CommandBuilder(name).apply(block)
        if (commands.any { it.name == name }) {
            error("The command '$name is already registered!'")
        }
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, registryAccess ->
            dispatcher.register(
                literal(name).executes { context ->
                    val args = ""
                    info.toSimpleCommand().processCommand(args.split(" ").toTypedArray())
                    1
                },
            )
        }

        commands.add(info)
    }
}
