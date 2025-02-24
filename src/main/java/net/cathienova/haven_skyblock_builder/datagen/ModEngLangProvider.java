package net.cathienova.haven_skyblock_builder.datagen;

import net.cathienova.haven_skyblock_builder.item.ModCreativeModTabs;
import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEngLangProvider extends LanguageProvider
{
    public ModEngLangProvider(PackOutput output)
    {
        super(output, HavenSkyblockBuilder.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        String prefix = "§6[§5Haven §2Skyblock §3Builder§6]§r ";
        add(ModCreativeModTabs.HavenSkyblockBuilder_tab_title, "Haven Skyblock Builder");

        add("haven_skyblock_builder.island.create.name", prefix + "§cPlease provide a name for your island.");
        add("haven_skyblock_builder.island.create.missing_island", prefix + "§cMissing island name for the creation.");
        add("haven_skyblock_builder.island.create.missing_name", prefix + "§cMissing name for the island.");
        add("haven_skyblock_builder.island.not_in_overworld", prefix + "§cYou must be in the overworld for this command.");
        add("haven_skyblock_builder.island.spawn_teleport", prefix + "§aTeleported to spawn.");

        add("haven_skyblock_builder.team.creation_success", prefix + "§6'§5%s§6' §acreated successfully. Welcome to your new island!");
        add("haven_skyblock_builder.team.already_in_team", prefix + "§cYou are already in a team.");
        add("haven_skyblock_builder.team.name_exists", prefix + "§cA team with that name already exists.");
        add("haven_skyblock_builder.team.island_error", prefix + "§cFailed to find a valid position for the new island. Please try again later.");
        add("haven_skyblock_builder.team.not_in_team", prefix + "§cYou are not part of any team.");
        add("haven_skyblock_builder.team.leave_success", prefix + "§aYou have left the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.disband_success", prefix + "§aThe team §6'§5%s§6' §ahas been disbanded.");
        add("haven_skyblock_builder.team.disband_leave", prefix + "§cYour team has been disbanded by §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.not_leader", prefix + "§cYou are not the leader of a team.");
        add("haven_skyblock_builder.team.no_home", prefix + "§cNo home is set for your team.");
        add("haven_skyblock_builder.team.home_teleport", prefix + "§aTeleported to team home.");
        add("haven_skyblock_builder.team.home_set", prefix + "§aTeam home set to: %s.");
        add("haven_skyblock_builder.team.player_not_online", prefix + "§cPlayer §6'§5%s§6' §cis not online.");
        add("haven_skyblock_builder.team.invite_sent", prefix + "§aInvitation sent to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_received", prefix + "§a%s has invited you to their team.");
        add("haven_skyblock_builder.team.invitee_already_in_team", prefix + "§cPlayer §6'§5%s§6' §cis already in a team.");
        add("haven_skyblock_builder.team.no_invites", prefix + "§cYou have no pending invitations.");
        add("haven_skyblock_builder.team.team_not_found", prefix + "§cThe team you were invited to no longer exists.");
        add("haven_skyblock_builder.team.join_success", prefix + "§aYou have joined the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_declined", prefix + "§aYou have declined the invitation.");
        add("haven_skyblock_builder.team.member_not_found", prefix + "§c§6'§5%s§6' §cis not in your team.");
        add("haven_skyblock_builder.team.kick_success", prefix + "§a§6'§5%s§6' §ahas been removed from the team.");
        add("haven_skyblock_builder.team.kicked", prefix + "§cYou have been removed from the team §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.transfer_success", prefix + "§aLeadership transferred to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.leader_change", prefix + "§aYou are no longer the leader, it has been transferred to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.new_leader", prefix + "§aYou are now the leader of the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.no_teams", prefix + "§cNo teams found.");
        add("haven_skyblock_builder.team.list_header", prefix + "§aTeams:");
        add("haven_skyblock_builder.team.not_found", prefix + "§cTeam §6'§5%s§6' §cnot found.");
        add("haven_skyblock_builder.team.player_not_found", prefix + "§cPlayer §6'§5%s§6' §cnot found.");
        add("haven_skyblock_builder.team.member_added", prefix + "§aPlayer §6'§5%s§6' added to team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.member_removed", prefix + "§aPlayer §6'§5%s§6' removed from team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_already_sent", prefix + "§cAn invitation has already been sent to §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.name_changed", prefix + "§aTeam name changed to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.list_entry", "§6'§5%s§6' §a- Leader: §6'§5%s§6'§a, Members: %s");
        add("haven_skyblock_builder.admin.list_entry", "§6'§5%s§6'");
        add("haven_skyblock_builder.team.visit_disabled", prefix + "§cVisiting this island is now disabled.");
        add("haven_skyblock_builder.team.visit_enabled", prefix + "§aVisiting this island is now enabled.");
        add("haven_skyblock_builder.team.visit_teleport", prefix + "§aTeleported to §6'§5%s§6'§a's island.");
        add("haven_skyblock_builder.team.visit_not_allowed", prefix + "§cYou are not allowed to visit §6'§5%s§6'§c'.");
        add("haven_skyblock_builder.team.cannot_kick_leader", prefix + "§cYou cannot kick the team leader.");
        add("haven_skyblock_builder.admin.no_permission", prefix + "§cYou do not have permission to use this command.");

        add("haven_skyblock_builder.team.cannot_deport_team_member", prefix + "§cYou cannot deport a member of your own team.");
        add("haven_skyblock_builder.team.not_near_island", prefix + "§cThe player is not near your island.");
        add("haven_skyblock_builder.team.booted_to_own_island", prefix + "§cYou have been deported to your own island.");
        add("haven_skyblock_builder.team.booted_to_spawn", prefix + "§cYou have been deported to the spawn area.");
        add("haven_skyblock_builder.team.boot_success", prefix + "§aPlayer §6'§5%s§6' §ahas been deported from the island.");
        add("haven_skyblock_builder.team.cannot_kick_self", prefix + "§cYou cannot kick yourself from the team.");
        add("haven_skyblock_builder.team.can_not_visit_own_island", prefix + "§cYou cannot visit your own island.");
        // using ' with colors will not display colors after ' so we need a different approach
        add("haven_skyblock_builder.team.island_information", prefix + "§aTeam: §6'§5%s§6'§a\nLeader: §6'§5%s§6'§a\nAllow visit: %s\n§aHome Position: §5%s§r\n§aMembers: §5%s");
        add("haven_skyblock_builder.team.finding_island_location", prefix + "§aFinding a suitable location for the island...");
        add("haven_skyblock_builder.team.remove_success", prefix + "§aTeam §6'§5%s§6' §aremoved successfully.");

        add("haven_skyblock_builder.error.teleport_failed", prefix + "§cFailed to teleport to the island. Please try again...");
        add("haven_skyblock_builder.error.kick_failed", prefix + "§cFailed to kick player from the team. Please try again...");

        add("haven_skyblock_builder.cooldown_message", prefix + "§cYou must wait §6'%s§6' §cseconds before using this command again.");

        add("haven_skyblock_builder.reload", prefix + "§aConfig reloaded.");

        add("haven_skyblock_builder.gui.screen", "Haven Skyblock Builder");

        add("key.categories.haven_skyblock_builder", "Haven Skyblock Builder");
        add("key.haven_skyblock_builder.open_skyblock_screen", "Open Haven Skyblock Builder GUI");

        add("generator.haven_skyblock_builder.skyblock_world", "Haven Skyblock");
        add("haven_skyblock_builder.message.skyblock_spawn", prefix + "§aYou don't have an island yet, you can make one with '/havensb island create <template> <name>'.");
    }
}
