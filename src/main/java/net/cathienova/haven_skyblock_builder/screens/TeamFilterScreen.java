package net.cathienova.haven_skyblock_builder.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class TeamFilterScreen extends Screen {
    private final IslandScreen previousScreen;
    private boolean visitOnly;
    private boolean joinOnly;
    private boolean hideEmpty;
    private boolean showDisbanded;
    private IslandScreen.TeamSort teamSort;
    private HavenCheckbox visitCheckbox;
    private HavenCheckbox joinCheckbox;
    private HavenCheckbox emptyCheckbox;
    private HavenCheckbox disbandedCheckbox;
    private Button sortButton;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public TeamFilterScreen(IslandScreen previousScreen) {
        super(Component.translatable("haven_skyblock_builder.gui.filters"));
        this.previousScreen = previousScreen;
        this.visitOnly = previousScreen.isVisitOnly();
        this.joinOnly = previousScreen.isJoinOnly();
        this.hideEmpty = previousScreen.isHideEmpty();
        this.showDisbanded = previousScreen.isShowDisbanded();
        this.teamSort = previousScreen.getTeamSort();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.panelWidth = Math.min(300, this.width - 40);
        this.panelHeight = Math.min(238, this.height - 40);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;
        int left = this.panelLeft + 40;
        int y = this.panelTop + 40;

        this.visitCheckbox = this.addRenderableWidget(new HavenCheckbox(
                left,
                y,
                this.panelWidth - 80,
                this.visitOnly,
                Component.translatable("haven_skyblock_builder.gui.filter_visits"),
                selected -> this.visitOnly = selected
        ));
        y += 29;
        this.joinCheckbox = this.addRenderableWidget(new HavenCheckbox(
                left,
                y,
                this.panelWidth - 80,
                this.joinOnly,
                Component.translatable("haven_skyblock_builder.gui.filter_join"),
                selected -> this.joinOnly = selected
        ));
        y += 29;
        this.emptyCheckbox = this.addRenderableWidget(new HavenCheckbox(
                left,
                y,
                this.panelWidth - 80,
                this.hideEmpty,
                Component.translatable("haven_skyblock_builder.gui.filter_hide_empty"),
                selected -> this.hideEmpty = selected
        ));
        y += 29;
        this.disbandedCheckbox = this.addRenderableWidget(new HavenCheckbox(
                left,
                y,
                this.panelWidth - 80,
                this.showDisbanded,
                Component.translatable("haven_skyblock_builder.gui.filter_disbanded"),
                selected -> this.showDisbanded = selected
        ));
        y += 33;

        int sortWidth = Math.min(214, this.panelWidth - 36);
        this.sortButton = this.addRenderableWidget(Button.builder(this.sortText(), button ->
        {
            this.teamSort = this.teamSort.next();
            this.sortButton.setMessage(this.sortText());
        }).bounds(this.width / 2 - sortWidth / 2, y, sortWidth, 20).build());

        int bottom = this.panelTop + this.panelHeight - 30;
        int buttonWidth = 90;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> this.applyAndClose())
                .bounds(this.width / 2 - buttonWidth / 2, bottom, buttonWidth, 20).build());
    }

    private Component sortText() {
        return Component.translatable("haven_skyblock_builder.gui.sort", Component.translatable(this.teamSort.translationKey()));
    }

    private void applyAndClose() {
        this.previousScreen.applyFilters(this.visitOnly, this.joinOnly, this.hideEmpty, this.showDisbanded, this.teamSort);
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
    }

    @Override
    public void onClose() {
        this.applyAndClose();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean handled = super.mouseClicked(event, doubleClick);
        if (!handled || this.getFocused() instanceof Button || this.getFocused() instanceof HavenCheckbox) {
            this.setFocused(null);
        }
        return handled;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.previousScreen.extractRenderState(graphics, -1, -1, partialTick);
        graphics.fill(0, 0, this.width, this.height, 0x66000000);
        graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + this.panelWidth + 1, this.panelTop + this.panelHeight + 1, 0xFF697383);
        graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + this.panelWidth, this.panelTop + this.panelHeight, 0xFF131923);
        graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 14, 0xFFFFFFFF);
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }
}