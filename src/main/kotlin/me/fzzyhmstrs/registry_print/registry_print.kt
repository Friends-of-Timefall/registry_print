@file:Suppress("PropertyName")

package me.fzzyhmstrs.registry_print

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.registry.BuiltinRegistries
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import kotlin.random.Random


object RegistryPrint: ModInitializer {
    const val MOD_ID = "registry_print"
    val LOGGER: Logger = LoggerFactory.getLogger("registry_print")
    override fun onInitialize() {

        ArgumentTypeRegistry.registerArgumentType(
            Identifier(MOD_ID,"registries"),
            RegistryArgumentType::class.java, ConstantArgumentSerializer.of(
                Supplier {
                    RegistryArgumentType()
                }
            )
        )

        CommandRegistrationCallback.EVENT.register { commandDispatcher, registryAccess, _ -> register(commandDispatcher, registryAccess)}

    }

    private fun register(commandDispatcher: CommandDispatcher<ServerCommandSource>, registryAccess: CommandRegistryAccess){
        commandDispatcher.register(
            CommandManager.literal("registry_print")
                .then(CommandManager.argument("registry", RegistryArgumentType())
                    .executes {context -> printRegistry(context)}
                )
        )
    }

    private fun printRegistry(context: CommandContext<ServerCommandSource>): Int{
        val lookup = BuiltinRegistries.createWrapperLookup()
        val registryKeyId = context.getArgument("registry",Identifier::class.java)
        val registryKey = RegistryKey.ofRegistry<Any>(registryKeyId)
        lookup.getOptionalWrapper(registryKey).ifPresent {
            LOGGER.error("Registry Printed: ${it.registryKey}")
            val outputs: MutableList<String> = mutableListOf()
            it.streamEntries().forEach { ref ->
                outputs.add(ref.registryKey().value.toString())
            }
            outputs.sort()
            var str = ""
            outputs.forEach { output -> str += "$output\n" }
            println(str)
        }
        return 1
    }

}