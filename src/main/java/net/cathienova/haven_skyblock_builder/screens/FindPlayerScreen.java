package net.cathienova.haven_skyblock_builder.screens;

import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Objects;

public class FindPlayerScreen extends Screen {
    private final TeamInfoScreen previousScreen;
    private final List<IslandScreenDataMessage.PlayerEntry> players;
    private PlayerList playerList;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;

    public FindPlayerScreen(TeamInfoScreen previousScreen, List<IslandScreenDataMessage.PlayerEntry> players) {
        super(Component.translatable("haven_skyblock_builder.gui.invite_player"));
        this.previousScreen = previousScreen;
        this.players = List.copyOf(players);
    }

    @Override
    protected void init() {
        this.panelWidth = Math.min(320, this.width - 40);
        this.panelHeight = Math.min(300, this.height - 40);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        int listTop = this.panelTop + 42;
        int buttonY = this.panelTop + this.panelHeight - 30;
        int listHeight = Math.max(60, buttonY - listTop - 8);
        this.playerList = new PlayerList(Objects.requireNonNull(this.minecraft), this.panelWidth - 28, listHeight, listTop, 28);
        this.playerList.setX(this.panelLeft + 14);
        this.playerList.setPlayers(this.players);
        this.addWidget(this.playerList);

        int cancelWidth = 90;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(this.width / 2 - cancelWidth / 2, buttonY, cancelWidth, 20)
                .build());
    }

    private void invite(IslandScreenDataMessage.PlayerEntry player) {
        this.previousScreen.invitePlayer(player);
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
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
        graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 14, 0xFFFFFFFF);

        if (this.players.isEmpty()) {
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.no_players_online"), this.width / 2, this.panelTop + this.panelHeight / 2, 0xFF777777);
        } else if (this.playerList != null) {
            this.playerList.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }

    private class PlayerList extends ObjectSelectionList<PlayerList.Entry> {
        PlayerList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void setPlayers(List<IslandScreenDataMessage.PlayerEntry> players) {
            this.clearEntries();
            for (IslandScreenDataMessage.PlayerEntry player : players) {
                this.addEntry(new Entry(player));
            }
            this.setSelected(null);
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

        private class Entry extends ObjectSelectionList.Entry<Entry> {
            private final IslandScreenDataMessage.PlayerEntry player;

            Entry(IslandScreenDataMessage.PlayerEntry player) {
                this.player = player;
            }

            @Override
            public Component getNarration() {
                return Component.literal(this.player.name());
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int x = this.getContentX() + 6;
                int y = this.getContentY() + 4;
                if (hovered) {
                    graphics.fill(this.getContentX() + 2, this.getContentY() + 2, this.getContentX() + PlayerList.this.getRowWidth() - 2, this.getContentY() + 26, 0xFF202934);
                }
                PlayerHeadRenderer.draw(graphics, this.player.uuid(), x, y, 18);
                graphics.text(FindPlayerScreen.this.font, Component.literal(this.player.name()), x + 24, y + 5, 0xFFE0E0E0);
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
                FindPlayerScreen.this.invite(this.player);
                return true;
            }
        }
    }
}