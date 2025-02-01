package me.prisonranksx.components;

import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.utils.NumParser;
import me.prisonranksx.utils.Scrif;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public class RequirementsComponent extends Component {

    private final Map<String, Double> greaterThanRequirements;
    private final Map<String, Double> lessThanRequirements;
    private final Map<String, String> equalRequirements;
    private final Map<String, String> notEqualRequirements;
    private final Map<Scrif, List<String>> scriptRequirements;
    private List<String> keys, values;

    public RequirementsComponent(Map<String, Double> greaterThanRequirements, Map<String, Double> lessThanRequirements,
                                 Map<String, String> equalRequirements, Map<String, String> notEqualRequirements,
                                 Map<Scrif, List<String>> scriptRequirements) {
        this.greaterThanRequirements = greaterThanRequirements;
        this.lessThanRequirements = lessThanRequirements;
        this.equalRequirements = equalRequirements;
        this.notEqualRequirements = notEqualRequirements;
        this.scriptRequirements = scriptRequirements;
    }

    public enum RequirementEvaluationResult {

        GREATER_THAN_FAIL(false),
        LESS_THAN_FAIL(false),
        EQUAL_FAIL(false),
        NOT_EQUAL_FAIL(false),
        SCRIPT_FAIL(false),
        UNKNOWN(false),
        PASS(true);

        private boolean succeeded;
        private Entry<String, ?> failedPlaceholder;

        RequirementEvaluationResult(boolean succeeded) {
            this.succeeded = succeeded;
        }

        public boolean hasSucceeded() {
            return succeeded;
        }

        public <T> RequirementEvaluationResult setFailedPlaceholder(Entry<String, T> placeholder) {
            failedPlaceholder = placeholder;
            return this;
        }

        @SuppressWarnings("unchecked")
        public <T> Entry<String, T> getFailedPlaceholder() {
            return (Entry<String, T>) failedPlaceholder;
        }

    }

    /**
     * @param stringWithPlaceholders a string that has PlaceholderAPI placeholders
     *                               a.k.a any string that starts with "%" and ends
     *                               "%"
     * @return placeholders that start with % and end with % including the %%. <br>
     * For example, {@code [%player_name%, %player_health%]} <br>
     * from string
     * {@code "%player_name% has a cool name, and his health is %player_health%."}
     */
    private static List<String> grabPlaceholdersFromString(String stringWithPlaceholders) {
        List<String> grabbedPlaceholders = new ArrayList<>();
        StringBuilder placeholderBuilder = new StringBuilder();
        boolean startGrabbing = false;
        boolean startedGrabbing = false;
        for (int i = 0; i < stringWithPlaceholders.length(); i++) {
            char currentChar = stringWithPlaceholders.charAt(i);
            if (currentChar == '%') startGrabbing = true;
            if (startGrabbing) {
                if (startedGrabbing) {
                    if (currentChar == '%') {
                        placeholderBuilder.append(currentChar);
                        grabbedPlaceholders.add(placeholderBuilder.toString());
                        placeholderBuilder = new StringBuilder();
                        startGrabbing = false;
                        startedGrabbing = false;
                    } else {
                        if (currentChar == ' ') {
                            placeholderBuilder = new StringBuilder();
                            startGrabbing = false;
                            startedGrabbing = false;
                        } else {
                            placeholderBuilder.append(currentChar);
                        }
                    }
                } else {
                    if (currentChar != ' ') {
                        placeholderBuilder.append(currentChar);
                        startedGrabbing = true;
                    } else {
                        startGrabbing = false;
                        startedGrabbing = false;
                    }
                }
            }
        }
        return grabbedPlaceholders;
    }

    /**
     * Initializes a requirements component from a string list
     *
     * @param requirementsList string list to parse
     * @return a new instance of requirements component parsed from the given list
     * or null if list is null or empty
     */
    @Nullable
    public static RequirementsComponent parseRequirements(@Nullable List<String> requirementsList) {
        if (requirementsList == null || requirementsList.isEmpty()) return null;
        Map<String, Double> greaterThanRequirements = new HashMap<>(), lessThanRequirements = new HashMap<>();
        Map<String, String> equalRequirements = new HashMap<>(), notEqualRequirements = new HashMap<>();
        Map<Scrif, List<String>> scriptRequirements = new HashMap<>();
        List<String> keys = new ArrayList<>(), values = new ArrayList<>();
        requirementsList.stream().map(requirementLine -> requirementLine.toLowerCase()).forEach(requirementLine -> {
            if (requirementLine.startsWith("[script]")) {
                String scriptRequirementLine = requirementLine.replace("[script] ", "").replace("[script]", "");
                List<String> retrievedPlaceholders = grabPlaceholdersFromString(scriptRequirementLine);
                scriptRequirements.put(Scrif.create(scriptRequirementLine), retrievedPlaceholders);
            } else {
                if (requirementLine.contains(">>")) {
                    String[] split = requirementLine.split(">>");
                    greaterThanRequirements.put(split[0], Double.parseDouble(split[1]));
                    keys.add(split[0]);
                    values.add(split[1]);
                } else if (requirementLine.contains("<<")) {
                    String[] split = requirementLine.split("<<");
                    lessThanRequirements.put(split[0], Double.parseDouble(split[1]));
                    keys.add(split[0]);
                    values.add(split[1]);
                } else if (requirementLine.contains("->")) {
                    String[] split = requirementLine.split("->");
                    equalRequirements.put(split[0], split[1]);
                    keys.add(split[0]);
                    values.add(split[1]);
                } else if (requirementLine.contains("<-")) {
                    String[] split = requirementLine.split("<-");
                    notEqualRequirements.put(split[0], split[1]);
                    keys.add(split[0]);
                    values.add(split[1]);
                } else {
                    String scriptRequirementLine = requirementLine;
                    List<String> retrievedPlaceholders = grabPlaceholdersFromString(scriptRequirementLine);
                    scriptRequirements.put(Scrif.create(scriptRequirementLine), retrievedPlaceholders);
                }
            }
        });
        return new RequirementsComponent(greaterThanRequirements.isEmpty() ? null : greaterThanRequirements,
                lessThanRequirements.isEmpty() ? null : lessThanRequirements,
                equalRequirements.isEmpty() ? null : equalRequirements,
                notEqualRequirements.isEmpty() ? null : notEqualRequirements,
                scriptRequirements.isEmpty() ? null : scriptRequirements).setKeysAndValues(keys, values);
    }

    public RequirementsComponent setKeysAndValues(List<String> keys, List<String> values) {
        this.keys = keys;
        this.values = values;
        return this;
    }

    public void forEachNumberRequirement(BiConsumer<String, Double> action) {
        if (greaterThanRequirements != null) greaterThanRequirements.forEach(action);
        if (lessThanRequirements != null) lessThanRequirements.forEach(action);
    }

    /**
     * @return a map that consist of keys representing the placeholder with the
     * percent sign, and the values representing
     * the value to compare with the placeholder i.e the number written
     * after the double arrow ">>"
     * <p>
     * An example of an entry in the map would be:
     * {@code [%ezblocks_block%, 5000]}
     * <br>
     * This expression written in java code is as follows:
     * {@code %ezblocks_block% >= 5000}
     */
    public Map<String, Double> getGreaterThanRequirements() {
        return greaterThanRequirements;
    }

    /**
     * @return a map that consist of keys representing the placeholder with the
     * percent sign, and the values representing
     * the value to compare with the placeholder i.e the number written
     * after the backward double arrow "<<"
     * <p>
     * An example of an entry in the map would be:
     * {@code [%ezblocks_block%, 5000]}
     * <br>
     * This expression written in java code is as follows:
     * {@code %ezblocks_block% <= 5000}
     */
    public Map<String, Double> getLessThanRequirements() {
        return lessThanRequirements;
    }

    /**
     * @return a map that consist of keys representing the placeholder with the
     * percent sign, and the values representing
     * the value to compare with the placeholder i.e the string written
     * after the lambda arrow operator "->"
     * <p>
     * An example of an entry in the map would be:
     * {@code [%vault_group%, vip]}
     * <br>
     * This expression written in java code is as follows:
     * {@code "%vault_group%".equals("vip")}
     */
    public Map<String, String> getEqualRequirements() {
        return equalRequirements;
    }

    /**
     * @return a map that consist of keys representing the placeholder with the
     * percent sign, and the values representing
     * the value to compare with the placeholder i.e the string written
     * after the backward lambda arrow operator "<-"
     * <p>
     * An example of an entry in the map would be:
     * {@code [%vault_group%, vip]}
     * <br>
     * This expression written in java code is as follows:
     * {@code !"%vault_group%".equals("vip")}
     * <br>
     * Notice the <b>!</b> before the expression which indicates a reversed
     * evaluation
     */
    public Map<String, String> getNotEqualRequirements() {
        return notEqualRequirements;
    }

    /**
     * @return a map that consist of keys representing the scripts, and the values
     * representing
     * the placeholders for validation.
     * <p>
     * An example of an entry in the map would be:
     * {@code [(Script that contains)'%vault_group%'=='vip', %vault_group%]}
     */
    public Map<Scrif, List<String>> getScriptRequirements() {
        return scriptRequirements;
    }

    /**
     * Performs requirements check on a player
     *
     * @param player to check the requirements against
     * @return the result of the evaluation, which is either the type of failure
     * with the failed requirement including a reference to the
     * placeholders, or success without mentioning the passed
     * requirements or the placeholders due to their redundancy.
     */
    public RequirementEvaluationResult evaluateRequirements(Player player) {
        boolean result;
        Entry<String, ?> lastTest = null;
        if (scriptRequirements != null) {
            for (Scrif script : scriptRequirements.keySet()) {
                Entry<String, String> scriptResult = script
                        .applyThenEvaluateOrGet((scriptLine) -> StringManager.parsePlaceholders(scriptLine, player));
                lastTest = scriptResult;
                if (scriptResult != null)
                    return RequirementEvaluationResult.SCRIPT_FAIL.setFailedPlaceholder(scriptResult);
            }
        }
        if (greaterThanRequirements != null) {
            for (Entry<String, Double> requirement : greaterThanRequirements.entrySet()) {
                result = NumParser.asDouble(StringManager.parsePlaceholders(requirement.getKey(), player),
                        s -> PrisonRanksX
                                .logSevere("Failed to parse placeholder '" + s + "' as number. Defaulting to 1."),
                        1) >= requirement.getValue();
                lastTest = requirement;
                if (!result) return RequirementEvaluationResult.GREATER_THAN_FAIL.setFailedPlaceholder(requirement);
            }
        }
        if (lessThanRequirements != null) {
            for (Entry<String, Double> requirement : lessThanRequirements.entrySet()) {
                result = NumParser.asDouble(StringManager.parsePlaceholders(requirement.getKey(), player),
                        s -> PrisonRanksX
                                .logSevere("Failed to parse placeholder '" + s + "' as number. Defaulting to 1."),
                        1) <= requirement.getValue();
                lastTest = requirement;
                if (!result) return RequirementEvaluationResult.LESS_THAN_FAIL.setFailedPlaceholder(requirement);
            }
        }
        if (equalRequirements != null) {
            for (Entry<String, String> requirement : equalRequirements.entrySet()) {
                result = StringManager.parsePlaceholders(requirement.getKey(), player).equals(requirement.getValue());
                lastTest = requirement;
                if (!result) return RequirementEvaluationResult.EQUAL_FAIL.setFailedPlaceholder(requirement);
            }
        }
        if (notEqualRequirements != null) {
            for (Entry<String, String> requirement : notEqualRequirements.entrySet()) {
                result = !StringManager.parsePlaceholders(requirement.getKey(), player).equals(requirement.getValue());
                lastTest = requirement;
                if (!result) return RequirementEvaluationResult.NOT_EQUAL_FAIL.setFailedPlaceholder(requirement);
            }
        }
        return RequirementEvaluationResult.PASS.setFailedPlaceholder(lastTest);
    }

    private static boolean isInvalidIndex(List<?> list, int index) {
        return index < 0 || index > list.size() - 1;
    }

    private static <T> T get(List<T> list, int index, T fallBack) {
        if (isInvalidIndex(list, index) && !list.isEmpty())
            return list.get(0);
        else if (isInvalidIndex(list, index) && list.isEmpty()) return fallBack;
        return list.get(index);
    }

    public static List<String> updateMsg(List<String> messages, RequirementsComponent requirementsComponent) {
        if (messages == null) return null;
        if (requirementsComponent == null || requirementsComponent.keys == null || requirementsComponent.keys.isEmpty())
            return messages;
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            for (int j = 0; j < requirementsComponent.keys.size(); j++) {
                message = message.replace("%key." + j + "%", get(requirementsComponent.keys, j, "unknown key"))
                        .replace("%value." + j + "%", get(requirementsComponent.values, j, "unknown value"));
            }
            messages.set(i, message);
        }
        return messages;
    }

    public List<String> updateMsg(List<String> messages) {
        return updateMsg(messages, this);
    }

    @Override
    public boolean use(Player player) {
        return evaluateRequirements(player).hasSucceeded();
    }

    @Override
    public ComponentType getType() {
        return ComponentType.REQUIREMENTS;
    }

}
