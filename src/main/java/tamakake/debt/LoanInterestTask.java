package tamakake.debt;

import org.bukkit.scheduler.BukkitRunnable;
import tamakake.Tamakakebank;

import java.util.UUID;

public class LoanInterestTask extends BukkitRunnable {

    private final Tamakakebank plugin;

    public LoanInterestTask(Tamakakebank plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {

        double rate = 0.10; // 10%

        for (UUID uuid : plugin.getMysql().getAllUsers()) {

            long debt = plugin.getMysql().getDebt(uuid);

            if (debt <= 0) continue;

            long interest = (long) (debt * rate);

            plugin.getMysql().addDebt(uuid, interest);
        }

        plugin.getLogger().info("借金利息を付与しました");
    }
}