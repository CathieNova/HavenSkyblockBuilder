package net.cathienova.haven_skyblock_builder.screens;

import net.cathienova.haven_skyblock_builder.networking.IslandScreenActionMessage;
import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

public class TeamInfoScreen extends Screen {
    private final IslandScreen previousScreen;
    private IslandScreenDataMessage data;
    private Tab selectedTab = Tab.ISLAND;
    private boolean allowVisit;
    private boolean allowJoinRequests;
    private boolean confirmingLeave;
    private String teamNameValue;
    private String homeXValue;
    private String homeYValue;
    private String homeZValue;
    private String selectedMemberId = "";
    private String selectedRequestId = "";
    private boolean memberCanInvite;
    private boolean memberCanAcceptRequests;
    private boolean memberCanChangeSpawn;
    private boolean memberCanChangeVisits;
    private boolean memberCanChangeJoinRequests;
    private EditBox teamNameInput;
    private EditBox homeXInput;
    private EditBox homeYInput;
    private EditBox homeZInput;
    private HavenCheckbox visitButton;
    private HavenCheckbox joinButton;
    private Button saveButton;
    private Button findPlayerButton;
    private Button removeMemberButton;
    private Button acceptButton;
    private Button denyButton;
    private Button leaveButton;
    private HavenCheckbox memberInviteButton;
    private HavenCheckbox memberAcceptButton;
    private HavenCheckbox memberSpawnButton;
    private HavenCheckbox memberVisitsButton;
    private HavenCheckbox memberJoinButton;
    private Button savePermissionsButton;
    private MemberList memberList;
    private JoinRequestList requestList;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int contentLeft;
    private int contentTop;
    private int contentWidth;
    private int contentHeight;

    public TeamInfoScreen(IslandScreen previousScreen, IslandScreenDataMessage data) {
        super(Component.translatable("haven_skyblock_builder.gui.team_settings"));
        this.previousScreen = previousScreen;
        this.data = data;
        this.allowVisit = data.allowVisit();
        this.allowJoinRequests = data.allowJoinRequests();
        this.teamNameValue = data.teamName();
        this.homeXValue = Integer.toString(data.homeX());
        this.homeYValue = Integer.toString(data.homeY());
        this.homeZValue = Integer.toString(data.homeZ());
    }

    private static Integer parseNumber(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void init() {
        this.buildWidgets();
    }

    private void buildWidgets() {
        this.clearWidgets();
        this.teamNameInput = null;
        this.homeXInput = null;
        this.homeYInput = null;
        this.homeZInput = null;
        this.memberList = null;
        this.requestList = null;

        this.panelWidth = Math.max(360, Math.min(760, this.width * 76 / 100));
        this.panelHeight = Math.max(230, Math.min(430, this.height * 82 / 100));
        this.panelWidth = Math.min(this.panelWidth, this.width - 24);
        this.panelHeight = Math.min(this.panelHeight, this.height - 24);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;
        this.contentLeft = this.panelLeft + 14;
        this.contentTop = this.panelTop + 66;
        this.contentWidth = this.panelWidth - 28;
        this.contentHeight = this.panelHeight - 102;

        this.addRenderableWidget(Button.builder(Component.literal("X"), button ->
                        {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(null);
                            }
                        })
                .bounds(this.panelLeft + this.panelWidth - 26, this.panelTop + 8, 18, 18)
                .build());

        int tabGap = 6;
        int tabWidth = Math.min(150, (this.contentWidth - tabGap * 2) / 3);
        int tabsWidth = tabWidth * 3 + tabGap * 2;
        int tabsLeft = this.panelLeft + (this.panelWidth - tabsWidth) / 2;
        int tabY = this.panelTop + 40;
        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.tab_island"), button -> this.changeTab(Tab.ISLAND))
                .bounds(tabsLeft, tabY, tabWidth, 20).build()).active = this.selectedTab != Tab.ISLAND;
        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.tab_members"), button -> this.changeTab(Tab.MEMBERS))
                .bounds(tabsLeft + tabWidth + tabGap, tabY, tabWidth, 20).build()).active = this.selectedTab != Tab.MEMBERS;
        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.tab_requests", this.data.joinRequests().size()), button -> this.changeTab(Tab.REQUESTS))
                .bounds(tabsLeft + (tabWidth + tabGap) * 2, tabY, tabWidth, 20).build()).active = this.selectedTab != Tab.REQUESTS;

        switch (this.selectedTab) {
            case ISLAND -> this.buildIslandTab();
            case MEMBERS -> this.buildMembersTab();
            case REQUESTS -> this.buildRequestsTab();
        }

        int bottomY = this.panelTop + this.panelHeight - 30;
        int bottomGap = 8;
        int leaveWidth = 108;
        int previousWidth = 124;
        int bottomLeft = this.width / 2 - (leaveWidth + bottomGap + previousWidth) / 2;
        this.leaveButton = this.addRenderableWidget(Button.builder(this.leaveText(), button -> this.leaveTeam())
                .bounds(bottomLeft, bottomY, leaveWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.previous_screen"), button -> this.onClose())
                .bounds(bottomLeft + leaveWidth + bottomGap, bottomY, previousWidth, 20).build());
        this.updateButtonStates();
    }

    private void buildIslandTab() {
        int cardWidth = Math.min(520, this.contentWidth - 20);
        int left = this.contentLeft + (this.contentWidth - cardWidth) / 2;
        int gap = 6;
        int coordinateWidth = (cardWidth - gap * 2) / 3;

        this.teamNameInput = new EditBox(this.font, left, this.contentTop + 38, cardWidth, 20, Component.translatable("haven_skyblock_builder.gui.team_name"));
        this.teamNameInput.setMaxLength(64);
        this.teamNameInput.setHint(Component.translatable("haven_skyblock_builder.gui.team_name_hint"));
        this.teamNameInput.setValue(this.teamNameValue);
        this.teamNameInput.setResponder(value -> this.teamNameValue = value);
        this.addRenderableWidget(this.teamNameInput);

        int y = this.contentTop + 92;
        this.homeXInput = this.createNumberInput(left, y, coordinateWidth, this.homeXValue);
        this.homeYInput = this.createNumberInput(left + coordinateWidth + gap, y, coordinateWidth, this.homeYValue);
        this.homeZInput = this.createNumberInput(left + (coordinateWidth + gap) * 2, y, coordinateWidth, this.homeZValue);
        y += 25;

        int currentPositionWidth = Math.min(160, cardWidth);
        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.use_current_position"), button -> this.useCurrentPosition())
                .bounds(left + (cardWidth - currentPositionWidth) / 2, y, currentPositionWidth, 18).build()).active = this.data.canChangeSpawn();
        y += 24;

        int checkboxColumnWidth = Math.min(190, (cardWidth - gap) / 2);
        int checkboxesLeft = left + (cardWidth - checkboxColumnWidth * 2 - gap) / 2;
        this.visitButton = this.addRenderableWidget(new HavenCheckbox(
                checkboxesLeft,
                y,
                checkboxColumnWidth,
                this.allowVisit,
                Component.translatable("haven_skyblock_builder.gui.allow_visits"),
                selected -> this.allowVisit = selected
        ));
        this.joinButton = this.addRenderableWidget(new HavenCheckbox(
                checkboxesLeft + checkboxColumnWidth + gap,
                y,
                checkboxColumnWidth,
                this.allowJoinRequests,
                Component.translatable("haven_skyblock_builder.gui.allow_join_requests"),
                selected -> this.allowJoinRequests = selected
        ));
        y += 20;

        int saveWidth = 112;
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.save_settings"), button -> this.saveSettings())
                .bounds(left + (cardWidth - saveWidth) / 2, y, saveWidth, 18).build());
    }

    private void buildMembersTab() {
        int gap = 10;
        int leftWidth = Math.min(230, Math.max(160, this.contentWidth * 36 / 100));
        int rightLeft = this.contentLeft + leftWidth + gap;
        int rightWidth = this.contentWidth - leftWidth - gap;
        int listTop = this.contentTop + 24;
        int findPlayerY = this.contentTop + this.contentHeight - 22;
        int listHeight = Math.max(48, findPlayerY - listTop - 5);

        this.memberList = new MemberList(Objects.requireNonNull(this.minecraft), leftWidth, listHeight, listTop, 24);
        this.memberList.setX(this.contentLeft);
        this.memberList.setMembers(this.data.members());
        this.addWidget(this.memberList);

        int findPlayerWidth = Math.min(112, leftWidth);
        this.findPlayerButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.invite_player"), button -> this.openFindPlayer())
                .bounds(this.contentLeft + (leftWidth - findPlayerWidth) / 2, findPlayerY, findPlayerWidth, 18).build());

        int columnGap = 5;
        int halfWidth = Math.min(176, (rightWidth - columnGap) / 2);
        int permissionsLeft = rightLeft + (rightWidth - halfWidth * 2 - columnGap) / 2;
        int y = this.contentTop + 24;
        this.memberInviteButton = this.addPermissionButton(permissionsLeft, y, halfWidth, Permission.INVITE);
        this.memberAcceptButton = this.addPermissionButton(permissionsLeft + halfWidth + columnGap, y, halfWidth, Permission.ACCEPT_REQUESTS);
        y += 23;
        this.memberSpawnButton = this.addPermissionButton(permissionsLeft, y, halfWidth, Permission.CHANGE_SPAWN);
        this.memberVisitsButton = this.addPermissionButton(permissionsLeft + halfWidth + columnGap, y, halfWidth, Permission.CHANGE_VISITS);
        y += 23;
        int joinWidth = Math.min(220, rightWidth);
        this.memberJoinButton = this.addPermissionButton(rightLeft + (rightWidth - joinWidth) / 2, y, joinWidth, Permission.CHANGE_JOIN);
        y += 25;
        int actionWidth = Math.min(170, (rightWidth - columnGap) / 2);
        int actionsLeft = rightLeft + (rightWidth - actionWidth * 2 - columnGap) / 2;
        this.savePermissionsButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.save_permissions"), button -> this.saveMemberPermissions())
                .bounds(actionsLeft, y, actionWidth, 18).build());
        this.removeMemberButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.remove_member"), button -> this.removeSelectedMember())
                .bounds(actionsLeft + actionWidth + columnGap, y, actionWidth, 18).build());
    }

    private void buildRequestsTab() {
        int listTop = this.contentTop + 24;
        int y = this.contentTop + this.contentHeight - 22;
        int listHeight = Math.max(60, y - listTop - 6);
        this.requestList = new JoinRequestList(Objects.requireNonNull(this.minecraft), this.contentWidth, listHeight, listTop, 24);
        this.requestList.setX(this.contentLeft);
        this.requestList.setRequests(this.data.joinRequests());
        this.addWidget(this.requestList);
        int gap = 8;
        int width = 96;
        int left = this.width / 2 - (width * 2 + gap) / 2;
        this.acceptButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.accept"), button -> this.acceptSelectedRequest())
                .bounds(left, y, width, 20).build());
        this.denyButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.deny"), button -> this.denySelectedRequest())
                .bounds(left + width + gap, y, width, 20).build());
    }

    private HavenCheckbox addPermissionButton(int x, int y, int width, Permission permission) {
        return this.addRenderableWidget(new HavenCheckbox(
                x,
                y,
                width,
                this.permissionEnabled(permission),
                Component.translatable(permission.translationKey),
                selected -> this.setPermission(permission, selected)
        ));
    }

    public void updateData(IslandScreenDataMessage message) {
        this.data = message;
        this.previousScreen.updateData(message);
        if (!message.hasTeam()) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return;
        }
        this.allowVisit = message.allowVisit();
        this.allowJoinRequests = message.allowJoinRequests();
        this.teamNameValue = message.teamName();
        this.homeXValue = Integer.toString(message.homeX());
        this.homeYValue = Integer.toString(message.homeY());
        this.homeZValue = Integer.toString(message.homeZ());
        this.confirmingLeave = false;
        this.selectedMemberId = this.findMember(this.selectedMemberId) == null ? "" : this.selectedMemberId;
        this.selectedRequestId = this.findRequest(this.selectedRequestId) == null ? "" : this.selectedRequestId;
        this.loadSelectedMemberPermissions();
        if (this.minecraft != null && this.minecraft.screen == this) {
            this.buildWidgets();
        }
    }

    private void changeTab(Tab tab) {
        this.selectedTab = tab;
        this.setFocused(null);
        this.buildWidgets();
    }

    private EditBox createNumberInput(int x, int y, int width, String value) {
        EditBox input = new EditBox(this.font, x, y, width, 20, Component.empty());
        input.setMaxLength(11);
        input.setFilter(text -> text.isEmpty() || text.equals("-") || text.matches("-?\\d+"));
        input.setValue(value);
        input.setResponder(newValue ->
        {
            if (input == this.homeXInput) {
                this.homeXValue = newValue;
            } else if (input == this.homeYInput) {
                this.homeYValue = newValue;
            } else {
                this.homeZValue = newValue;
            }
        });
        this.addRenderableWidget(input);
        return input;
    }

    private void useCurrentPosition() {
        if (!this.data.canChangeSpawn() || this.minecraft == null || this.minecraft.player == null) {
            return;
        }
        BlockPos position = this.minecraft.player.blockPosition();
        this.homeXValue = Integer.toString(position.getX());
        this.homeYValue = Integer.toString(position.getY());
        this.homeZValue = Integer.toString(position.getZ());
        this.homeXInput.setValue(this.homeXValue);
        this.homeYInput.setValue(this.homeYValue);
        this.homeZInput.setValue(this.homeZValue);
    }

    private void saveSettings() {
        Integer x = parseNumber(this.homeXValue);
        Integer y = parseNumber(this.homeYValue);
        Integer z = parseNumber(this.homeZValue);
        if (x == null || y == null || z == null) {
            return;
        }
        String teamName = this.teamNameValue == null ? "" : this.teamNameValue.trim();
        if (this.data.canEdit() && (teamName.isEmpty() || teamName.length() > 64)) {
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.updateTeam(teamName, this.allowVisit, this.allowJoinRequests, x, y, z));
    }

    private void openFindPlayer() {
        if (this.minecraft != null && this.data.canInvite()) {
            this.minecraft.setScreen(new FindPlayerScreen(this, this.data.onlinePlayers()));
        }
    }

    void invitePlayer(IslandScreenDataMessage.PlayerEntry player) {
        if (!this.data.canInvite()) {
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.inviteMember(player.name()));
    }

    private void leaveTeam() {
        if (!this.confirmingLeave) {
            this.confirmingLeave = true;
            this.leaveButton.setMessage(this.leaveText());
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.leaveTeam());
        this.leaveButton.active = false;
        if (this.minecraft != null) {
            this.minecraft.setScreen(null);
        }
    }

    private void removeSelectedMember() {
        IslandScreenDataMessage.MemberEntry member = this.findMember(this.selectedMemberId);
        if (!this.data.canEdit() || member == null || member.leader()) {
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.removeMember(member.uuid()));
    }

    private void saveMemberPermissions() {
        IslandScreenDataMessage.MemberEntry member = this.findMember(this.selectedMemberId);
        if (!this.data.canEdit() || member == null || member.leader()) {
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.updateMemberPermissions(
                member.uuid(),
                this.memberCanInvite,
                this.memberCanAcceptRequests,
                this.memberCanChangeSpawn,
                this.memberCanChangeVisits,
                this.memberCanChangeJoinRequests
        ));
    }

    private void acceptSelectedRequest() {
        if (this.data.canAcceptRequests() && !this.selectedRequestId.isEmpty()) {
            ClientPacketDistributor.sendToServer(IslandScreenActionMessage.acceptJoinRequest(this.selectedRequestId));
        }
    }

    private void denySelectedRequest() {
        if (this.data.canAcceptRequests() && !this.selectedRequestId.isEmpty()) {
            ClientPacketDistributor.sendToServer(IslandScreenActionMessage.denyJoinRequest(this.selectedRequestId));
        }
    }

    private void selectMember(IslandScreenDataMessage.MemberEntry member) {
        this.selectedMemberId = member.uuid();
        this.loadSelectedMemberPermissions();
        this.updatePermissionMessages();
        this.updateButtonStates();
    }

    private void loadSelectedMemberPermissions() {
        IslandScreenDataMessage.MemberEntry member = this.findMember(this.selectedMemberId);
        if (member == null) {
            this.memberCanInvite = false;
            this.memberCanAcceptRequests = false;
            this.memberCanChangeSpawn = false;
            this.memberCanChangeVisits = false;
            this.memberCanChangeJoinRequests = false;
            return;
        }
        this.memberCanInvite = member.canInvite();
        this.memberCanAcceptRequests = member.canAcceptRequests();
        this.memberCanChangeSpawn = member.canChangeSpawn();
        this.memberCanChangeVisits = member.canChangeVisits();
        this.memberCanChangeJoinRequests = member.canChangeJoinRequests();
    }

    private void selectRequest(IslandScreenDataMessage.JoinRequestEntry request) {
        this.selectedRequestId = request.uuid();
        this.updateButtonStates();
    }

    private IslandScreenDataMessage.MemberEntry findMember(String uuid) {
        return this.data.members().stream().filter(member -> member.uuid().equals(uuid)).findFirst().orElse(null);
    }

    private IslandScreenDataMessage.JoinRequestEntry findRequest(String uuid) {
        return this.data.joinRequests().stream().filter(request -> request.uuid().equals(uuid)).findFirst().orElse(null);
    }

    private void setPermission(Permission permission, boolean selected) {
        if (!this.canEditSelectedMember()) {
            return;
        }
        switch (permission) {
            case INVITE -> this.memberCanInvite = selected;
            case ACCEPT_REQUESTS -> this.memberCanAcceptRequests = selected;
            case CHANGE_SPAWN -> this.memberCanChangeSpawn = selected;
            case CHANGE_VISITS -> this.memberCanChangeVisits = selected;
            case CHANGE_JOIN -> this.memberCanChangeJoinRequests = selected;
        }
    }

    private boolean permissionEnabled(Permission permission) {
        return switch (permission) {
            case INVITE -> this.memberCanInvite;
            case ACCEPT_REQUESTS -> this.memberCanAcceptRequests;
            case CHANGE_SPAWN -> this.memberCanChangeSpawn;
            case CHANGE_VISITS -> this.memberCanChangeVisits;
            case CHANGE_JOIN -> this.memberCanChangeJoinRequests;
        };
    }

    private boolean canEditSelectedMember() {
        IslandScreenDataMessage.MemberEntry member = this.findMember(this.selectedMemberId);
        return this.data.canEdit() && member != null && !member.leader();
    }

    private void updateButtonStates() {
        if (this.homeXInput != null) {
            this.teamNameInput.active = this.data.canEdit();
            this.homeXInput.active = this.data.canChangeSpawn();
            this.homeYInput.active = this.data.canChangeSpawn();
            this.homeZInput.active = this.data.canChangeSpawn();
            this.visitButton.active = this.data.canChangeVisits();
            this.joinButton.active = this.data.canChangeJoinRequests();
            this.saveButton.active = this.data.canEdit() || this.data.canChangeSpawn() || this.data.canChangeVisits() || this.data.canChangeJoinRequests();
        }
        if (this.findPlayerButton != null) {
            this.findPlayerButton.active = this.data.canInvite();
            boolean canEditMember = this.canEditSelectedMember();
            this.memberInviteButton.active = canEditMember;
            this.memberAcceptButton.active = canEditMember;
            this.memberSpawnButton.active = canEditMember;
            this.memberVisitsButton.active = canEditMember;
            this.memberJoinButton.active = canEditMember;
            this.savePermissionsButton.active = canEditMember;
            this.removeMemberButton.active = canEditMember;
        }
        if (this.acceptButton != null) {
            boolean requestSelected = this.data.canAcceptRequests() && !this.selectedRequestId.isEmpty();
            this.acceptButton.active = requestSelected;
            this.denyButton.active = requestSelected;
        }
    }

    private void updatePermissionMessages() {
        if (this.memberInviteButton != null) {
            this.memberInviteButton.setSelected(this.memberCanInvite);
            this.memberAcceptButton.setSelected(this.memberCanAcceptRequests);
            this.memberSpawnButton.setSelected(this.memberCanChangeSpawn);
            this.memberVisitsButton.setSelected(this.memberCanChangeVisits);
            this.memberJoinButton.setSelected(this.memberCanChangeJoinRequests);
        }
    }

    private Component leaveText() {
        return Component.translatable(this.confirmingLeave ? "haven_skyblock_builder.gui.confirm_leave" : "haven_skyblock_builder.gui.leave_team");
    }

    @Override
    public void tick() {
        this.updateButtonStates();
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (!handled || this.getFocused() instanceof Button || this.getFocused() instanceof HavenCheckbox) {
            this.setFocused(null);
        }
        return handled;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + this.panelWidth + 1, this.panelTop + this.panelHeight + 1, 0xFF586171);
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xF010141C);
        graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 12, 0xFFFFFFFF);
        graphics.centeredText(this.font, Component.literal(this.data.teamName()).withStyle(ChatFormatting.LIGHT_PURPLE), this.width / 2, this.panelTop + 28, 0xFFFFFFFF);

        graphics.fill(this.contentLeft - 1, this.contentTop - 1, this.contentLeft + this.contentWidth + 1, this.contentTop + this.contentHeight + 1, 0xFF38404D);
        graphics.fill(this.contentLeft, this.contentTop, this.contentLeft + this.contentWidth, this.contentTop + this.contentHeight, 0xFF0B1017);

        switch (this.selectedTab) {
            case ISLAND -> this.renderIslandTab(graphics);
            case MEMBERS -> this.renderMembersTab(graphics, mouseX, mouseY, partialTick);
            case REQUESTS -> this.renderRequestsTab(graphics, mouseX, mouseY, partialTick);
        }
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }

    private void renderIslandTab(GuiGraphicsExtractor graphics) {
        int cardWidth = Math.min(520, this.contentWidth - 20);
        int left = this.contentLeft + (this.contentWidth - cardWidth) / 2;
        graphics.fill(left - 8, this.contentTop + 4, left + cardWidth + 8, this.contentTop + this.contentHeight - 4, 0xFF111821);
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.template_value", this.data.islandTemplate()), left, this.contentTop + 10, 0xFFB8B8B8);
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.team_name"), left, this.contentTop + 25, 0xFFFFFFFF);
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.island_spawn_position"), left, this.contentTop + 66, 0xFFFFFFFF);
        int gap = 6;
        int coordinateWidth = (cardWidth - gap * 2) / 3;
        graphics.centeredText(this.font, Component.literal("X"), left + coordinateWidth / 2, this.homeXInput.getY() - 12, 0xFF888888);
        graphics.centeredText(this.font, Component.literal("Y"), left + coordinateWidth + gap + coordinateWidth / 2, this.homeYInput.getY() - 12, 0xFF888888);
        graphics.centeredText(this.font, Component.literal("Z"), left + (coordinateWidth + gap) * 2 + coordinateWidth / 2, this.homeZInput.getY() - 12, 0xFF888888);
    }

    private void renderMembersTab(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int gap = 10;
        int leftWidth = Math.min(230, Math.max(160, this.contentWidth * 36 / 100));
        int rightLeft = this.contentLeft + leftWidth + gap;
        int rightWidth = this.contentWidth - leftWidth - gap;
        graphics.fill(rightLeft - 4, this.contentTop + 2, rightLeft + rightWidth, this.contentTop + this.contentHeight - 2, 0xFF111821);
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.members_count", this.data.members().size()), this.contentLeft + 6, this.contentTop + 6, 0xFFFFFFFF);
        IslandScreenDataMessage.MemberEntry selected = this.findMember(this.selectedMemberId);
        graphics.text(this.font, selected == null
                        ? Component.translatable("haven_skyblock_builder.gui.select_member")
                        : Component.translatable("haven_skyblock_builder.gui.member_permissions", selected.name()),
                rightLeft, this.contentTop + 6, selected == null ? 0xFF777777 : 0xFFFFFFFF);
        if (this.memberList != null) {
            this.memberList.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderRequestsTab(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.join_requests_count", this.data.joinRequests().size()), this.contentLeft + 6, this.contentTop + 6, 0xFFFFFFFF);
        if (this.requestList != null) {
            this.requestList.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        if (this.data.joinRequests().isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.no_join_requests"), this.width / 2, this.contentTop + this.contentHeight / 2, 0xFF777777);
        }
    }

    private enum Tab {
        ISLAND,
        MEMBERS,
        REQUESTS
    }

    private enum Permission {
        INVITE("haven_skyblock_builder.gui.permission_invite"),
        ACCEPT_REQUESTS("haven_skyblock_builder.gui.permission_accept_requests"),
        CHANGE_SPAWN("haven_skyblock_builder.gui.permission_change_spawn"),
        CHANGE_VISITS("haven_skyblock_builder.gui.permission_change_visits"),
        CHANGE_JOIN("haven_skyblock_builder.gui.permission_change_join");

        private final String translationKey;

        Permission(String translationKey) {
            this.translationKey = translationKey;
        }
    }

    private class MemberList extends ObjectSelectionList<MemberList.Entry> {
        MemberList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void setMembers(List<IslandScreenDataMessage.MemberEntry> members) {
            this.clearEntries();
            Entry selected = null;
            for (IslandScreenDataMessage.MemberEntry member : members) {
                Entry entry = new Entry(member);
                this.addEntry(entry);
                if (member.uuid().equals(TeamInfoScreen.this.selectedMemberId)) {
                    selected = entry;
                }
            }
            this.setSelected(selected);
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final IslandScreenDataMessage.MemberEntry member;

            Entry(IslandScreenDataMessage.MemberEntry member) {
                this.member = member;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.member.name());
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                Component name = this.member.leader()
                        ? Component.translatable("haven_skyblock_builder.gui.member_leader", this.member.name()).withStyle(ChatFormatting.GOLD)
                        : Component.literal(this.member.name());
                PlayerHeadRenderer.draw(graphics, this.member.uuid(), this.getContentX() + 4, this.getContentY() + 4, 16);
                graphics.text(TeamInfoScreen.this.font, name, this.getContentX() + 26, this.getContentY() + 7, this.member.leader() ? 0xFFFFAA00 : 0xFFE0E0E0);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    return false;
                }
                MemberList.this.setSelected(this);
                TeamInfoScreen.this.selectMember(this.member);
                return true;
            }
        }
    }

    private class JoinRequestList extends ObjectSelectionList<JoinRequestList.Entry> {
        JoinRequestList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void setRequests(List<IslandScreenDataMessage.JoinRequestEntry> requests) {
            this.clearEntries();
            Entry selected = null;
            for (IslandScreenDataMessage.JoinRequestEntry request : requests) {
                Entry entry = new Entry(request);
                this.addEntry(entry);
                if (request.uuid().equals(TeamInfoScreen.this.selectedRequestId)) {
                    selected = entry;
                }
            }
            this.setSelected(selected);
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final IslandScreenDataMessage.JoinRequestEntry request;

            Entry(IslandScreenDataMessage.JoinRequestEntry request) {
                this.request = request;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.request.name());
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                PlayerHeadRenderer.draw(graphics, this.request.uuid(), this.getContentX() + 4, this.getContentY() + 4, 16);
                graphics.text(TeamInfoScreen.this.font, Component.literal(this.request.name()), this.getContentX() + 26, this.getContentY() + 7, 0xFFE0E0E0);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    return false;
                }
                JoinRequestList.this.setSelected(this);
                TeamInfoScreen.this.selectRequest(this.request);
                return true;
            }
        }
    }
}