package me.akar1881.sre.config;

import java.util.ArrayList;
import java.util.List;

public class SREConfig {
    public boolean renderPlayers = true;
    public boolean renderPartyMembers = true;
    public boolean keybindsEnabled = true;
    public List<String> playersToRender = new ArrayList<>();
    
    public boolean counterEnabled = false;
    public boolean counterWidgetEnabled = true;
    public float counterWidgetX = 0.01f;
    public float counterWidgetY = 0.3f;
}
