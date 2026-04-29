package tamakake.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import tamakake.Tamakakebank;

public class PlayerJoinListener implements Listener {

    private final Tamakakebank plugin;

    public PlayerJoinListener(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {

        plugin.getMysql().registerPlayer(
                e.getPlayer().getUniqueId(),
                e.getPlayer().getName()
        );
    }
}