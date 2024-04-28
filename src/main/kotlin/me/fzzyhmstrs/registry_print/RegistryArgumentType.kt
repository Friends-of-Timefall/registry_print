package me.fzzyhmstrs.registry_print

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class RegistryArgumentType: ArgumentType<Identifier> {

    private val invalidIdException = DynamicCommandExceptionType { id -> Text.of("Invalid registry id: [$id]")}

    private val ids by lazy {
        val list: MutableList<Identifier> = mutableListOf()
        val lookup = BuiltinRegistries.createWrapperLookup()
        for (f in RegistryKeys::class.java.declaredFields){
            val key = f.get(null) as? RegistryKey<Registry<*>> ?: continue
            lookup.getOptionalWrapper(key).ifPresent { list.add(it.registryKey.value) }
        }
        list
        //DynamicRegistryManager.of(Registries.REGISTRIES).streamAllRegistries().map { it.key.value }.toList()
    }

    override fun parse(reader: StringReader): Identifier {
        val i = reader.cursor
        val identifier = Identifier.fromCommandInput(reader)
        return if (ids.contains(identifier)){
            identifier
        } else {
            reader.cursor = i
            throw invalidIdException.create(identifier)
        }
    }

    override fun <S : Any?> listSuggestions(
        context: CommandContext<S>?,
        builder: SuggestionsBuilder?
    ): CompletableFuture<Suggestions>? {
        return CommandSource.suggestIdentifiers(ids,builder)
    }
}