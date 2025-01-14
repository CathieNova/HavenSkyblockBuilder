package net.cathienova.haven_skyblock_builder.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.cathienova.haven_skyblock_builder.team.Team;
import net.cathienova.haven_skyblock_builder.team.TeamManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestions
{
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
}
