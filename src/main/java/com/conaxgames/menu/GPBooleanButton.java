package com.conaxgames.menu;

import com.conaxgames.libraries.menu.Button;
import com.conaxgames.libraries.message.FormatUtil;
import com.conaxgames.libraries.util.CC;
import com.conaxgames.libraries.util.Callback;
import com.conaxgames.libraries.util.WoolUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;

public class GPBooleanButton extends Button
{
    private final boolean confirm;
    private final Callback<Boolean> callback;
    private final List<String> details;

    public void clicked(Player player, int i, ClickType clickType) {
        player.closeInventory();
        this.callback.callback(this.confirm);
    }

    public String getName(Player player) {
        return this.confirm ? CC.GREEN + "Confirm" : CC.RED + "Cancel";
    }

    public List<String> getDescription(Player player) {
        List<String> description = new ArrayList<>();
        if (this.confirm) {
            this.details.forEach(line -> {
                description.addAll(FormatUtil.wordWrap(line));
            });
        } else {
            description.add(CC.GRAY + "Cancels this process.");
        }

        return description;
    }

    public int getDamageValue(Player player) {
        return this.confirm ? 5 : 14;
    }

    public Material getMaterial(Player player) {
        return WoolUtil.convertChatColorToXClay(this.confirm ? ChatColor.GREEN : ChatColor.RED).parseMaterial();
    }

    @ConstructorProperties({"confirm", "callback"})
    public GPBooleanButton(boolean confirm, Callback<Boolean> callback, List<String> details) {
        this.details = details;
        this.confirm = confirm;
        this.callback = callback;
    }
}
