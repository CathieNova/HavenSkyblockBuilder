package net.cathienova.haven_skyblock_builder.screens;

import net.cathienova.haven_skyblock_builder.networking.IslandScreenActionMessage;
import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.cathienova.haven_skyblock_builder.networking.IslandScreenRequestMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class IslandScreen extends Screen {
    private static final Component TITLE = Component.translatable("haven_skyblock_builder.gui.screen");
    private IslandScreenDataMessage data = emptyData();
    private boolean loading = true;
    private boolean visitOnly;
    private boolean joinOnly;
    private boolean hideEmpty;
    private boolean showDisbanded;
    private TeamSort teamSort = TeamSort.NAME;
    private List<IslandScreenDataMessage.TeamEntry> visibleTeams = List.of();
    private TeamList teamList;
    private Button filtersButton;
    private Button primaryButton;
    private Button homeButton;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public IslandScreen() {
        super(TITLE);
    }

    private static IslandScreenDataMessage emptyData() {
        return new IslandScreenDataMessage(false, false, false, false, false, false, false, "", "", "", false, false, "", 0, 0, 0, 0, 0, List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void init() {
        this.buildWidgets();
        this.requestData();
    }

    private void buildWidgets() {
        this.clearWidgets();
        this.panelWidth = Math.max(360, Math.min(760, this.width * 76 / 100));
        this.panelHeight = Math.max(230, Math.min(430, this.height * 82 / 100));
        this.panelWidth = Math.min(this.panelWidth, this.width - 24);
        this.panelHeight = Math.min(this.panelHeight, this.height - 24);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int contentLeft = this.panelLeft + 14;
        int contentWidth = this.panelWidth - 28;
        int topButtonY = this.panelTop + 30;
        int primaryWidth = this.data.hasTeam() ? 118 : 126;
        int filtersWidth = 82;

        this.addRenderableWidget(Button.builder(Component.literal("X"), button ->
                        {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(null);
                            }
                        })
                .bounds(this.panelLeft + this.panelWidth - 26, this.panelTop + 8, 18, 18)
                .build());

        this.primaryButton = this.addRenderableWidget(Button.builder(
                        Component.translatable(this.data.hasTeam() ? "haven_skyblock_builder.gui.team_settings" : "haven_skyblock_builder.gui.create_team"),
                        button ->
                        {
                            if (this.data.hasTeam()) {
                                this.openTeamSettings();
                            } else {
                                this.openCreateTeam();
                            }
                        })
                .bounds(this.panelLeft + this.panelWidth - primaryWidth - 14, topButtonY, primaryWidth, 20)
                .build());

        this.filtersButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.filters"), button -> this.openFilters())
                .bounds(this.primaryButton.getX() - filtersWidth - 6, topButtonY, filtersWidth, 20)
                .build());

        int listTop = this.panelTop + (this.data.hasTeam() ? 74 : 62);
        int bottomY = this.panelTop + this.panelHeight - 30;
        int listHeight = Math.max(90, bottomY - listTop - 8);
        this.teamList = new TeamList(Objects.requireNonNull(this.minecraft), contentWidth, listHeight, listTop, 30);
        this.teamList.setX(contentLeft);
        this.addWidget(this.teamList);

        int gap = 8;
        int homeWidth = 118;
        int spawnWidth = 142;
        int totalWidth = this.data.hasTeam() ? homeWidth + gap + spawnWidth : spawnWidth;
        int buttonX = this.panelLeft + (this.panelWidth - totalWidth) / 2;

        if (this.data.hasTeam()) {
            this.homeButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.home_island"), button -> this.runCommand("havensb island home"))
                    .bounds(buttonX, bottomY, homeWidth, 20)
                    .build());
            buttonX += homeWidth + gap;
        }

        this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.teleport_spawn"), button -> this.runCommand("havensb spawn"))
                .bounds(buttonX, bottomY, spawnWidth, 20)
                .build());

        this.refreshTeamList();
        this.updateButtonStates();
    }

    public void updateData(IslandScreenDataMessage message) {
        boolean teamStateChanged = this.data.hasTeam() != message.hasTeam();
        this.data = message;
        this.loading = false;
        if (teamStateChanged && this.minecraft != null && this.minecraft.screen == this) {
            this.buildWidgets();
            return;
        }
        this.refreshTeamList();
        this.updateButtonStates();
    }

    public IslandScreenDataMessage getData() {
        return this.data;
    }

    boolean isVisitOnly() {
        return this.visitOnly;
    }

    boolean isJoinOnly() {
        return this.joinOnly;
    }

    boolean isHideEmpty() {
        return this.hideEmpty;
    }

    boolean isShowDisbanded() {
        return this.showDisbanded;
    }

    TeamSort getTeamSort() {
        return this.teamSort;
    }

    void applyFilters(boolean visitOnly, boolean joinOnly, boolean hideEmpty, boolean showDisbanded, TeamSort teamSort) {
        this.visitOnly = visitOnly;
        this.joinOnly = joinOnly;
        this.hideEmpty = hideEmpty;
        this.showDisbanded = showDisbanded;
        this.teamSort = teamSort;
        this.refreshTeamList();
    }

    private void requestData() {
        this.loading = true;
        this.updateButtonStates();
        ClientPacketDistributor.sendToServer(IslandScreenRequestMessage.INSTANCE);
    }

    private void openFilters() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new TeamFilterScreen(this));
        }
    }

    private void openCreateTeam() {
        if (this.minecraft != null && !this.data.hasTeam()) {
            this.minecraft.setScreen(new CreateTeamScreen(this, this.data));
        }
    }

    private void openTeamSettings() {
        if (this.minecraft != null && this.data.hasTeam()) {
            this.minecraft.setScreen(new TeamInfoScreen(this, this.data));
        }
    }

    private void openTeamOverview(IslandScreenDataMessage.TeamEntry team) {
        if (this.minecraft != null) {
            this.minecraft.setScreen(new TeamOverviewScreen(this, team));
        }
    }

    private void runCommand(String command) {
        if (this.minecraft != null && this.minecraft.getConnection() != null) {
            this.minecraft.getConnection().sendCommand(command);
            this.minecraft.setScreen(null);
        }
    }

    private void visitTeam(IslandScreenDataMessage.TeamEntry team) {
        if (!team.allowVisit() || team.ownTeam() || team.disbanded()) {
            return;
        }
        this.runCommand("havensb team visit " + team.name());
    }

    private void requestJoin(IslandScreenDataMessage.TeamEntry team) {
        if (this.data.hasTeam() || !team.allowJoinRequests() || team.disbanded()) {
            return;
        }
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.requestJoin(team.uuid()));
    }

    private void refreshTeamList() {
        List<IslandScreenDataMessage.TeamEntry> teams = new ArrayList<>();
        for (IslandScreenDataMessage.TeamEntry team : this.data.teams()) {
            if (!this.showDisbanded && team.disbanded()) {
                continue;
            }
            if (this.visitOnly && !team.allowVisit()) {
                continue;
            }
            if (this.joinOnly && !team.allowJoinRequests()) {
                continue;
            }
            if (this.hideEmpty && team.memberCount() == 0) {
                continue;
            }
            teams.add(team);
        }
        teams.sort(this.teamSort.comparator());
        this.visibleTeams = List.copyOf(teams);
        if (this.teamList != null) {
            this.teamList.setTeams(this.visibleTeams);
        }
    }

    private void updateButtonStates() {
        if (this.primaryButton != null) {
            this.primaryButton.active = !this.loading;
        }
        if (this.filtersButton != null) {
            this.filtersButton.active = !this.loading;
        }
        if (this.homeButton != null) {
            this.homeButton.active = !this.loading && this.data.hasTeam();
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (!handled || this.getFocused() instanceof Button) {
            this.setFocused(null);
        }
        return handled;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + this.panelWidth + 1, this.panelTop + this.panelHeight + 1, 0xFF586171);
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xF010141C);
        graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 12, 0xFFFFFFFF);

        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.teams_count", this.visibleTeams.size()), this.panelLeft + 16, this.panelTop + 36, 0xFFE0E0E0);
        if (this.data.hasTeam()) {
            Component teamLine = Component.translatable("haven_skyblock_builder.gui.your_team", Component.literal(this.data.teamName()).withStyle(ChatFormatting.LIGHT_PURPLE));
            graphics.text(this.font, teamLine, this.panelLeft + 16, this.panelTop + 52, 0xFFFFFFFF);
        }

        if (this.loading) {
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.loading"), this.width / 2, this.panelTop + this.panelHeight / 2, 0xFFAAAAAA);
        } else if (this.visibleTeams.isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.no_teams_match"), this.width / 2, this.panelTop + this.panelHeight / 2, 0xFF888888);
        }

        if (this.teamList != null) {
            this.teamList.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }

    private String fitText(String text, int maximumWidth) {
        if (this.font.width(text) <= maximumWidth) {
            return text;
        }
        String suffix = "...";
        String shortened = text;
        while (!shortened.isEmpty() && this.font.width(shortened + suffix) > maximumWidth) {
            shortened = shortened.substring(0, shortened.length() - 1);
        }
        return shortened + suffix;
    }

    private List<Component> teamTooltip(IslandScreenDataMessage.TeamEntry team) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(team.name()).withStyle(team.ownTeam() ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.WHITE));
        lines.add(Component.translatable("haven_skyblock_builder.gui.tooltip_leader", team.leaderName()));
        lines.add(Component.translatable("haven_skyblock_builder.gui.tooltip_members", team.memberCount()));
        int limit = Math.min(8, team.memberPlayers().size());
        for (int index = 0; index < limit; index++) {
            lines.add(Component.literal("  " + team.memberPlayers().get(index).name()).withStyle(ChatFormatting.GRAY));
        }
        if (team.memberPlayers().size() > limit) {
            lines.add(Component.translatable("haven_skyblock_builder.gui.more_members", team.memberPlayers().size() - limit).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (team.disbanded()) {
            lines.add(Component.translatable("haven_skyblock_builder.gui.disbanded").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    private void drawRowButton(GuiGraphicsExtractor graphics, int x, int y, int width, int height, Component text, boolean active, int mouseX, int mouseY) {
        boolean hovered = active && inside(mouseX, mouseY, x, y, width, height);
        int border = active ? hovered ? 0xFFFFFFFF : 0xFF8A8A8A : 0xFF454545;
        int fill = active ? hovered ? 0xFF777777 : 0xFF555555 : 0xFF252525;
        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        graphics.centeredText(this.font, text, x + width / 2, y + 6, active ? 0xFFFFFFFF : 0xFF777777);
    }

    enum TeamSort {
        NAME("haven_skyblock_builder.gui.sort_name", Comparator.comparing(IslandScreenDataMessage.TeamEntry::name, String.CASE_INSENSITIVE_ORDER)),
        MEMBERS_HIGH("haven_skyblock_builder.gui.sort_members_high", Comparator.<IslandScreenDataMessage.TeamEntry>comparingInt(IslandScreenDataMessage.TeamEntry::memberCount).reversed().thenComparing(IslandScreenDataMessage.TeamEntry::name, String.CASE_INSENSITIVE_ORDER)),
        MEMBERS_LOW("haven_skyblock_builder.gui.sort_members_low", Comparator.<IslandScreenDataMessage.TeamEntry>comparingInt(IslandScreenDataMessage.TeamEntry::memberCount).thenComparing(IslandScreenDataMessage.TeamEntry::name, String.CASE_INSENSITIVE_ORDER)),
        NEWEST("haven_skyblock_builder.gui.sort_newest", Comparator.comparingLong(IslandScreenDataMessage.TeamEntry::createdAt).reversed());

        private final String translationKey;
        private final Comparator<IslandScreenDataMessage.TeamEntry> comparator;

        TeamSort(String translationKey, Comparator<IslandScreenDataMessage.TeamEntry> comparator) {
            this.translationKey = translationKey;
            this.comparator = comparator;
        }

        String translationKey() {
            return this.translationKey;
        }

        Comparator<IslandScreenDataMessage.TeamEntry> comparator() {
            return this.comparator;
        }

        TeamSort next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    class TeamList extends ObjectSelectionList<TeamList.Entry> {
        TeamList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void setTeams(List<IslandScreenDataMessage.TeamEntry> teams) {
            this.clearEntries();
            for (IslandScreenDataMessage.TeamEntry team : teams) {
                this.addEntry(new Entry(team));
            }
            this.setSelected(null);
            this.setScrollAmount(0);
        }

        @Override
        public void setSelected(Entry entry) {
            super.setSelected(null);
        }

        @Override
        protected int scrollBarX() {
            return this.getX() + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 12;
        }

        class Entry extends ObjectSelectionList.Entry<Entry> {
            private final IslandScreenDataMessage.TeamEntry team;

            Entry(IslandScreenDataMessage.TeamEntry team) {
                this.team = team;
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.literal(this.team.name()));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int left = this.getContentX() + 6;
                int top = this.getContentY() + 7;
                int rowWidth = TeamList.this.getRowWidth();
                int infoWidth = 52;
                int visitWidth = 54;
                int joinWidth = this.canJoin() ? 62 : 0;
                int buttonGap = 5;
                int buttonsWidth = infoWidth + visitWidth + joinWidth + buttonGap * (joinWidth > 0 ? 2 : 1);
                int textWidth = rowWidth - buttonsWidth - 18;
                int nameColor = this.team.disbanded() ? 0xFF777777 : this.team.ownTeam() ? 0xFFCC77FF : 0xFFEAEAEA;
                graphics.text(IslandScreen.this.font, Component.literal(IslandScreen.this.fitText(this.team.name() + " - " + this.team.memberCount(), textWidth)), left, top, nameColor);

                int infoLeft = this.getContentX() + rowWidth - buttonsWidth - 4;
                int visitLeft = infoLeft + infoWidth + buttonGap;
                int joinLeft = visitLeft + visitWidth + buttonGap;
                drawRowButton(graphics, infoLeft, this.getContentY() + 4, infoWidth, 20, Component.translatable("haven_skyblock_builder.gui.info"), true, mouseX, mouseY);
                drawRowButton(graphics, visitLeft, this.getContentY() + 4, visitWidth, 20, Component.translatable("haven_skyblock_builder.gui.visit"), this.canVisit(), mouseX, mouseY);
                if (joinWidth > 0) {
                    drawRowButton(graphics, joinLeft, this.getContentY() + 4, joinWidth, 20, Component.translatable("haven_skyblock_builder.gui.req_join"), this.canJoin(), mouseX, mouseY);
                }

                if (hovered && mouseX < infoLeft) {
                    graphics.setComponentTooltipForNextFrame(IslandScreen.this.font, IslandScreen.this.teamTooltip(this.team), mouseX, mouseY);
                }
            }

            private boolean canVisit() {
                return !this.team.disbanded() && !this.team.ownTeam() && this.team.allowVisit();
            }

            private boolean canJoin() {
                return !this.team.disbanded() && !IslandScreen.this.data.hasTeam() && this.team.allowJoinRequests();
            }

            @Override
            public boolean isFocused() {
                return false;
            }

            @Override
            public void setFocused(boolean focused) {
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    return false;
                }
                int rowWidth = TeamList.this.getRowWidth();
                int infoWidth = 52;
                int visitWidth = 54;
                int joinWidth = this.canJoin() ? 62 : 0;
                int buttonGap = 5;
                int buttonsWidth = infoWidth + visitWidth + joinWidth + buttonGap * (joinWidth > 0 ? 2 : 1);
                int infoLeft = this.getContentX() + rowWidth - buttonsWidth - 4;
                int visitLeft = infoLeft + infoWidth + buttonGap;
                int joinLeft = visitLeft + visitWidth + buttonGap;
                if (inside(event.x(), event.y(), infoLeft, this.getContentY() + 4, infoWidth, 20)) {
                    IslandScreen.this.openTeamOverview(this.team);
                    return true;
                }
                if (inside(event.x(), event.y(), visitLeft, this.getContentY() + 4, visitWidth, 20) && this.canVisit()) {
                    IslandScreen.this.visitTeam(this.team);
                    return true;
                }
                if (joinWidth > 0 && inside(event.x(), event.y(), joinLeft, this.getContentY() + 4, joinWidth, 20) && this.canJoin()) {
                    IslandScreen.this.requestJoin(this.team);
                    return true;
                }
                return false;
            }
        }
    }
}