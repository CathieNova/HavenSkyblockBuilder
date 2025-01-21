package net.cathienova.haven_skyblock_builder.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.io.*;
import java.util.concurrent.CompletableFuture;

public class ModCommands
{
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("havensb");
        command.then(Commands.literal("spawn")
                .executes(SkyblockUtils::goSpawn));

        // Island-based commands
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

        // Team-based commands
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
        admin.then(Commands.literal("addmember")
                .then(Commands.argument("team", StringArgumentType.word())
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
        admin.then(Commands.literal("changename")
                .then(Commands.argument("team", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestTeams)
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(SkyblockUtils::adminChangeTeamName))));
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
}
