package me.prisonranksx.holders;

import me.prisonranksx.common.Common;
import me.prisonranksx.components.*;
import me.prisonranksx.data.PrestigeStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * A prestige that constantly changes it settings as needed for infinite
 * prestige for fast access.
 */
public class UniversalPrestige extends Prestige {

    private String name;
    private String displayName;
    private String nextPrestigeName;
    private double cost;
    private double costIncrease;
    private List<String> broadcastMessages;
    private List<String> messages;
    private List<String> requirementsMessages;
    private CommandsComponent commandsComponent;
    private RequirementsComponent requirementsComponent;
    private ActionBarComponent actionBarComponent;
    private PermissionsComponent permissionsComponent;
    private FireworkComponent fireworkComponent;
    private RandomCommandsComponent randomCommandsComponent;
    private long number;

    public UniversalPrestige(String name, String displayName, String nextPrestigeName, double cost) {
        this(name, displayName, nextPrestigeName, cost, null, null, null, null, null, null, null, null, null, 0.0);
    }

    public UniversalPrestige(String name, String displayName, String nextPrestigeName, double cost,
                             @Nullable List<String> broadcastMessages, @Nullable List<String> messages,
                             @Nullable CommandsComponent commandsComponent, @Nullable RequirementsComponent requirementsComponent,
                             @Nullable ActionBarComponent actionBarComponent, @Nullable PermissionsComponent permissionsComponent,
                             @Nullable FireworkComponent fireworkComponent, @Nullable RandomCommandsComponent randomCommandsComponent,
                             List<String> requirementsMessages, double costIncrease) {
        super(name, displayName, nextPrestigeName, cost, broadcastMessages, messages, commandsComponent,
                requirementsComponent, actionBarComponent, permissionsComponent, fireworkComponent,
                randomCommandsComponent, requirementsMessages, costIncrease);
        this.name = name;
        this.displayName = displayName == null ? name : displayName;
        this.nextPrestigeName = nextPrestigeName == null || nextPrestigeName.equals("LASTPRESTIGE") ? null
                : nextPrestigeName;
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
        this.costIncrease = costIncrease;
        this.number = PrestigeStorage.getHandler().isInfinite() ? Long.parseLong(name)
                : PrestigeStorage.getPrestiges().size() + 1;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UniversalPrestige setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getDisplayName() {
        String rangedDisplayName = PrestigeStorage.getRangedDisplay(getNumber());
        return rangedDisplayName == null ? displayName.replace("{number}", getName())
                : rangedDisplayName.replace("{number}", getName());
    }

    public String getNonReplacedDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getNextPrestigeName() {
        long nextPrestige = Long.parseLong(name) + 1;
        return nextPrestige > PrestigeStorage.getLastPrestigeAsNumber() ? null : String.valueOf(nextPrestige);
    }

    public Prestige getNextPrestige() {
        return PrestigeStorage.getPrestige(getNumber() + 1);
    }

    @Override
    public void setNextPrestigeName(String nextPrestigeName) {
        this.nextPrestigeName = nextPrestigeName;
    }

    @Override
    public double getCost() {
        return Common.eval(PrestigeStorage.getCostExpression().replace("{number}", name));
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
    public void useRequirementsComponent(Consumer<RequirementsComponent> action) {
        if (requirementsComponent == null) return;
        action.accept(requirementsComponent);
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

    @Override
    public double getCostIncrease() {
        return costIncrease;
    }

    @Override
    public void setCostIncrease(double costIncrease) {
        this.costIncrease = costIncrease;
    }

    @Override
    public long getNumber() {
        return Long.parseLong(name);
    }

    @Override
    public void setNumber(long number) {
        this.number = number;
        name = String.valueOf(number);
    }

    public UniversalPrestige clone() {
        return new UniversalPrestige(name, displayName, nextPrestigeName, cost, broadcastMessages, messages,
                commandsComponent, requirementsComponent, actionBarComponent, permissionsComponent, fireworkComponent,
                randomCommandsComponent, requirementsMessages, costIncrease);
    }

    @Override
    public LevelType getLevelType() {
        return LevelType.PRESTIGE;
    }

    public boolean isMultiAccess() {
        return true;
    }

}
