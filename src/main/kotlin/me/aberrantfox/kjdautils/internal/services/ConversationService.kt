package me.aberrantfox.kjdautils.internal.services

import kotlinx.coroutines.runBlocking
import me.aberrantfox.kjdautils.api.dsl.*
import me.aberrantfox.kjdautils.discord.Discord
import me.aberrantfox.kjdautils.extensions.stdlib.pluralize
import me.aberrantfox.kjdautils.internal.utils.InternalLogger
import net.dv8tion.jda.api.entities.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import java.lang.reflect.Method

class ConversationService(val discord: Discord) {
    @PublishedApi
    internal val availableConversations = mutableMapOf<Class<out Conversation>, Pair<Conversation, Method>>()

    @PublishedApi
    internal val activeConversations = mutableMapOf<String, ConversationBuilder>()

    private fun getConversation(user: User) = activeConversations[user.id]
    fun hasConversation(user: User) = activeConversations[user.id] != null

    internal fun registerConversations(path: String) {
        Reflections(path)
            .getSubTypesOf(Conversation::class.java)
            .forEach {
                val startFunctions = Reflections(it, MethodAnnotationsScanner()).getMethodsAnnotatedWith(Conversation.Start::class.java)
                val conversationName = it.name.substringAfterLast(".")

                val starter = startFunctions.firstOrNull()
                val starterName = starter?.name ?: ""

                when (startFunctions.size) {
                    0 -> {
                        InternalLogger.error("$conversationName has no method annotated with @Start. It cannot be registered.")
                        return@forEach
                    }
                    1 -> Unit
                    else -> {
                        InternalLogger.error("$conversationName has multiple methods annotated with @Start. Defaulting to first ($starterName).")
                    }
                }

                if (starter!!.returnType != ConversationBuilder::class.java) {
                    InternalLogger.error("$conversationName @Start function ($starterName) does not build a conversation. It cannot be registered.")
                    return@forEach
                }

                availableConversations[it as Class<out Conversation>] = (it.constructors.first().newInstance() as Conversation) to starter
            }

        println(availableConversations.size.pluralize("Conversation"))
    }

    inline fun <reified T : Conversation> startConversation(user: User, vararg arguments: Any): Boolean {
        if (user.isBot)
            return false

        if (hasConversation(user))
            return false

        val (instance, function) = availableConversations[T::class.java]!!
        val conversation = function.invoke(instance, *arguments) as ConversationBuilder

        activeConversations[user.id] = conversation

        val state = ConversationStateContainer(user, discord)

        conversation.start(state) {
            activeConversations.remove(user.id)
        }

        return true
    }

    internal fun handleResponse(message: Message) {
        runBlocking {
            val conversation = getConversation(message.author) ?: return@runBlocking
            conversation.acceptMessage(message)
        }
    }
}
