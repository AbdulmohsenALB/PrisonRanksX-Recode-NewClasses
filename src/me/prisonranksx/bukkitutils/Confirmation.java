package me.prisonranksx.bukkitutils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Confirmation {

    public static final Map<String, ConfirmationProcessor> processors = new HashMap<>();
    public static final int DEFAULT_DELAY_SECONDS = 5;
    private static final JavaPlugin PLUGIN = JavaPlugin.getProvidingPlugin(Confirmation.class);

    public static ConfirmationProcessor setupConfirmationProcessor(String id, int resetDelaySeconds) {
        ConfirmationProcessor processor = new ConfirmationProcessor(id, resetDelaySeconds);
        processors.put(id, processor);
        return processor;
    }

    public static ConfirmationProcessor getProcessor(String id) {
        return processors.get(id);
    }

    public static ConfirmationProcessor.ConfirmationState getState(String id, String playerName) {
        ConfirmationProcessor processor = processors.get(id);
        if (processor == null) processors.put(id, new ConfirmationProcessor(id, DEFAULT_DELAY_SECONDS));
        processor = processors.get(id);
        return processor.getState(playerName);
    }

    public static ConfirmationProcessor checkConfirmation(String id, String name, Runnable confirmed, Runnable failed) {
        return processors.get(id).ifConfirmed(name, confirmed, failed);
    }

    public static class ConfirmationProcessor {

        private String id;
        private int delaySeconds;
        private Set<String> playerNames;
        private Map<String, BukkitTask> confirmationTasks;

        public ConfirmationProcessor(String id, int delaySeconds) {
            this.id = id;
            this.delaySeconds = delaySeconds;
            this.playerNames = new HashSet<>();
            this.confirmationTasks = new HashMap<>();
        }

        public String getId() {
            return id;
        }

        public int getDelaySeconds() {
            return delaySeconds;
        }

        public ConfirmationProcessor setFail(String playerName) {
            playerNames.remove(playerName);
            BukkitTask task = confirmationTasks.get(playerName);
            if (task != null) task.cancel();
            confirmationTasks.remove(playerName);
            return this;
        }

        public ConfirmationProcessor setReadyToConfirm(String playerName) {
            playerNames.add(playerName);
            BukkitTask task = confirmationTasks.get(playerName);
            if (task == null)
                confirmationTasks.put(playerName, PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN, () -> {
                    playerNames.remove(playerName);
                }, getDelaySeconds() * 20L));
            return this;
        }

        public ConfirmationProcessor ifConfirmed(String playerName, Runnable confirmed, Runnable failed) {
            if (playerNames.contains(playerName)) {
                if (confirmed != null) confirmed.run();
                setFail(playerName);
            } else {
                if (failed != null) failed.run();
                setReadyToConfirm(playerName);
            }
            return this;
        }

        public ConfirmationState getState(String playerName) {
            return new ConfirmationState(id, playerName, playerNames.contains(playerName));
        }

        public static class ConfirmationState {

            private String id;
            private String playerName;
            private boolean readyToConfirm;

            public ConfirmationState(String id, String playerName, boolean readyToConfirm) {
                this.id = id;
                this.playerName = playerName;
                this.readyToConfirm = readyToConfirm;
            }

            public String getPlayerName() {
                return playerName;
            }

            public boolean isReadyToConfirm() {
                return readyToConfirm;
            }

            public ConfirmationState ifConfirmed(Runnable confirmed) {
                if (!readyToConfirm) return this;
                if (confirmed != null) confirmed.run();
                processors.get(id).setFail(playerName);
                return this;
            }

            public void orElse(Runnable ask) {
                if (readyToConfirm) return;
                if (ask != null) ask.run();
                processors.get(id).setReadyToConfirm(playerName);
            }
        }

    }

}
