package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.Command
import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.commands.get
import me.jakejmattson.discordkt.api.dsl.internalLocale

/**
 * Accepts a DiscordKt command by name.
 */
public open class CommandArg(override val name: String = "Command",
                      override val description: String = internalLocale.commandArgDescription) : Argument<Command> {
    /**
     * Accepts a DiscordKt command by name.
     */
    public companion object : CommandArg()

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Command> {
        val command = event.discord.commands[arg] ?: return Error(internalLocale.notFound)
        return Success(command)
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = event.discord.commands.mapNotNull { it.names.firstOrNull() }
    override fun formatData(data: Command): String = data.names.first()
}