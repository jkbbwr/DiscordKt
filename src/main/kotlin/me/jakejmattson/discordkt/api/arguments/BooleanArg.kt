package me.jakejmattson.discordkt.api.arguments

import me.jakejmattson.discordkt.api.commands.CommandEvent
import me.jakejmattson.discordkt.api.dsl.internalLocale
import me.jakejmattson.discordkt.api.locale.inject

/**
 * Accepts either of two values. Defaults to true/false.
 *
 * @param truthValue The string value that results in true.
 * @param falseValue The string value that results in false.
 */
public open class BooleanArg(override val name: String = "Boolean",
                             private val truthValue: String = "true",
                             private val falseValue: String = "false",
                             override val description: String = internalLocale.booleanArgDescription.inject(truthValue, falseValue)) : Argument<Boolean> {
    /**
     * Accepts either true or false.
     */
    public companion object : BooleanArg()

    init {
        require(truthValue.isNotEmpty() && falseValue.isNotEmpty()) { "Custom BooleanArg ($name) options cannot be empty!" }
        require(!truthValue.equals(falseValue, ignoreCase = true)) { "Custom BooleanArg ($name) options cannot be the same!" }
    }

    override suspend fun convert(arg: String, args: List<String>, event: CommandEvent<*>): ArgumentResult<Boolean> {
        return when (arg.lowercase()) {
            truthValue.lowercase() -> Success(true)
            falseValue.lowercase() -> Success(false)
            else -> Error(internalLocale.invalidBooleanArg.inject(truthValue, falseValue))
        }
    }

    override suspend fun generateExamples(event: CommandEvent<*>): List<String> = listOf(truthValue, falseValue)
}