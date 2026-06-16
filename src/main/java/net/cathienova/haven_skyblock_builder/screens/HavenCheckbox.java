package net.cathienova.haven_skyblock_builder.screens;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class HavenCheckbox extends AbstractWidget {
    private static final int BOX_SIZE = 12;
    private final Consumer<Boolean> changed;
    private boolean selected;

    public HavenCheckbox(int x, int y, int width, boolean selected, Component label, Consumer<Boolean> changed) {
        super(x, y, width, 14, label);
        this.selected = selected;
        this.changed = changed;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        if (!this.active) {
            return;
        }
        this.selected = !this.selected;
        this.changed.accept(this.selected);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int boxY = this.getY() + 1;
        int borderColor = !this.active ? 0xFF555A62 : this.isHovered() ? 0xFFFFFFFF : 0xFF9EA6B2;
        int fillColor = this.active ? 0xFF090C11 : 0xFF171A20;
        int textColor = this.active ? 0xFFDDDDDD : 0xFF777777;

        graphics.fill(this.getX(), boxY, this.getX() + BOX_SIZE, boxY + BOX_SIZE, borderColor);
        graphics.fill(this.getX() + 1, boxY + 1, this.getX() + BOX_SIZE - 1, boxY + BOX_SIZE - 1, fillColor);

        if (this.selected) {
            int selectedColor = this.active ? 0xFF6F3A83 : 0xFF403646;
            int checkColor = this.active ? 0xFFFFFFFF : 0xFF8A8A8A;
            graphics.fill(this.getX() + 2, boxY + 2, this.getX() + BOX_SIZE - 2, boxY + BOX_SIZE - 2, selectedColor);
            graphics.fill(this.getX() + 2, boxY + 5, this.getX() + 3, boxY + 6, checkColor);
            graphics.fill(this.getX() + 3, boxY + 6, this.getX() + 4, boxY + 7, checkColor);
            graphics.fill(this.getX() + 4, boxY + 7, this.getX() + 5, boxY + 8, checkColor);
            graphics.fill(this.getX() + 5, boxY + 6, this.getX() + 6, boxY + 7, checkColor);
            graphics.fill(this.getX() + 6, boxY + 5, this.getX() + 7, boxY + 6, checkColor);
            graphics.fill(this.getX() + 7, boxY + 4, this.getX() + 8, boxY + 5, checkColor);
            graphics.fill(this.getX() + 8, boxY + 3, this.getX() + 9, boxY + 4, checkColor);
        }

        graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + BOX_SIZE + 6, this.getY() + 3, textColor);

        if (this.isHovered() && this.active) {
            graphics.requestCursor(CursorTypes.POINTING_HAND);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}