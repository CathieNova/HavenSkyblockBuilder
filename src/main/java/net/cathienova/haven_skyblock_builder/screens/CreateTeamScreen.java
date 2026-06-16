package net.cathienova.haven_skyblock_builder.screens;

import net.cathienova.haven_skyblock_builder.networking.IslandScreenActionMessage;
import net.cathienova.haven_skyblock_builder.networking.IslandScreenDataMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class CreateTeamScreen extends Screen {
    private static final Component TITLE = Component.translatable("haven_skyblock_builder.gui.create_team");
    private final IslandScreen previousScreen;
    private IslandScreenDataMessage data;
    private int selectedTemplateIndex;
    private boolean allowVisit = true;
    private boolean allowJoinRequests;
    private boolean submitting;
    private boolean draggingPreview;
    private String teamName = "";
    private float previewYaw = 25.0F;
    private float previewPitch = 25.0F;
    private float previewZoom = 1.0F;
    private EditBox nameInput;
    private Button previousTemplateButton;
    private Button nextTemplateButton;
    private Button templateButton;
    private HavenCheckbox visitCheckbox;
    private HavenCheckbox joinCheckbox;
    private Button createButton;
    private int panelLeft;
    private int panelTop;
    private int panelWidth;
    private int panelHeight;
    private int previewLeft;
    private int previewTop;
    private int previewWidth;
    private int previewHeight;
    private List<StructurePreviewRenderState.PreviewBlock> previewBlocks = List.of();

    public CreateTeamScreen(IslandScreen previousScreen, IslandScreenDataMessage data) {
        super(TITLE);
        this.previousScreen = previousScreen;
        this.data = data;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.panelWidth = Math.max(420, Math.min(760, this.width * 78 / 100));
        this.panelHeight = Math.max(270, Math.min(430, this.height * 84 / 100));
        this.panelWidth = Math.min(this.panelWidth, this.width - 24);
        this.panelHeight = Math.min(this.panelHeight, this.height - 24);
        this.panelLeft = (this.width - this.panelWidth) / 2;
        this.panelTop = (this.height - this.panelHeight) / 2;

        this.previewLeft = this.panelLeft + 16;
        this.previewTop = this.panelTop + 42;
        this.previewWidth = Math.max(150, this.panelWidth * 44 / 100 - 20);
        this.previewHeight = this.panelHeight - 88;

        int formLeft = this.previewLeft + this.previewWidth + 22;
        int formWidth = this.panelLeft + this.panelWidth - 16 - formLeft;
        int y = this.panelTop + 60;

        this.nameInput = new EditBox(this.font, formLeft, y, formWidth, 20, Component.translatable("haven_skyblock_builder.gui.team_name"));
        this.nameInput.setMaxLength(64);
        this.nameInput.setHint(Component.translatable("haven_skyblock_builder.gui.team_name_hint"));
        this.nameInput.setValue(this.teamName);
        this.nameInput.setResponder(value -> this.teamName = value);
        this.addRenderableWidget(this.nameInput);
        y += 48;

        int arrowWidth = 28;
        this.previousTemplateButton = this.addRenderableWidget(Button.builder(Component.literal("<"), button -> this.changeTemplate(-1))
                .bounds(formLeft, y, arrowWidth, 20).build());
        this.templateButton = this.addRenderableWidget(Button.builder(this.templateText(), button -> this.changeTemplate(1))
                .bounds(formLeft + arrowWidth + 5, y, Math.max(70, formWidth - arrowWidth * 2 - 10), 20).build());
        this.nextTemplateButton = this.addRenderableWidget(Button.builder(Component.literal(">"), button -> this.changeTemplate(1))
                .bounds(formLeft + formWidth - arrowWidth, y, arrowWidth, 20).build());
        y += 38;

        int checkboxLeft = formLeft + Math.max(0, (formWidth - 190) / 2);
        this.visitCheckbox = this.addRenderableWidget(new HavenCheckbox(
                checkboxLeft,
                y,
                190,
                this.allowVisit,
                Component.translatable("haven_skyblock_builder.gui.allow_visits"),
                selected -> this.allowVisit = selected
        ));
        y += 28;
        this.joinCheckbox = this.addRenderableWidget(new HavenCheckbox(
                checkboxLeft,
                y,
                190,
                this.allowJoinRequests,
                Component.translatable("haven_skyblock_builder.gui.allow_join_requests"),
                selected -> this.allowJoinRequests = selected
        ));

        int bottomY = this.panelTop + this.panelHeight - 30;
        int buttonWidth = 96;
        int buttonGap = 8;
        int buttonsLeft = formLeft + (formWidth - buttonWidth * 2 - buttonGap) / 2;
        this.createButton = this.addRenderableWidget(Button.builder(Component.translatable("haven_skyblock_builder.gui.create"), button -> this.createTeam())
                .bounds(buttonsLeft, bottomY, buttonWidth, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> this.onClose())
                .bounds(buttonsLeft + buttonWidth + buttonGap, bottomY, buttonWidth, 20).build());

        this.updateTemplateControls();
        this.updateCreateButton();
    }

    public void updateData(IslandScreenDataMessage message) {
        this.data = message;
        this.previousScreen.updateData(message);
        if (message.hasTeam()) {
            if (this.minecraft != null) {
                this.minecraft.setScreen(null);
            }
            return;
        }
        this.submitting = false;
        this.selectedTemplateIndex = Math.min(this.selectedTemplateIndex, Math.max(0, this.data.templates().size() - 1));
        this.updateTemplateControls();
        this.updateCreateButton();
    }

    private void changeTemplate(int direction) {
        if (this.data.templates().isEmpty()) {
            return;
        }
        this.selectedTemplateIndex = Math.floorMod(this.selectedTemplateIndex + direction, this.data.templates().size());
        this.previewYaw = 25.0F;
        this.previewPitch = 25.0F;
        this.previewZoom = 1.0F;
        this.updateTemplateControls();
    }

    private void updateTemplateControls() {
        boolean hasTemplates = !this.data.templates().isEmpty();
        if (this.previousTemplateButton != null) {
            this.previousTemplateButton.active = hasTemplates && this.data.templates().size() > 1;
            this.nextTemplateButton.active = hasTemplates && this.data.templates().size() > 1;
            this.templateButton.active = hasTemplates;
            this.templateButton.setMessage(this.templateText());
        }
        this.buildPreviewModels();
        this.updateCreateButton();
    }

    private void buildPreviewModels() {
        IslandScreenDataMessage.TemplateEntry template = this.selectedTemplate();
        if (template == null || template.blocks().isEmpty()) {
            this.previewBlocks = List.of();
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        BlockDisplayContext displayContext = BlockDisplayContext.create();
        List<BlockModelRenderState> paletteModels = new ArrayList<>(template.palette().size());

        for (String blockStateText : template.palette()) {
            try {
                BlockState blockState = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK, blockStateText, false).blockState();
                BlockModelRenderState renderState = new BlockModelRenderState();
                minecraft.getBlockModelResolver().update(renderState, blockState, displayContext);
                paletteModels.add(renderState);
            } catch (Exception e) {
                paletteModels.add(null);
            }
        }

        List<StructurePreviewRenderState.PreviewBlock> blocks = new ArrayList<>(template.blocks().size());
        for (IslandScreenDataMessage.PreviewBlock block : template.blocks()) {
            if (block.paletteIndex() < 0 || block.paletteIndex() >= paletteModels.size()) {
                continue;
            }
            BlockModelRenderState renderState = paletteModels.get(block.paletteIndex());
            if (renderState != null) {
                blocks.add(new StructurePreviewRenderState.PreviewBlock(renderState, block.x(), block.y(), block.z()));
            }
        }
        this.previewBlocks = List.copyOf(blocks);
    }

    private void createTeam() {
        IslandScreenDataMessage.TemplateEntry template = this.selectedTemplate();
        String name = this.teamName.trim();
        if (template == null || name.isEmpty() || this.submitting) {
            return;
        }
        this.submitting = true;
        this.updateCreateButton();
        ClientPacketDistributor.sendToServer(IslandScreenActionMessage.createTeam(name, template.name(), this.allowVisit, this.allowJoinRequests));
    }

    private void updateCreateButton() {
        if (this.createButton != null && this.nameInput != null) {
            this.createButton.active = !this.submitting && !this.teamName.trim().isEmpty() && this.selectedTemplate() != null;
            this.createButton.setMessage(Component.translatable(this.submitting ? "haven_skyblock_builder.gui.creating" : "haven_skyblock_builder.gui.create"));
        }
    }

    private IslandScreenDataMessage.TemplateEntry selectedTemplate() {
        if (this.data.templates().isEmpty() || this.selectedTemplateIndex < 0 || this.selectedTemplateIndex >= this.data.templates().size()) {
            return null;
        }
        return this.data.templates().get(this.selectedTemplateIndex);
    }

    private Component templateText() {
        IslandScreenDataMessage.TemplateEntry template = this.selectedTemplate();
        return template == null ? Component.translatable("haven_skyblock_builder.gui.no_templates") : Component.literal(template.name());
    }

    @Override
    public void tick() {
        this.updateCreateButton();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.insidePreview(event.x(), event.y())) {
            this.draggingPreview = true;
            this.setFocused(null);
            return true;
        }
        boolean handled = super.mouseClicked(event, doubleClick);
        if (!handled || this.getFocused() instanceof Button || this.getFocused() instanceof HavenCheckbox) {
            this.setFocused(null);
        }
        return handled;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (this.draggingPreview && event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.previewYaw += (float) deltaX * 0.7F;
            this.previewPitch = Math.max(-85.0F, Math.min(85.0F, this.previewPitch + (float) deltaY * 0.55F));
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.draggingPreview) {
            this.draggingPreview = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (this.insidePreview(mouseX, mouseY) && scrollY != 0.0D) {
            this.previewZoom = Math.max(0.35F, Math.min(3.0F, this.previewZoom + (float) scrollY * 0.12F));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean insidePreview(double x, double y) {
        return x >= this.previewLeft && x < this.previewLeft + this.previewWidth && y >= this.previewTop && y < this.previewTop + this.previewHeight;
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
        graphics.centeredText(this.font, this.title, this.width / 2, this.panelTop + 14, 0xFFFFFFFF);

        graphics.fill(this.previewLeft - 1, this.previewTop - 1, this.previewLeft + this.previewWidth + 1, this.previewTop + this.previewHeight + 1, 0xFF4D5563);
        graphics.fill(this.previewLeft, this.previewTop, this.previewLeft + this.previewWidth, this.previewTop + this.previewHeight, 0xFF171C25);
        graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.island_preview"), this.previewLeft + this.previewWidth / 2, this.previewTop + 8, 0xFFDDDDDD);

        IslandScreenDataMessage.TemplateEntry template = this.selectedTemplate();
        if (template != null) {
            graphics.centeredText(this.font, Component.literal(template.name()), this.previewLeft + this.previewWidth / 2, this.previewTop + 22, 0xFFCC77FF);
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.template_size", template.sizeX(), template.sizeY(), template.sizeZ()), this.previewLeft + this.previewWidth / 2, this.previewTop + 34, 0xFF888888);
            this.renderTemplatePreview(graphics, template);
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.preview_controls"), this.previewLeft + this.previewWidth / 2, this.previewTop + this.previewHeight - 14, 0xFF8F98A8);
        } else {
            graphics.centeredText(this.font, Component.translatable("haven_skyblock_builder.gui.no_templates"), this.previewLeft + this.previewWidth / 2, this.previewTop + this.previewHeight / 2, 0xFF888888);
        }

        int formLeft = this.previewLeft + this.previewWidth + 22;
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.team_name"), formLeft, this.nameInput.getY() - 13, 0xFFDDDDDD);
        graphics.text(this.font, Component.translatable("haven_skyblock_builder.gui.template"), formLeft, this.previousTemplateButton.getY() - 13, 0xFFDDDDDD);
        this.renderables.forEach(renderable -> renderable.extractRenderState(graphics, mouseX, mouseY, partialTick));
    }

    private void renderTemplatePreview(GuiGraphicsExtractor graphics, IslandScreenDataMessage.TemplateEntry template) {
        if (this.previewBlocks.isEmpty()) {
            return;
        }

        int x0 = this.previewLeft + 4;
        int y0 = this.previewTop + 46;
        int x1 = this.previewLeft + this.previewWidth - 4;
        int y1 = this.previewTop + this.previewHeight - 22;
        float diagonal = (float) Math.sqrt(
                template.sizeX() * template.sizeX()
                        + template.sizeY() * template.sizeY()
                        + template.sizeZ() * template.sizeZ()
        );
        float fitScale = Math.min(x1 - x0, y1 - y0) * 0.72F / Math.max(1.0F, diagonal);

        graphics.submitPictureInPictureRenderState(new StructurePreviewRenderState(
                this.previewBlocks,
                this.previewYaw,
                this.previewPitch,
                (template.sizeX() - 1) / 2.0F,
                (template.sizeY() - 1) / 2.0F,
                (template.sizeZ() - 1) / 2.0F,
                x0,
                y0,
                x1,
                y1,
                fitScale * this.previewZoom,
                graphics.peekScissorStack()
        ));
    }
}