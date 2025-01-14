package net.cathienova.haven_skyblock_builder.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.cathienova.haven_skyblock_builder.util.SkyblockUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class ModCommands
{

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("havensb");

        // Island-based commands
        LiteralArgumentBuilder<CommandSourceStack> island = Commands.literal("island");
        island.then(Commands.literal("create")
                .executes(context ->
                {
                    context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.island.create.missing_island"));
                    return 0;
                })
                .then(Commands.argument("template", StringArgumentType.word())
                        .suggests(ModCommands::suggestIslandTemplates)
                        .executes(context ->
                        {
                            context.getSource().sendFailure(Component.translatable("haven_skyblock_builder.island.create.missing_name"));
                            return 0;
                        })
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(SkyblockUtils::createTeam))));
        island.then(Commands.literal("home").executes(SkyblockUtils::goHome));
        island.then(Commands.literal("sethome").executes(SkyblockUtils::setHome));
        island.then(Commands.literal("list").executes(SkyblockUtils::listTeams));
        island.then(Commands.literal("visit")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeams)
                        .executes(SkyblockUtils::visitIsland)));
        command.then(island);

        // Team-based commands
        LiteralArgumentBuilder<CommandSourceStack> team = Commands.literal("team");
        team.then(Commands.literal("invite")
                .then(Commands.argument("player", EntityArgument.player())
                        .suggests(ModCommands::suggestOnlinePlayers)
                        .executes(SkyblockUtils::invitePlayer)));
        team.then(Commands.literal("allowvisit")
                .then(Commands.argument("allow", StringArgumentType.string())
                        .suggests(ModCommands::suggestStates)
                        .executes(SkyblockUtils::setAllowVisit)));
        team.then(Commands.literal("accept").executes(SkyblockUtils::acceptInvite));
        team.then(Commands.literal("deny").executes(SkyblockUtils::denyInvite));
        team.then(Commands.literal("kick")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeamMembers)
                        .executes(SkyblockUtils::kickPlayer)));
        team.then(Commands.literal("transfer")
                .then(Commands.argument("player", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeamMembers)
                        .executes(SkyblockUtils::transferLeadership)));
        team.then(Commands.literal("leave")
                .executes(SkyblockUtils::leaveTeam));
        team.then(Commands.literal("disband")
                .executes(SkyblockUtils::disbandTeam));
        command.then(team);

        // Admin commands
        LiteralArgumentBuilder<CommandSourceStack> admin = Commands.literal("admin");
        admin.requires(source -> source.hasPermission(2));
        admin.then(Commands.literal("reload")
                .executes(ModCommands::reloadConfig));
        admin.then(Commands.literal("addmember")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeams)
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ModCommands::suggestPlayers)
                                .executes(SkyblockUtils::addMember))));
        admin.then(Commands.literal("removemember")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeams)
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(ModCommands::suggestTeamMembers)
                                .executes(SkyblockUtils::removeMember))));
        admin.then(Commands.literal("changename")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(ModCommands::suggestTeams)
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(SkyblockUtils::changeName))));
        command.then(admin);

        dispatcher.register(command);
    }

    public static CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            context.getSource().getServer().getPlayerList().getPlayers().stream()
                    .map(player -> player.getName().getString())
                    .filter(name -> name.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }

    public static CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            context.getSource().getServer().getPlayerList().getPlayers().stream()
                    .map(player -> player.getName().getString())
                    .filter(name -> name.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }

    public static CompletableFuture<Suggestions> suggestTeamMembers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException
    {
        ServerPlayer leader = context.getSource().getPlayerOrException();
        Team team = TeamManager.getTeamByPlayer(leader.getUUID());

        if (team != null)
        {
            team.getMemberNames().stream()
                    .filter(name -> name.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
        }

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestTeams(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        TeamManager.getAllTeams().stream()
                .map(Team::getName)
                .filter(name -> name.startsWith(builder.getRemaining()))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestIslandTemplates(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        File templatesFolder = new File("config/HavenSkyblockBuilder/Templates"); // Define the folder path relative to the server directory

        if (templatesFolder.exists() && templatesFolder.isDirectory()) {
            File[] files = templatesFolder.listFiles((dir, name) -> name.endsWith(".nbt")); // Filter NBT files
            if (files != null) {
                for (File file : files) {
                    String templateName = file.getName().replace(".nbt", ""); // Remove ".nbt" extension
                    builder.suggest(templateName); // Add each template to suggestions
                }
            }
        }

        return builder.buildFuture();
    }

    public static CompletableFuture<Suggestions> suggestStates(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder)
    {
        builder.suggest("true");
        builder.suggest("false");
        return builder.buildFuture();
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        TeamManager.loadAllTeams(context.getSource().getServer());
        Player player = context.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.translatable("haven_skyblock_builder.reload"));
        return -1;
    }
}
