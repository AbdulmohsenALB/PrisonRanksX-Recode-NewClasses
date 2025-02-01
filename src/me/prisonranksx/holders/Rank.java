package me.prisonranksx.holders;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.components.*;
import me.prisonranksx.data.RankStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class Rank implements Level {

    private String name;
    private String displayName;
    private String nextRankName;
    private double cost;
    private List<String> broadcastMessages;
    private List<String> messages;
    private List<String> requirementsMessages;
    private boolean allowPrestige;
    private CommandsComponent commandsComponent;
    private RequirementsComponent requirementsComponent;
    private ActionBarComponent actionBarComponent;
    private PermissionsComponent permissionsComponent;
    private FireworkComponent fireworkComponent;
    private RandomCommandsComponent randomCommandsComponent;
    private long index;

    public Rank(String name, String displayName, String nextRankName, double cost) {
        this(name, displayName, nextRankName, cost, null, null, null, null, null, null, null, null, null, false);
    }

    public Rank(String name, String displayName, String nextRankName, double cost,
                @Nullable List<String> broadcastMessages, @Nullable List<String> messages,
                @Nullable CommandsComponent commandsComponent, @Nullable RequirementsComponent requirementsComponent,
                @Nullable ActionBarComponent actionBarComponent, @Nullable PermissionsComponent permissionsComponent,
                @Nullable FireworkComponent fireworkComponent, @Nullable RandomCommandsComponent randomCommandsComponent,
                List<String> requirementsMessages, boolean allowPrestige) {
        this.name = name;
        this.displayName = displayName == null ? name : displayName;
        this.nextRankName = nextRankName == null || nextRankName.equalsIgnoreCase("LASTRANK") ? null : nextRankName;
        this.cost = cost;
        this.broadcastMessages = broadcastMessages == null || broadcastMessages.isEmpty() ? null : broadcastMessages;
        this.messages = messages == null || messages.isEmpty() ? null : messages;
        this.commandsComponent = commandsComponent;
        this.requirementsComponent = requirementsComponent;
        this.actionBarComponent = actionBarComponent;
        this.permissionsComponent = permissionsComponent;
        this.fireworkComponent = fireworkComponent;
        this.randomCommandsComponent = randomCommandsComponent;
        this.requirementsMessages = requirementsMessages == null || requirementsMessages.isEmpty()
                || randomCommandsComponent == null ? null : requirementsMessages;
        this.allowPrestige = allowPrestige;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Rank setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return null when rank is last rank or its nextrank is set to "LASTRANK"
     */
    @Override
    @Nullable
    public String getNextName() {
        if (nextRankName != null && !RankStorage.rankExists(nextRankName)) {
            PrisonRanksX.logSevere("Rank '" + name + "' nextRank is set to '" + nextRankName
                    + "' which is invalid! Fix that in config files.");
            return null;
        }
        return nextRankName;
    }

    @Override
    public void setNextName(String nextRankName) {
        this.nextRankName = nextRankName;
    }

    @Override
    public double getCost() {
        return cost;
    }

    @Override
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public List<String> getBroadcastMessages() {
        return broadcastMessages;
    }

    @Override
    public void setBroadcastMessages(List<String> broadcastMessages) {
        this.broadcastMessages = broadcastMessages;
    }

    @Override
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    @Override
    public CommandsComponent getCommandsComponent() {
        return commandsComponent;
    }

    @Override
    public void useCommandsComponent(Consumer<CommandsComponent> action) {
        if (commandsComponent == null) return;
        action.accept(commandsComponent);
    }

    @Override
    public void setCommandsComponent(CommandsComponent commandsComponent) {
        this.commandsComponent = commandsComponent;
    }

    @Override
    public RequirementsComponent getRequirementsComponent() {
        return requirementsComponent;
    }

    @Override
    public void setRequirementsComponent(RequirementsComponent requirementsComponent) {
        this.requirementsComponent = requirementsComponent;
    }

    @Override
    public ActionBarComponent getActionBarComponent() {
        return actionBarComponent;
    }

    @Override
    public void useActionBarComponent(Consumer<ActionBarComponent> action) {
        if (actionBarComponent == null) return;
        action.accept(actionBarComponent);
    }

    @Override
    public void setActionBarComponent(ActionBarComponent actionBarComponent) {
        this.actionBarComponent = actionBarComponent;
    }

    @Override
    public PermissionsComponent getPermissionsComponent() {
        return permissionsComponent;
    }

    @Override
    public void usePermissionsComponent(Consumer<PermissionsComponent> action) {
        if (permissionsComponent == null) return;
        action.accept(permissionsComponent);
    }

    @Override
    public void setPermissionsComponent(PermissionsComponent permissionsComponent) {
        this.permissionsComponent = permissionsComponent;
    }

    @Override
    public FireworkComponent getFireworkComponent() {
        return fireworkComponent;
    }

    @Override
    public void useFireworkComponent(Consumer<FireworkComponent> action) {
        if (fireworkComponent == null) return;
        action.accept(fireworkComponent);
    }

    @Override
    public void setFireworkComponent(FireworkComponent fireworkComponent) {
        this.fireworkComponent = fireworkComponent;
    }

    @Override
    public RandomCommandsComponent getRandomCommandsComponent() {
        return randomCommandsComponent;
    }

    @Override
    public void useRandomCommandsComponent(Consumer<RandomCommandsComponent> action) {
        if (randomCommandsComponent == null) return;
        action.accept(randomCommandsComponent);
    }

    @Override
    public void setRandomCommandsComponent(RandomCommandsComponent randomCommandsComponent) {
        this.randomCommandsComponent = randomCommandsComponent;
    }

    @Override
    public List<String> getRequirementsMessages() {
        return RequirementsComponent.updateMsg(requirementsMessages, requirementsComponent);
    }

    @Override
    public void setRequirementsMessages(List<String> requirementsMessages) {
        this.requirementsMessages = requirementsMessages;
    }

    public boolean isAllowPrestige() {
        return allowPrestige;
    }

    public void setAllowPrestige(boolean allowPrestige) {
        this.allowPrestige = allowPrestige;
    }

    @Override
    public long getIndex() {
        return index;
    }

    @Override
    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.RANK;
    }

}
