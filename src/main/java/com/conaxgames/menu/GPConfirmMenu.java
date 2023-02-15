package com.conaxgames.menu;

import com.conaxgames.libraries.menu.Button;
import com.conaxgames.libraries.menu.Menu;
import com.conaxgames.libraries.util.Callback;
import org.bukkit.entity.Player;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPConfirmMenu extends Menu
{
    private final String title;
    private final List<String> details;
    private final Callback<Boolean> response;

    @ConstructorProperties({"title", "response"})
    public GPConfirmMenu(String title, Callback<Boolean> response, List<String> details) {
        this.title = title;
        this.details = details;
        this.response = response;
        this.setPlaceholder(true);
    }

    public Map<Integer, Button> getButtons(Player player) {
        HashMap<Integer, Button> buttons = new HashMap<>();
        buttons.put(11, new GPBooleanButton(true, this.response, this.details));
        buttons.put(15, new GPBooleanButton(false, this.response, this.details));
        return buttons;
    }

    public String getTitle(Player player) {
        return this.title;
    }

    public int size(Map<Integer, Button> buttons) {
        return 27;
    }
}
