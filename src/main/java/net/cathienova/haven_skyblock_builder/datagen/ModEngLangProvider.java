package net.cathienova.haven_skyblock_builder.datagen;

import net.cathienova.haven_skyblock_builder.HavenSkyblockBuilder;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModEngLangProvider extends LanguageProvider {
    public ModEngLangProvider(PackOutput output) {
        super(output, HavenSkyblockBuilder.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("haven_skyblock_builder.island.create.name", "§cPlease provide a name for your island.");
        add("haven_skyblock_builder.island.create.missing_island", "§cMissing island name for the creation.");
        add("haven_skyblock_builder.island.create.missing_name", "§cMissing name for the island.");
        add("haven_skyblock_builder.island.not_in_overworld", "§cYou must be in the overworld for this command.");
        add("haven_skyblock_builder.island.spawn_teleport", "§aTeleported to spawn.");

        add("haven_skyblock_builder.team.creation_success", "§6'§5%s§6' §acreated successfully. Welcome to your new island!");
        add("haven_skyblock_builder.team.already_in_team", "§cYou are already in a team.");
        add("haven_skyblock_builder.team.name_exists", "§cA team with that name already exists.");
        add("haven_skyblock_builder.team.island_error", "§cFailed to find a valid position for the new island. Please try again later.");
        add("haven_skyblock_builder.team.not_in_team", "§cYou are not part of any team.");
        add("haven_skyblock_builder.team.leave_success", "§aYou have left the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.disband_success", "§aThe team §6'§5%s§6' §ahas been disbanded.");
        add("haven_skyblock_builder.team.disband_leave", "§cYour team has been disbanded by §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.not_leader", "§cYou are not the leader of a team.");
        add("haven_skyblock_builder.team.no_home", "§cNo home is set for your team.");
        add("haven_skyblock_builder.team.home_teleport", "§aTeleported to team home.");
        add("haven_skyblock_builder.team.home_set", "§aTeam home set to: %s.");
        add("haven_skyblock_builder.team.player_not_online", "§cPlayer §6'§5%s§6' §cis not online.");
        add("haven_skyblock_builder.team.invite_sent", "§aInvitation sent to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_received", "§a%s has invited you to their team.");
        add("haven_skyblock_builder.team.invitee_already_in_team", "§cPlayer §6'§5%s§6' §cis already in a team.");
        add("haven_skyblock_builder.team.no_invites", "§cYou have no pending invitations.");
        add("haven_skyblock_builder.team.team_not_found", "§cThe team you were invited to no longer exists.");
        add("haven_skyblock_builder.team.join_success", "§aYou have joined the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_declined", "§aYou have declined the invitation.");
        add("haven_skyblock_builder.team.member_not_found", "§c§6'§5%s§6' §cis not in your team.");
        add("haven_skyblock_builder.team.kick_success", "§a§6'§5%s§6' §ahas been removed from the team.");
        add("haven_skyblock_builder.team.kicked", "§cYou have been removed from the team §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.transfer_success", "§aLeadership transferred to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.leader_change", "§aYou are no longer the leader, it has been transferred to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.new_leader", "§aYou are now the leader of the team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.no_teams", "§cNo teams found.");
        add("haven_skyblock_builder.team.list_header", "§aTeams:");
        add("haven_skyblock_builder.team.not_found", "§cTeam §6'§5%s§6' §cnot found.");
        add("haven_skyblock_builder.team.player_not_found", "§cPlayer §6'§5%s§6' §cnot found.");
        add("haven_skyblock_builder.team.member_added", "§aPlayer §6'§5%s§6' added to team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.member_removed", "§aPlayer §6'§5%s§6' removed from team §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.invite_already_sent", "§cAn invitation has already been sent to §6'§5%s§6'§c.");
        add("haven_skyblock_builder.team.name_changed", "§aTeam name changed to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.list_entry", "§6'§5%s§6' §a- Leader: §6'§5%s§6'§a, Members: %s");
        add("haven_skyblock_builder.team.visit_disabled", "§cVisiting this island is now disabled.");
        add("haven_skyblock_builder.team.visit_enabled", "§aVisiting this island is now enabled.");
        add("haven_skyblock_builder.team.visit_teleport", "§aTeleported to §6'§5%s§6'§a's island.");
        add("haven_skyblock_builder.team.visit_not_allowed", "§cYou are not allowed to visit §6'§5%s§6'§c'.");
        add("haven_skyblock_builder.team.cannot_kick_leader", "§cYou cannot kick the team leader.");

        add("haven_skyblock_builder.admin.no_permission", "§cYou do not have permission to use this command.");
        add("haven_skyblock_builder.admin.list_entry", "§6'§5%s§6'");

        add("haven_skyblock_builder.team.cannot_deport_team_member", "§cYou cannot deport a member of your own team.");
        add("haven_skyblock_builder.team.not_near_island", "§cThe player is not near your island.");
        add("haven_skyblock_builder.team.booted_to_own_island", "§cYou have been deported to your own island.");
        add("haven_skyblock_builder.team.booted_to_spawn", "§cYou have been deported to the spawn area.");
        add("haven_skyblock_builder.team.boot_success", "§aPlayer §6'§5%s§6' §ahas been deported from the island.");
        add("haven_skyblock_builder.team.cannot_kick_self", "§cYou cannot kick yourself from the team.");
        add("haven_skyblock_builder.team.can_not_visit_own_island", "§cYou cannot visit your own island.");

        add("haven_skyblock_builder.team.island_information", "§aTeam: §6'§5%s§6'§a\nLeader: §6'§5%s§6'§a\nAllow visit: %s\n§aHome Position: §5%s§r\n§aMembers: §5%s");
        add("haven_skyblock_builder.team.finding_island_location", "§aFinding a suitable location for the island...");
        add("haven_skyblock_builder.team.remove_success", "§aTeam §6'§5%s§6' §aremoved successfully.");

        add("haven_skyblock_builder.error.teleport_failed", "§cFailed to teleport to the island. Please try again...");
        add("haven_skyblock_builder.error.kick_failed", "§cFailed to kick player from the team. Please try again...");

        add("haven_skyblock_builder.cooldown_message", "§cYou must wait §6'%s§6' §cseconds before using this command again.");

        add("haven_skyblock_builder.reload", "§aConfig reloaded.");

        add("haven_skyblock_builder.gui.screen", "Haven Skyblock Builder");
        add("haven_skyblock_builder.gui.loading", "Loading island information...");
        add("haven_skyblock_builder.gui.your_island", "Your Island: %s");
        add("haven_skyblock_builder.gui.no_island", "You do not have an island yet.");
        add("haven_skyblock_builder.gui.create_hint", "Create one with /havensb island create <template> <name>");
        add("haven_skyblock_builder.gui.leader", "Leader: %s");
        add("haven_skyblock_builder.gui.members", "Members: %s");
        add("haven_skyblock_builder.gui.visits_enabled", "Island visits are enabled");
        add("haven_skyblock_builder.gui.visits_disabled", "Island visits are disabled");
        add("haven_skyblock_builder.gui.home", "Home");
        add("haven_skyblock_builder.gui.spawn", "Spawn");
        add("haven_skyblock_builder.gui.visit", "Visit");
        add("haven_skyblock_builder.gui.refresh", "Refresh");
        add("haven_skyblock_builder.gui.islands", "Islands");
        add("haven_skyblock_builder.gui.no_islands", "No islands are available.");
        add("haven_skyblock_builder.gui.enabled", "Enabled");
        add("haven_skyblock_builder.gui.disabled", "Disabled");
        add("haven_skyblock_builder.gui.island_entry", "Leader: %s | Members: %s | Visits: %s");
        add("haven_skyblock_builder.gui.teams_count", "Teams (%s)");
        add("haven_skyblock_builder.gui.create_team", "Create New Team");
        add("haven_skyblock_builder.gui.team_info", "Team Info");
        add("haven_skyblock_builder.gui.click_for_info_button", "Click for Info");
        add("haven_skyblock_builder.gui.your_team_prefix", "Your Team: ");
        add("haven_skyblock_builder.gui.filter_visits_all", "Visits: All");
        add("haven_skyblock_builder.gui.filter_visits_allowed", "Visits: Allowed");
        add("haven_skyblock_builder.gui.filter_join_all", "Join: All");
        add("haven_skyblock_builder.gui.filter_join_allowed", "Join: Allowed");
        add("haven_skyblock_builder.gui.filter_empty_shown", "Empty: Shown");
        add("haven_skyblock_builder.gui.filter_empty_hidden", "Empty: Hidden");
        add("haven_skyblock_builder.gui.sort", "Sort: %s");
        add("haven_skyblock_builder.gui.sort_name", "Name");
        add("haven_skyblock_builder.gui.sort_members_high", "Members ↓");
        add("haven_skyblock_builder.gui.sort_members_low", "Members ↑");
        add("haven_skyblock_builder.gui.sort_newest", "Newest");
        add("haven_skyblock_builder.gui.visit_team", "Visit Team");
        add("haven_skyblock_builder.gui.request_join", "Request to Join");
        add("haven_skyblock_builder.gui.home_island", "Home Island");
        add("haven_skyblock_builder.gui.teleport_spawn", "Teleport to Spawn");
        add("haven_skyblock_builder.gui.no_teams_match", "No teams match the selected filters.");
        add("haven_skyblock_builder.gui.team_access", "Visits: %s | Join requests: %s");
        add("haven_skyblock_builder.gui.allowed", "Allowed");
        add("haven_skyblock_builder.gui.blocked", "Blocked");
        add("haven_skyblock_builder.gui.tooltip_leader", "Leader: %s");
        add("haven_skyblock_builder.gui.tooltip_members", "Members: %s");
        add("haven_skyblock_builder.gui.tooltip_created", "Created: %s");
        add("haven_skyblock_builder.gui.tooltip_changed", "Last changed: %s");
        add("haven_skyblock_builder.gui.click_for_info", "Click Team Info for settings");
        add("haven_skyblock_builder.gui.team_name", "Team Name");
        add("haven_skyblock_builder.gui.team_name_hint", "Enter a team name");
        add("haven_skyblock_builder.gui.template", "Island Template");
        add("haven_skyblock_builder.gui.no_templates", "No island templates found");
        add("haven_skyblock_builder.gui.allow_visits", "Allow Visits");
        add("haven_skyblock_builder.gui.allow_join_requests", "Allow Join Requests");
        add("haven_skyblock_builder.gui.visits_toggle", "Visits: %s");
        add("haven_skyblock_builder.gui.join_requests_toggle", "Join: %s");
        add("haven_skyblock_builder.gui.create", "Create");
        add("haven_skyblock_builder.gui.creating", "Creating...");
        add("haven_skyblock_builder.gui.island_preview", "3D Island Preview");
        add("haven_skyblock_builder.gui.template_size", "Size: %s x %s x %s");
        add("haven_skyblock_builder.gui.island_settings", "Island Settings");
        add("haven_skyblock_builder.gui.template_value", "Template: %s");
        add("haven_skyblock_builder.gui.spawn_position", "Island Spawn Position");
        add("haven_skyblock_builder.gui.save_settings", "Save Settings");
        add("haven_skyblock_builder.gui.invite_member", "Invite Member");
        add("haven_skyblock_builder.gui.invite_player", "Invite Player");
        add("haven_skyblock_builder.gui.no_players_online", "No eligible players are online.");
        add("haven_skyblock_builder.gui.player_name", "Player name");
        add("haven_skyblock_builder.gui.invite", "Invite");
        add("haven_skyblock_builder.gui.leave_team", "Leave Team");
        add("haven_skyblock_builder.gui.confirm_leave", "Confirm Leave");
        add("haven_skyblock_builder.gui.previous_screen", "Previous Screen");
        add("haven_skyblock_builder.gui.remove_member", "Remove Selected Member");
        add("haven_skyblock_builder.gui.accept", "Accept");
        add("haven_skyblock_builder.gui.deny", "Deny");
        add("haven_skyblock_builder.gui.team_metadata", "Leader: %s | Created: %s | Changed: %s");
        add("haven_skyblock_builder.gui.members_count", "Members (%s)");
        add("haven_skyblock_builder.gui.join_requests_count", "Join Requests (%s)");
        add("haven_skyblock_builder.gui.read_only", "Only the team leader can edit these settings.");
        add("haven_skyblock_builder.gui.read_only_short", "Read Only");
        add("haven_skyblock_builder.gui.member_leader", "%s (Leader)");
        add("haven_skyblock_builder.gui.settings_saved", "§aTeam settings saved.");
        add("haven_skyblock_builder.gui.permissions_saved", "§aMember permissions saved.");
        add("haven_skyblock_builder.gui.filters", "Filters");
        add("haven_skyblock_builder.gui.filter_visits", "Only Teams Allowing Visits");
        add("haven_skyblock_builder.gui.filter_join", "Only Teams Allowing Join Requests");
        add("haven_skyblock_builder.gui.filter_hide_empty", "Hide Empty Teams");
        add("haven_skyblock_builder.gui.filter_disbanded", "Show Disbanded Teams");
        add("haven_skyblock_builder.gui.team_settings", "Team Settings");
        add("haven_skyblock_builder.gui.your_team", "Your Team: %s");
        add("haven_skyblock_builder.gui.info", "Info");
        add("haven_skyblock_builder.gui.join", "Join");
        add("haven_skyblock_builder.gui.req_join", "Req Join");
        add("haven_skyblock_builder.gui.more_members", "...and %s more");
        add("haven_skyblock_builder.gui.disbanded", "Disbanded");
        add("haven_skyblock_builder.gui.team_overview", "Team Information");
        add("haven_skyblock_builder.gui.visits_value", "Visits: %s");
        add("haven_skyblock_builder.gui.join_value", "Join Requests: %s");
        add("haven_skyblock_builder.gui.preview_controls", "Drag to rotate • Scroll to zoom");
        add("haven_skyblock_builder.gui.tab_island", "Island");
        add("haven_skyblock_builder.gui.tab_members", "Members");
        add("haven_skyblock_builder.gui.tab_requests", "Requests (%s)");
        add("haven_skyblock_builder.gui.island_spawn_position", "Island Spawn Position");
        add("haven_skyblock_builder.gui.use_current_position", "Use Current Position");
        add("haven_skyblock_builder.gui.select_member", "Select a member to manage permissions.");
        add("haven_skyblock_builder.gui.member_permissions", "Permissions for %s");
        add("haven_skyblock_builder.gui.permission_invite", "Invite Members");
        add("haven_skyblock_builder.gui.permission_accept_requests", "Accept Join Requests");
        add("haven_skyblock_builder.gui.permission_change_spawn", "Change Island Spawn");
        add("haven_skyblock_builder.gui.permission_change_visits", "Change Visit Setting");
        add("haven_skyblock_builder.gui.permission_change_join", "Change Join Request Setting");
        add("haven_skyblock_builder.gui.save_permissions", "Save Member Permissions");
        add("haven_skyblock_builder.gui.no_join_requests", "No pending join requests.");

        add("haven_skyblock_builder.team.template_not_found", "§cIsland template §6'§5%s§6' §cwas not found.");
        add("haven_skyblock_builder.team.join_requests_disabled", "§cThis team does not accept join requests.");
        add("haven_skyblock_builder.team.join_request_exists", "§cYou already have a pending request for this team.");
        add("haven_skyblock_builder.team.join_request_sent", "§aJoin request sent to §6'§5%s§6'§a.");
        add("haven_skyblock_builder.team.join_request_received", "§6'§5%s§6' §ahas requested to join your team.");
        add("haven_skyblock_builder.team.no_team_permission", "§cYou do not have permission to change that team setting.");

        add("key.category.haven_skyblock_builder.key_category", "Haven Skyblock Builder");
        add("key.haven_skyblock_builder.open_skyblock_screen", "Open Haven Skyblock Builder GUI");

        add("generator.haven_skyblock_builder.skyblock_world", "Haven Skyblock");
        add("haven_skyblock_builder.message.skyblock_spawn", "§aYou don't have an island yet, you can make one with '/havensb island create <template> <name>'.");
    }
}