package me.akar1881.sre.gui;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.counter.PartyCounterWidget;
import me.akar1881.sre.counter.PartySlayerCounter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class WidgetPositionGui extends Screen {
    private final Screen parent;
    
    private boolean dragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    private float widgetX;
    private float widgetY;
    
    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int HEADER_COLOR = 0xFFFFAA00;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    
    public WidgetPositionGui(Screen parent) {
        super(Text.literal("Widget Position"));
        this.parent = parent;
        this.widgetX = ConfigHandler.counterWidgetX;
        this.widgetY = ConfigHandler.counterWidgetY;
    }
    
    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Close"), button -> {
            ConfigHandler.counterWidgetX = widgetX;
            ConfigHandler.counterWidgetY = widgetY;
            ConfigHandler.syncAndSave();
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 - 100, this.height - 30, 95, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"), button -> {
            this.client.setScreen(parent);
        }).dimensions(this.width / 2 + 5, this.height - 30, 95, 20).build());
        
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Reset Position"), button -> {
            widgetX = 0.01f;
            widgetY = 0.3f;
        }).dimensions(this.width / 2 - 50, this.height - 55, 100, 20).build());
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Drag the widget to reposition it").formatted(Formatting.YELLOW), 
            this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.literal("Click and drag the box below").formatted(Formatting.GRAY), 
            this.width / 2, 35, 0xAAAAAA);
        
        renderPreviewWidget(context, mouseX, mouseY);
    }
    
    private void renderPreviewWidget(DrawContext context, int mouseX, int mouseY) {
        TextRenderer textRenderer = this.textRenderer;
        
        String[] previewNames = {"Player1", "Player2", "Player3"};
        int[] previewCounts = {5, 3, 1};
        
        int maxWidth = textRenderer.getWidth("Party Slayer Counter");
        for (int i = 0; i < previewNames.length; i++) {
            String line = previewNames[i] + ": " + previewCounts[i];
            int w = textRenderer.getWidth(line);
            if (w > maxWidth) maxWidth = w;
        }
        
        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = PADDING * 2 + LINE_HEIGHT + (previewNames.length + 1) * LINE_HEIGHT + 2;
        
        int x = (int) (widgetX * this.width);
        int y = (int) (widgetY * this.height);
        
        x = Math.max(0, Math.min(x, this.width - boxWidth));
        y = Math.max(50, Math.min(y, this.height - boxHeight - 60));
        
        context.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);
        
        boolean hovered = mouseX >= x && mouseX <= x + boxWidth && mouseY >= y && mouseY <= y + boxHeight;
        int borderColor = hovered || dragging ? 0xFFFFAA00 : BORDER_COLOR;
        
        context.drawHorizontalLine(x, x + boxWidth - 1, y, borderColor);
        context.drawHorizontalLine(x, x + boxWidth - 1, y + boxHeight - 1, borderColor);
        context.drawVerticalLine(x, y, y + boxHeight - 1, borderColor);
        context.drawVerticalLine(x + boxWidth - 1, y, y + boxHeight - 1, borderColor);
        
        int textY = y + PADDING;
        context.drawText(textRenderer, Text.literal("Party Slayer Counter").formatted(Formatting.GOLD), 
            x + PADDING, textY, HEADER_COLOR, true);
        textY += LINE_HEIGHT + 2;
        
        for (int i = 0; i < previewNames.length; i++) {
            context.drawText(textRenderer, Text.literal(previewNames[i] + ": ").formatted(Formatting.WHITE), 
                x + PADDING, textY, 0xFFFFFFFF, true);
            int nameWidth = textRenderer.getWidth(previewNames[i] + ": ");
            context.drawText(textRenderer, Text.literal(String.valueOf(previewCounts[i])).formatted(Formatting.GREEN), 
                x + PADDING + nameWidth, textY, 0xFF55FF55, true);
            textY += LINE_HEIGHT;
        }
        
        int total = 9;
        context.drawHorizontalLine(x + PADDING, x + boxWidth - PADDING - 1, textY - 2, 0xFF444444);
        context.drawText(textRenderer, Text.literal("Total: ").formatted(Formatting.YELLOW), 
            x + PADDING, textY, 0xFFFFFF55, true);
        int totalTextWidth = textRenderer.getWidth("Total: ");
        context.drawText(textRenderer, Text.literal(String.valueOf(total)).formatted(Formatting.GREEN, Formatting.BOLD), 
            x + PADDING + totalTextWidth, textY, 0xFF55FF55, true);
    }
    
    @Override
    public boolean mouseClicked(Click click, boolean consumed) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        
        if (button == 0) {
            TextRenderer textRenderer = this.textRenderer;
            int maxWidth = textRenderer.getWidth("Party Slayer Counter");
            int boxWidth = maxWidth + PADDING * 2;
            int boxHeight = PADDING * 2 + LINE_HEIGHT + 4 * LINE_HEIGHT + 2;
            
            int x = (int) (widgetX * this.width);
            int y = (int) (widgetY * this.height);
            x = Math.max(0, Math.min(x, this.width - boxWidth));
            y = Math.max(50, Math.min(y, this.height - boxHeight - 60));
            
            if (mouseX >= x && mouseX <= x + boxWidth && mouseY >= y && mouseY <= y + boxHeight) {
                dragging = true;
                dragOffsetX = (int) mouseX - x;
                dragOffsetY = (int) mouseY - y;
                return true;
            }
        }
        return super.mouseClicked(click, consumed);
    }
    
    @Override
    public boolean mouseReleased(Click click) {
        int button = click.button();
        if (button == 0) {
            dragging = false;
        }
        return super.mouseReleased(click);
    }
    
    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        
        if (dragging) {
            int newX = (int) mouseX - dragOffsetX;
            int newY = (int) mouseY - dragOffsetY;
            
            widgetX = (float) newX / this.width;
            widgetY = (float) newY / this.height;
            
            widgetX = Math.max(0, Math.min(widgetX, 1.0f));
            widgetY = Math.max(0, Math.min(widgetY, 1.0f));
            
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
