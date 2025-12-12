package me.akar1881.sre.counter;

import me.akar1881.sre.config.ConfigHandler;
import me.akar1881.sre.party.PartyHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PartyCounterWidget {
    private static final int BACKGROUND_COLOR = 0x80000000;
    private static final int BORDER_COLOR = 0xFF555555;
    private static final int HEADER_COLOR = 0xFFFFAA00;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int COUNT_COLOR = 0xFF55FF55;
    
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    
    public static void render(DrawContext context) {
        if (!ConfigHandler.counterEnabled || !ConfigHandler.counterWidgetEnabled) return;
        if (!PartyHandler.isInParty()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        Map<String, Integer> killCounts = PartySlayerCounter.getKillCounts();
        if (killCounts.isEmpty()) return;
        
        TextRenderer textRenderer = client.textRenderer;
        
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(killCounts.entrySet());
        sortedEntries.sort(Comparator.comparingInt(e -> -e.getValue()));
        
        int maxWidth = textRenderer.getWidth("Party Slayer Counter");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            String line = entry.getKey() + ": " + entry.getValue();
            int width = textRenderer.getWidth(line);
            if (width > maxWidth) maxWidth = width;
        }
        
        int totalKills = PartySlayerCounter.getTotalKills();
        String totalLine = "Total: " + totalKills;
        int totalWidth = textRenderer.getWidth(totalLine);
        if (totalWidth > maxWidth) maxWidth = totalWidth;
        
        int boxWidth = maxWidth + PADDING * 2;
        int boxHeight = PADDING * 2 + LINE_HEIGHT + (sortedEntries.size() + 1) * LINE_HEIGHT + 2;
        
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        
        int x = (int) (ConfigHandler.counterWidgetX * screenWidth);
        int y = (int) (ConfigHandler.counterWidgetY * screenHeight);
        
        x = Math.max(0, Math.min(x, screenWidth - boxWidth));
        y = Math.max(0, Math.min(y, screenHeight - boxHeight));
        
        context.fill(x, y, x + boxWidth, y + boxHeight, BACKGROUND_COLOR);
        
        context.drawHorizontalLine(x, x + boxWidth - 1, y, BORDER_COLOR);
        context.drawHorizontalLine(x, x + boxWidth - 1, y + boxHeight - 1, BORDER_COLOR);
        context.drawVerticalLine(x, y, y + boxHeight - 1, BORDER_COLOR);
        context.drawVerticalLine(x + boxWidth - 1, y, y + boxHeight - 1, BORDER_COLOR);
        
        int textY = y + PADDING;
        context.drawText(textRenderer, Text.literal("Party Slayer Counter").formatted(Formatting.GOLD), 
            x + PADDING, textY, HEADER_COLOR, true);
        textY += LINE_HEIGHT + 2;
        
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            context.drawText(textRenderer, Text.literal(entry.getKey() + ": ").formatted(Formatting.WHITE), 
                x + PADDING, textY, TEXT_COLOR, true);
            int nameWidth = textRenderer.getWidth(entry.getKey() + ": ");
            context.drawText(textRenderer, Text.literal(String.valueOf(entry.getValue())).formatted(Formatting.GREEN), 
                x + PADDING + nameWidth, textY, COUNT_COLOR, true);
            textY += LINE_HEIGHT;
        }
        
        context.drawHorizontalLine(x + PADDING, x + boxWidth - PADDING - 1, textY - 2, 0xFF444444);
        context.drawText(textRenderer, Text.literal("Total: ").formatted(Formatting.YELLOW), 
            x + PADDING, textY, 0xFFFFFF55, true);
        int totalTextWidth = textRenderer.getWidth("Total: ");
        context.drawText(textRenderer, Text.literal(String.valueOf(totalKills)).formatted(Formatting.GREEN, Formatting.BOLD), 
            x + PADDING + totalTextWidth, textY, COUNT_COLOR, true);
    }
    
    public static int getWidgetWidth() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return 120;
        
        TextRenderer textRenderer = client.textRenderer;
        if (textRenderer == null) return 120;
        
        Map<String, Integer> killCounts = PartySlayerCounter.getKillCounts();
        
        int maxWidth = textRenderer.getWidth("Party Slayer Counter");
        for (Map.Entry<String, Integer> entry : killCounts.entrySet()) {
            String line = entry.getKey() + ": " + entry.getValue();
            int width = textRenderer.getWidth(line);
            if (width > maxWidth) maxWidth = width;
        }
        
        return maxWidth + PADDING * 2;
    }
    
    public static int getWidgetHeight() {
        Map<String, Integer> killCounts = PartySlayerCounter.getKillCounts();
        int entries = Math.max(1, killCounts.size());
        return PADDING * 2 + LINE_HEIGHT + (entries + 1) * LINE_HEIGHT + 2;
    }
}
