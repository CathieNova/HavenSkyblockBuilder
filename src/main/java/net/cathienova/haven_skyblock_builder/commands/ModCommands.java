package net.cathienova.haven_skyblock_builder.commands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.SkyblockUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("havensb");
        command.then(Commands.literal("spawn")
                .executes(SkyblockUtils::goSpawn));

        // Island commands
        LiteralArgumentBuilder<CommandSourceStack> island = Commands.literal("island");
        island.then(Commands.literal("create")
                .executes(context ->
                {
                    context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.island.create.missing_island"));
                    return 0;
                })
                .then(Commands.argument("template", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestIslandTemplates)
                        .executes(context ->
                        {
                            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.island.create.missing_name"));
                            return 0;
                        })
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(SkyblockUtils::createTeam))));
        island.then(Commands.literal("home").executes(SkyblockUtils::goHome));
        island.then(Commands.literal("info").executes(SkyblockUtils::islandInfo));
        command.then(island);

        // Team commands
        LiteralArgumentBuilder<CommandSourceStack> team = Commands.literal("team");
        team.then(Commands.literal("list").executes(SkyblockUtils::listTeams));
        team.then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(CommandSuggestions::suggestOnlinePlayers)
                        .executes(SkyblockUtils::invitePlayer)));
        team.then(Commands.literal("accept").executes(SkyblockUtils::acceptInvite));
        team.then(Commands.literal("deny").executes(SkyblockUtils::denyInvite));
        team.then(Commands.literal("leave")
                .executes(SkyblockUtils::leaveTeam));
        team.then(Commands.literal("deport")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(CommandSuggestions::suggestOnlinePlayers)
                        .executes(SkyblockUtils::deportPlayer)));
        team.then(Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.greedyString())
                        .suggests(CommandSuggestions::suggestTeams)
                        .executes(SkyblockUtils::visitIsland)));
        command.then(team);

        // Leader commands
        LiteralArgumentBuilder<CommandSourceStack> leader = Commands.literal("leader");
        leader.then(Commands.literal("sethome").executes(SkyblockUtils::setHome));
        leader.then(Commands.literal("disband")
                .executes(SkyblockUtils::disbandTeam));
        leader.then(Commands.literal("kick")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(CommandSuggestions::suggestTeamMembers)
                        .executes(SkyblockUtils::kickPlayer)));
        leader.then(Commands.literal("transfer")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(CommandSuggestions::suggestTeamMembers)
                        .executes(SkyblockUtils::transferLeadership)));
        leader.then(Commands.literal("allowvisit")
                .then(Commands.argument("allow", BoolArgumentType.bool())
                        .suggests(CommandSuggestions::suggestStates)
                        .executes(SkyblockUtils::setAllowVisit)));
        leader.then(Commands.literal(("changename"))
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(SkyblockUtils::changeTeamName)));
        command.then(leader);

        // Admin commands
        LiteralArgumentBuilder<CommandSourceStack> admin = Commands.literal("admin");
        admin.requires(source -> source.hasPermission(2));
        admin.then(Commands.literal("reload")
                .executes(ModCommands::reloadConfig));
        admin.then(Commands.literal("listteams")
                .executes(SkyblockUtils::adminListTeams));
        admin.then(Commands.literal("addmember")
                .then(Commands.argument("team", StringArgumentType.greedyString())
                        .suggests(CommandSuggestions::suggestTeams)
                        .then(Commands.argument("player", EntityArgument.player())
                                .suggests(CommandSuggestions::suggestPlayers)
                                .executes(SkyblockUtils::addMember))));
        admin.then(Commands.literal("removemember")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestTeams)
                        .then(Commands.argument("player", EntityArgument.player())
                                .suggests(CommandSuggestions::suggestTeamMembers)
                                .executes(SkyblockUtils::removeMember))));
        admin.then(Commands.literal("removeteam")
                .then(Commands.argument("team", StringArgumentType.greedyString())
                        .suggests(CommandSuggestions::suggestTeams)
                        .executes(SkyblockUtils::adminRemoveTeam)));
        admin.then(Commands.literal("changename")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestTeams)
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(SkyblockUtils::adminChangeTeamName))));
        admin.then(Commands.literal("generatejsons")
                .then(Commands.literal("structures")
                        .executes(ModCommands::generateStructureList))
                .then(Commands.literal("biomes")
                        .executes(ModCommands::generateBiomeList)));
        command.then(admin);

        dispatcher.register(command);
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        TeamManager.loadAllTeams(context.getSource().getServer());
        Player player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.reload"));
        return -1;
    }

    private static int generateStructureList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        List<String> structureList = new ArrayList<>();
        server.registryAccess().registryOrThrow(Registries.STRUCTURE).entrySet().forEach(entry -> {
            ResourceLocation key = entry.getKey().location();
            structureList.add(key.toString());
        });

        structureList.sort(String::compareToIgnoreCase);

        File outputDir = new File(server.getServerDirectory().toFile(), "config/HavenSkyblockBuilder/generatedjsons");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            source.sendFailure(Component.translatable("Failed to create directory: " + outputDir.getAbsolutePath()));
            return 0;
        }

        File outputFile = new File(outputDir, "structures_list.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(structureList, writer);
            source.sendSuccess(() -> Component.translatable("Generated structure list to: " + outputFile.getAbsolutePath()), true);
        } catch (IOException e) {
            source.sendFailure(Component.translatable("Failed to write structure list: " + e.getMessage()));
        }

        return 1;
    }

    private static int generateBiomeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        List<String> biomeList = new ArrayList<>();
        server.registryAccess().registryOrThrow(Registries.BIOME).entrySet().forEach(entry -> {
            ResourceLocation key = entry.getKey().location();
            biomeList.add(key.toString());
        });

        biomeList.sort(String::compareToIgnoreCase);

        File outputDir = new File(server.getServerDirectory().toFile(), "config/HavenSkyblockBuilder/generatedjsons");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            source.sendFailure(Component.translatable("Failed to create directory: " + outputDir.getAbsolutePath()));
            return 0;
        }

        File outputFile = new File(outputDir, "biomes_list.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(outputFile)) {
            gson.toJson(biomeList, writer);
            source.sendSuccess(() -> Component.translatable("Generated biome list to: " + outputFile.getAbsolutePath()), true);
        } catch (IOException e) {
            source.sendFailure(Component.translatable("Failed to write biome list: " + e.getMessage()));
        }

        return 1;
    }

}
