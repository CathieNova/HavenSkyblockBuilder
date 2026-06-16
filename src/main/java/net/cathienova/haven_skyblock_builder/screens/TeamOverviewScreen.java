package net.cathienova.haven_skyblock_builder.screens;

import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TeamOverviewScreen extends Screen {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private final IslandScreen previousScreen;
    private final IslandScreenDataMessage.TeamEntry team;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public TeamOverviewScreen(IslandScreen previousScreen, IslandScreenDataMessage.TeamEntry team) {
        super(Component.translatable("haven_skyblock_builder.gui.team_overview"));
        this.previousScreen = previousScreen;
        this.team = team;
    }

    private static String formatDate(long time) {
        return time <= 0 ? "Unknown" : DATE_FORMAT.format(Instant.ofEpochMilli(time));
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(360, this.width - 40);
        int memberHeight = Math.min(7, this.team.memberPlayers().size()) * 17;
        this.panelHeight = Math.min(330, Math.max(248, 226 + memberHeight));
        this.panelHeight = Math.min(this.panelHeight, this.height - 40);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;
        int buttonWidth = 90;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 - buttonWidth / 2, this.panelTop + this.panelHeight - 30, buttonWidth, 20)
                .build());
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
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.previousScreen.extractRenderState(graphics, -1, -1, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + this.panelWidth + 1, this.panelTop + this.panelHeight + 1, 0xFF586171);
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xF010141C);
        graphics.centeredText(this.font, Component.literal(this.team.name()).withStyle(ChatFormatting.LIGHT_PURPLE), this.width / 2, this.panelTop + 16, 0xFFFFFFFF);

        int x = this.panelLeft + 20;
        int y = this.panelTop + 44;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.tooltip_leader", this.team.leaderName()), x, y, 0xFFE0E0E0);
        y += 15;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.template_value", this.team.islandTemplate()), x, y, 0xFFB8B8B8);
        y += 15;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.visits_value", Component.translatable(this.team.allowVisit() ? "haven_skyblock_builder.gui.allowed" : "haven_skyblock_builder.gui.blocked")), x, y, 0xFFB8B8B8);
        y += 15;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.join_value", Component.translatable(this.team.allowJoinRequests() ? "haven_skyblock_builder.gui.allowed" : "haven_skyblock_builder.gui.blocked")), x, y, 0xFFB8B8B8);
        y += 15;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.tooltip_created", formatDate(this.team.createdAt())), x, y, 0xFF888888);
        y += 15;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.tooltip_changed", formatDate(this.team.lastChangedAt())), x, y, 0xFF888888);
        y += 22;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.members_count", this.team.memberCount()), x, y, 0xFFFFFFFF);
        y += 15;

        int limit = Math.min(7, this.team.memberPlayers().size());
        for (int index = 0; index < limit; index++) {
            IslandScreenDataMessage.PlayerEntry member = this.team.memberPlayers().get(index);
            PlayerHeadRenderer.draw(graphics, member.uuid(), x + 8, y - 2, 14);
            graphics.text(this.font, Component.literal(member.name()), x + 28, y + 1, 0xFFB8B8B8);
            y += 17;
        }
        if (this.team.memberPlayers().size() > limit) {
            graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.more_members", this.team.memberPlayers().size() - limit), x + 28, y, 0xFF777777);
        }
        if (this.team.disbanded()) {
            graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.disbanded").withStyle(ChatFormatting.RED), this.panelLeft + this.panelWidth - 84, this.panelTop + 44, 0xFFFFFFFF);
        }
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }
}