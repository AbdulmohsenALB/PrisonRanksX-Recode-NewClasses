package me.prisonranksx.commands;

import com.google.common.collect.Lists;
import me.prisonranksx.PrisonRanksX;
import me.prisonranksx.api.PRXAPI;
import me.prisonranksx.bukkitutils.Colorizer;
import me.prisonranksx.bukkitutils.Confirmation;
import me.prisonranksx.bukkitutils.FireworkColor;
import me.prisonranksx.bukkitutils.bukkittickbalancer.BukkitTickBalancer;
import me.prisonranksx.bukkitutils.bukkittickbalancer.ConcurrentTask;
import me.prisonranksx.components.*;
import me.prisonranksx.data.*;
import me.prisonranksx.holders.Rank;
import me.prisonranksx.holders.User;
import me.prisonranksx.managers.ConfigManager;
import me.prisonranksx.managers.MySQLManager;
import me.prisonranksx.managers.StringManager;
import me.prisonranksx.reflections.UniqueId;
import me.prisonranksx.settings.Messages;
import me.prisonranksx.utils.InitHashMaps;
import me.prisonranksx.utils.NumParser;
import me.prisonranksx.utils.ProbabilityCollection;
import me.prisonranksx.utils.Scrif;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PRXCommand extends PluginCommand {

    private PrisonRanksX plugin;
    private final double invalidDouble = -69420.69420;
    private final int invalidInt = -69420;

    private final Map<String, Consumer<String[]>> externalCommands = new HashMap<>();
    private final Map<Integer, List<String>> helpMessages =
            InitHashMaps.newMap(StringManager::parseColorsAndSymbols, 1, Lists.newArrayList(
                            "&3[&bPrisonRanks&cX&3] &7<> = required, [] = optional", "&7&m+------------------------------------------+",
                            "&7/prx &chelp &f[page]", "&7/prx &creload", "&7/prx &csetrank &f<player> <rank> [path]",
                            "&7/prx &cresetrank &f<player>", "&7/prx &ccreaterank &f<name> <cost> [display] [-path:<name>]",
                            "&7/prx &cdelrank &f<name> [path]", "&7/prx &cmoverankpath &f<rank> <frompath> <topath>",
                            "&7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]", "&7/prx &csetrankcost &f<rank> <cost> [path]",
                            "&7/prx &cforcerankup &f<player>", "&7Page: &8(&c1&8/&c3&8)",
                            "&7&m+------------------------------------------+"), 2,
                    Lists.newArrayList("&3[&bPrisonRanks&cX&3] &7<> = required, [] = optional",
                            "&7&m+------------------------------------------+", "&7/prx &csetprestige &f<player> <prestige>",
                            "&7Page: &8(&c2&8/&c3&8)", "&7&m+------------------------------------------+"));

    public enum SubCommandName {
        HELP("help", "?", "h"),
        CONVERT("convert", "convertdata"),
        MSG("msg", "message"),
        SET_RANK("setrank", "changerank", "sr", "setrnak"),
        RESET_RANK("resetrank", "rr"),
        CREATE_RANK("createrank", "cr", "newrank", "addrank"),
        SET_RANK_DISPLAY("setrankdisplay", "changerankdisplay", "srd", "crd", "setrnakdisplay", "setrankprefix"),
        SET_RANK_COST("setrankcost", "changerankcost", "scr", "setrnakcost", "setrankprice"),
        DELETE_RANK("delrank", "deleterank", "dr", "removerank", "remrank"),
        SET_RANK_PATH("setrankpath", "moverankpath", "srp", "setrnakpath"),
        RANK("rank", "rankinfo", "rnak"),
        RANKS("ranks", "ranks", "rankslist"),
        RANKS_PLUS("ranks+", "ranksplus", "rankslist+"),
        RELOAD("reload", "rl"),
        FORCE_RANKUP("forcerankup", "fru", "forceru"),
        SET_PRESTIGE("setprestige", "changeprestige", "sp", "setpres"),
        CALCULATE("calc", "calculate", "math", "m"),
        _EXTERNAL_("_external_"),
        _NUMBER_("_NUMBER_"),
        TEST("test"),
        TEST_2("test2");

        private static final Map<String, SubCommandName> alternateNameMap = new HashMap<>();

        static {
            for (SubCommandName subCommand : values()) {
                for (String name : subCommand.getAlternateNames()) {
                    alternateNameMap.put(name.toLowerCase(), subCommand);
                }
            }
        }

        private String[] alternateNames;

        SubCommandName(String... alternateNames) {
            this.alternateNames = alternateNames;
        }

        public String[] getAlternateNames() {
            return alternateNames;
        }

        public static SubCommandName fromAlternateName(String alternateName) {
            return alternateNameMap.get(alternateName.toLowerCase());
        }
    }

    ConcurrentTask<Integer> concurrentTask;

    public static boolean isEnabled() {
        return CommandSetting.getSetting("prx", "enable");
    }

    public PRXCommand(PrisonRanksX plugin) {
        super(CommandSetting.getStringSetting("prx", "name", "prx"));
        this.plugin = plugin;
        setLabel(getCommandSection().getString("label", "prx"));
        setDescription(getCommandSection().getString("description", getName()));
        setUsage(getCommandSection().getString("usage", "/prx <command> [args]"));
        setPermission(getCommandSection().getString("permission"));
        setPermissionMessage(getCommandSection().getString("permission-message"));
        setAliases(getCommandSection().getStringList("aliases"));
    }

    private SubCommandName testSubCommand(CommandSender sender, String arg) {
        String subCommand = arg.toLowerCase();
        SubCommandName subCommandName = SubCommandName.fromAlternateName(subCommand);
        Consumer<String[]> ext = externalCommands.get(subCommand);
        if (NumParser.isInt(arg)) return SubCommandName._NUMBER_;
        if (subCommandName == null && ext == null) {
            sender.sendMessage(
                    StringManager.parseColors("&4Subcommand &c" + subCommand + " &4doesn't exist. See &e/prx help&4."));
            return null;
        }
        return ext != null ? SubCommandName._EXTERNAL_ : subCommandName;
    }

    private Player testTarget(CommandSender sender, String name) {
        Player target = Bukkit.getPlayer(name);
        if (target == null) {
            Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", name));
            return null;
        }
        return target;
    }

    /**
     * Reads target argument that is represented with &lt;player&gt; placeholder.
     * Below are the supported targets.
     * <table>
     * <tr>
     * <th>Value</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>&lt;player&gt;</td>
     * <td>Represents the player name</td>
     * </tr>
     * <tr>
     * <td>{@literal @a} / *</td>
     * <td>Refers to all online players</td>
     * </tr>
     * <tr>
     * <td>{@literal *-}</td>
     * <td>Represents online players minus players with admin
     * permission</td>
     * </tr>
     * <tr>
     * <td>{@literal @r}</td>
     * <td>Refers to a random player</td>
     * </tr>
     * <tr>
     * <td>{@literal @s}</td>
     * <td>Represents the sender</td>
     * </tr>
     * <tr>
     * <td>{@literal @p}</td>
     * <td>Refers to the closest player to the sender</td>
     * </tr>
     * </table>
     *
     * @param sender who sent the command
     * @param target argument to parse
     * @param action to perform on parsed target
     * @return true if target was parsed and action was executed, false otherwise
     */
    private boolean readTarget(CommandSender sender, String target, Consumer<Player> action) {
        if (target == null) return false;
        switch (target) {
            case "*":
            case "@a": // all online players
                if (Bukkit.getOnlinePlayers().isEmpty()) return false;
                Bukkit.getOnlinePlayers().forEach(action::accept);
                break;
            case "*-": // excludes players with admin permission
                List<Player> playersWithoutPermission = Bukkit.getOnlinePlayers()
                        .stream()
                        .filter(this::hasPermission)
                        .collect(Collectors.toList());
                if (playersWithoutPermission.isEmpty()) return false;

                playersWithoutPermission.forEach(action::accept);

                break;
            case "@r": // random player
                if (Bukkit.getOnlinePlayers().isEmpty()) return false;
                action.accept(Lists.newArrayList(Bukkit.getOnlinePlayers())
                        .get(ThreadLocalRandom.current().nextInt(0, Bukkit.getOnlinePlayers().size())));
                break;
            case "@s": // player who sent the command
                if (!(sender instanceof Player)) return false;
                action.accept((Player) sender);
                break;
            case "@p": // closest player to sender, regardless if it's a command block or a player
                if (sender instanceof ConsoleCommandSender) return false;
                Location senderLocation = sender instanceof Player ? ((Player) sender).getLocation()
                        : ((BlockCommandSender) sender).getBlock().getLocation();
                senderLocation.getWorld()
                        .getPlayers()
                        .stream()
                        .min(Comparator.comparingDouble((p) -> p.getLocation().distanceSquared(senderLocation)))
                        .ifPresent(action::accept);
                break;
            default:
                // condition
                if (target.indexOf('@') == 0 && target.startsWith("@if")) {
                    String conditionString = target.substring(3);
                    Scrif scrif = Scrif.create(conditionString);
                    Scrif.exposeMethods(Player.class);
                    Bukkit.getOnlinePlayers().forEach(p -> {
                        scrif.assignVariable("name", p.getName());
                        scrif.assignVariable("player", p);
                        boolean result = scrif.applyThenEvaluate(s -> StringManager.parsePlaceholders(s, p));
                        if (result) action.accept(p);
                    });
                    break;
                }
                // name of player
                Player player = Bukkit.getPlayer(target);
                if (player == null) {
                    Messages.sendMessage(sender, Messages.getUnknownPlayer(), s -> s.replace("%player%", target));
                    return false;
                }
                action.accept(player);
                break;
        }
        return true;
    }

    private String testRankName(CommandSender sender, String rankName, String pathName) {
        String foundRankName = pathName == null ? null : RankStorage.findRankName(rankName, pathName);
        if (foundRankName == null) {
            Messages.sendMessage(sender, Messages.getUnknownRank(), s -> s.replace("%rank%", rankName));
            return null;
        }
        return foundRankName;
    }

    private String testPrestigeName(CommandSender sender, String prestigeName) {
        String foundPrestigeName = PrestigeStorage.matchPrestigeName(prestigeName);
        if (foundPrestigeName == null && !prestigeName.equals("0") && !prestigeName.equals("-1")) {
            Messages.sendMessage(sender, Messages.getUnknownPrestige(), s -> s.replace("%prestige%", prestigeName));
            sendMsg(sender, "&cUse 0 or -1 to remove a player's prestige");
            return null;
        }
        return foundPrestigeName;
    }

    private String testPathName(CommandSender sender, String pathName) {
        boolean pathExists = RankStorage.pathExists(pathName);
        if (pathExists) return pathName;
        Messages.sendMessage(sender, Messages.getUnknownPath(), s -> s.replace("%path%", pathName));
        return null;
    }

    private double testDouble(CommandSender sender, String numberArgument) {
        double parsedDouble;
        try {
            parsedDouble = Double.parseDouble(numberArgument);
        } catch (NumberFormatException ex) {
            sender.sendMessage(StringManager.parseColors("&c" + numberArgument + " &4is not a valid decimal number."));
            return invalidDouble;
        }
        return parsedDouble;
    }

    private int testInt(CommandSender sender, String numberArgument) {
        int parsedInt;
        try {
            parsedInt = Integer.parseInt(numberArgument);
        } catch (NumberFormatException ex) {
            sender.sendMessage(StringManager.parseColors("&c" + numberArgument + " &4is not a valid integer number."));
            return invalidInt;
        }
        return parsedInt;
    }

    private boolean sendMsg(CommandSender sender, String msg) {
        sender.sendMessage(StringManager.parseColors(msg));
        return true;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!testPermission(sender)) return true;
        switch (args.length) {
            case 0:
                helpMessages.get(1).forEach(sender::sendMessage);
                return true;
            case 1:
                SubCommandName subCommand = testSubCommand(sender, args[0]);
                if (subCommand == null) return true;

                switch (subCommand) {
                    case _NUMBER_:
                        List<String> helpPage = helpMessages.get(NumParser.asInt(args[0]));
                        if (helpPage == null) return sendMsg(sender, "&cInvalid page number.");
                        helpPage.forEach(sender::sendMessage);
                        return true;
                    case HELP:
                        helpMessages.get(1).forEach(sender::sendMessage);
                        return true;
                    case RELOAD:
                        plugin.getAdminExecutor().reload();
                        Messages.sendMessage(sender, Messages.getReload());
                        return true;
                    case SET_RANK:
                        sendMsg(sender, "&4Syntax: &7/prx &csetrank &f<player> <rank> [path]");
                        sendMsg(sender, "&41. Example: &7/prx &csetrank &fNotch A");
                        sendMsg(sender, "&42. Example: &7/prx &csetrank &fNotch Bplus anotherpath");
                        return true;
                    case SET_PRESTIGE: return sendMsg(sender, "&4Syntax: &7/prx &csetprestige &f<player> <prestige>");
                    case RESET_RANK: return sendMsg(sender, "&4Syntax: &7/prx &cresetrank &f<player>");
                    case FORCE_RANKUP: return sendMsg(sender, "&4Syntax: &7/prx &cforcerankup &f<player>");
                    case CREATE_RANK:
                        sendMsg(sender, "&4Syntax: &7/prx &ccreaterank &f<rank> <cost> [prefix] [-path:<name>]");
                        sendMsg(sender, "&41. Example:\n&7/prx &ccreaterank &fC 2500 &b[C]");
                        sendMsg(sender, "&42. Example:\n&7/prx &ccreaterank &fAlpha 5000 &4[Alpha] -path:myotherpath");
                        return true;
                    case SET_RANK_DISPLAY: return sendMsg(sender, "&4Syntax: &7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]");
                    case SET_RANK_COST: return sendMsg(sender, "&4Syntax: &7/prx &csetrankcost &f<rank> <cost> [path]");
                    case DELETE_RANK: return sendMsg(sender, "&4Syntax: &7/prx &cdelrank &f<rank> [path]");
                    case SET_RANK_PATH: return sendMsg(sender, "&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>");
                    case CONVERT: return sendMsg(sender, "&4Syntax: &7/prx &cconvert &fMYSQL/YAML/YAML_PER_USER");
                    case RANKS:
                        RankStorage.getPaths()
                                .forEach(pathName -> RankStorage.getPathRanks(pathName)
                                        .forEach(rank -> sendMsg(sender,
                                                "&7Path: &f" + pathName + " &cRank: &f" + rank.getName())));
                        return true;
                    case RANKS_PLUS:
                        RankStorage.getPaths().forEach(pathName -> {
                            sendMsg(sender, pathName + ":");
                            RankStorage.getPathRanks(pathName)
                                    .forEach(
                                            rank -> sendMsg(sender, "&f    &f" + rank.getIndex() + ". " + rank.getName()
                                                    + " &7> &f" + rank.getDisplayName() + " &c$&a" + rank.getCost()));
                        });
                        return true;
                    case TEST:
                        concurrentTask = BukkitTickBalancer.scheduleConcurrentTask(i -> sender.sendMessage("Int: " + i),
                                (i -> {
                                    if (i == 9999) {
                                        concurrentTask.setAddedValues(0);
                                        concurrentTask.getBukkitTask().cancel();
                                    }
                                    return i == 84932;
                                }), i -> {
                                });
                        concurrentTask.init();
                        concurrentTask.addValues(IntStream.range(0, 10000).boxed().toArray(Integer[]::new));
                        return true;
                    case TEST_2:
                        long time2 = System.currentTimeMillis();
                        String msg = Colorizer.colorize("&6You prestiged to &e%num%&7.");
                        for (int i = 0; i < 9999; i++) {
                            ((Player) sender).sendRawMessage(msg.replace("%num%", String.valueOf(i)));
                        }
                        sender.sendMessage(
                                "[NORMAL] Command executed in " + (System.currentTimeMillis() - time2) + " ms.");
                        return true;
                }
            case 2:
                subCommand = testSubCommand(sender, args[0]);
                if (subCommand == null) return true;
                switch (subCommand) {
                    case SET_RANK:
                        sendMsg(sender, "&4Missing arguments: &c<rank>");
                        sendMsg(sender, "&4Syntax: &7/prx &csetrank &f<player> <rank> [path]");
                        return true;
                    case SET_PRESTIGE:
                        sendMsg(sender, "&4Missing arguments: &c<prestige>");
                        sendMsg(sender, "&4Syntax: &7/prx &csetprestige &f<player> <prestige>");
                        return true;
                    case RESET_RANK: {
                        readTarget(sender, args[1], target -> {
                            plugin.getAdminExecutor()
                                    .setPlayerRank(UniqueId.getUUID(target), RankStorage.getFirstRankName());
                            Messages.sendMessage(sender, Messages.getResetRank(),
                                    s -> s.replace("%player%", target.getName())
                                            .replace("%rank%", RankStorage.getFirstRankName()));
                        });
                        return true;
                    }
                    case FORCE_RANKUP: {
                        readTarget(sender, args[1], target -> plugin.getRankupExecutor().forceRankup(target));
                        return true;
                    }
                    case CREATE_RANK:
                        sendMsg(sender, "&4Missing arguments: &c<cost>");
                        sendMsg(sender, "&4Syntax: &7/prx &ccreaterank &f<rank> <cost> [display] [-path:<name>]");
                        sendMsg(sender, "&41. Example:\n&7/prx &ccreaterank &fC 2500 &b[C]");
                        sendMsg(sender, "&42. Example:\n&7/prx &ccreaterank &fAlpha 5000 &4[Alpha] -path:mypath");
                        return true;
                    case SET_RANK_DISPLAY:
                        sendMsg(sender, "&4Missing arguments: &c<display>");
                        sendMsg(sender, "&4Syntax: &7/prx &csetrankdisplay &f<rank> <display> [-path:<name>]");
                        sendMsg(sender, "&4Example: \n&7/prx &csetrankdisplay &fA &7[&bA&7]");
                        return true;
                    case SET_RANK_COST:
                        sendMsg(sender, "&4Missing arguments: &c<cost>");
                        sendMsg(sender, "&4Syntax: &7/prx &csetrankcost &f<rank> <cost> [path]");
                        sendMsg(sender, "&4Example: \n&7/prx &csetrankcost &fA 25000");
                        return true;
                    case DELETE_RANK: {
                        String rankName = testRankName(sender, args[1], RankStorage.getDefaultPath());
                        if (rankName == null) return true;
                        plugin.getAdminExecutor().deleteRank(rankName, RankStorage.getDefaultPath());
                        Messages.sendMessage(sender, Messages.getDeleteRank(), s -> s.replace("%args1%", rankName));
                        return true;
                    }
                    case SET_RANK_PATH: return sendMsg(sender, "&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>");
                    case CALCULATE: return sendMsg(sender,
                            "Result: " + Scrif.evaluateMathExpression(args[1].toLowerCase().replace("x", "*")));
                    case RANK:
                        String rankName = testRankName(sender, args[1], RankStorage.getDefaultPath());
                        if (rankName == null) return true;
                        Rank rank = RankStorage.getRank(rankName, RankStorage.getDefaultPath());

                        sendMsg(sender, "-- RANK INFO --");
                        long playersCount = Bukkit.getOnlinePlayers().stream().filter(player -> {
                            User user = plugin.getUserController().getUser(UniqueId.getUUID(player));
                            return Objects.equals(user.getRankName(), rankName);
                        }).count();
                        sendMsg(sender, "&7Amount of online players with this rank: &f" + playersCount);
                        sendMsg(sender, "&7Index: &f" + rank.getIndex());
                        sendMsg(sender, "&7Name: &f" + rank.getName());
                        sendMsg(sender, "&7Path: &f" + RankStorage.getDefaultPath());
                        sendMsg(sender, "&7Cost: &f" + rank.getCost());
                        sendMsg(sender, "&7Display: &f" + rank.getDisplayName());
                        sendMsg(sender, "&7Next Rank: &f" + rank.getNextName());
                        sendMsg(sender, "&7Is allow prestige: &f" + rank.isAllowPrestige());

                        List<String> broadcastMsgs = rank.getBroadcastMessages();
                        sendMsg(sender,
                                broadcastMsgs == null ? "&7&mBroadcast Messages:&f none" : "&7Broadcast Messages:");
                        if (broadcastMsgs != null) broadcastMsgs.forEach(msg -> sendMsg(sender, "&r" + msg));

                        List<String> messages = rank.getMessages();
                        sendMsg(sender, messages == null ? "&7&mMessages:&f none" : "&7Messages:");
                        if (messages != null) messages.forEach(msg -> sendMsg(sender, "&r" + msg));

                        CommandsComponent commandsComponent = rank.getCommandsComponent();
                        sendMsg(sender, commandsComponent == null ? "&7&mCommands:&f none" : "&7Commands:");
                        if (commandsComponent != null) {
                            List<String> console = commandsComponent.getConsoleCommands();
                            if (console != null)
                                console.forEach(command -> sendMsg(sender, "&f[console] &a" + command));
                            List<String> player = commandsComponent.getPlayerCommands();
                            if (player != null) player.forEach(command -> sendMsg(sender, "&f[player] &e" + command));
                        }

                        ActionBarComponent actionBarComponent = rank.getActionBarComponent();
                        sendMsg(sender, actionBarComponent == null ? "&7&mAction Bar:&f none" : "&7Action Bar:");
                        if (actionBarComponent != null) {
                            sendMsg(sender, "&r Interval: &r" + actionBarComponent.getActionBarSender().getInterval());
                            actionBarComponent.getActionBarSender().forEachMessage(m -> sendMsg(sender, "&r" + m));
                        }

                        PermissionsComponent permissionsComponent = rank.getPermissionsComponent();
                        sendMsg(sender, permissionsComponent == null ? "&7&mPermissions:&f none" : "&7Permissions:");
                        if (permissionsComponent != null) {
                            Set<String> add = permissionsComponent.getAddPermissionCollection();
                            Set<String> del = permissionsComponent.getDelPermissionCollection();
                            if (add != null) add.forEach(permission -> sendMsg(sender, "&7+ &a" + permission));
                            if (del != null) del.forEach(permission -> sendMsg(sender, "&7- &c" + permission));
                        }
                        RandomCommandsComponent randomCommandsComponent = rank.getRandomCommandsComponent();
                        sendMsg(sender,
                                randomCommandsComponent == null ? "&7&mRandom Commands:&f none" : "&7Random Commands:");
                        if (randomCommandsComponent != null) {
                            NavigableSet<ProbabilityCollection.ProbabilitySetElement<List<String>>> collection = randomCommandsComponent
                                    .getCollection();
                            if (collection != null) {
                                collection.forEach(probabilitySetElement -> {
                                    sendMsg(sender, " &rChance: " + probabilitySetElement.getProbability() + " %");
                                    sendMsg(sender, " &rCommands: " + probabilitySetElement.getObject());
                                });
                            }
                        }
                        RequirementsComponent requirementsComponent = rank.getRequirementsComponent();
                        sendMsg(sender, requirementsComponent == null ? "&7&mRequirements:&f none" : "&7Requirements:");
                        if (requirementsComponent != null) {
                            Map<String, String> equalRequirements = requirementsComponent.getEqualRequirements();
                            if (equalRequirements != null)
                                equalRequirements.forEach((k, v) -> sendMsg(sender, "&fEqual: " + k + "&a->&f" + v));
                            Map<String, String> notEqualRequirements = requirementsComponent.getNotEqualRequirements();
                            if (notEqualRequirements != null) notEqualRequirements
                                    .forEach((k, v) -> sendMsg(sender, "&fNot Equal: " + k + "&c<-&f" + v));
                            Map<String, Double> greaterThanRequirements = requirementsComponent
                                    .getGreaterThanRequirements();
                            if (greaterThanRequirements != null) greaterThanRequirements
                                    .forEach((k, v) -> sendMsg(sender, "&fGreater Than: " + k + "&a>>&f" + v));
                            Map<String, Double> lessThanRequirements = requirementsComponent.getLessThanRequirements();
                            if (lessThanRequirements != null) lessThanRequirements
                                    .forEach((k, v) -> sendMsg(sender, "&fLess Than: " + k + "&c<<&f" + v));
                            Map<Scrif, List<String>> scriptRequirements = requirementsComponent.getScriptRequirements();
                            if (scriptRequirements != null) scriptRequirements.forEach(
                                    (k, v) -> sendMsg(sender, "&fScript: " + k.getScript() + " Placeholders: " + v));

                        }
                        List<String> requirementsMessages = rank.getRequirementsMessages();
                        sendMsg(sender, requirementsMessages == null ? "&7&mRequirements Messages:&f none"
                                : "&7Requirements Messages:");
                        if (requirementsMessages != null) {
                            requirementsMessages.forEach(msg -> sendMsg(sender, "&r" + msg));
                            Messages.sendMessages(sender, requirementsMessages,
                                    l -> RequirementsComponent.updateMsg(l, requirementsComponent));
                        }
                        FireworkComponent fireworkComponent = rank.getFireworkComponent();
                        sendMsg(sender, fireworkComponent == null ? "&7&mFirework:&f none" : "&7Firework:");

                        if (fireworkComponent != null) {
                            sendMsg(sender, " Power: " + fireworkComponent.getPower());
                            fireworkComponent.getFireworkEffects().forEach(effect -> {
                                sendMsg(sender, " Type: " + effect.getType().name());
                                sendMsg(sender, " Colors: " + FireworkColor.stringify(effect.getColors()));
                                sendMsg(sender, " FadeColors: " + FireworkColor.stringify(effect.getFadeColors()));
                                sendMsg(sender, " Flicker: " + effect.hasFlicker());
                                sendMsg(sender, " Trail: " + effect.hasTrail());
                            });
                        }
                        return true;
                    case TEST: return true;
                    case TEST_2: return true;
                    case CONVERT:
                        switch (args[1].toUpperCase()) {
                            case "MYSQL":
                            case "SQL":
                                Confirmation.getState("conversion_mysql", sender.getName()).ifConfirmed(() -> {
                                    Messages.sendMessage(sender, Messages.getDataConversion());
                                    ConfigManager.getConfig().set("MySQL.enable", true);
                                    ConfigManager.getConfig().set("Options.data-storage-type", "MYSQL");
                                    plugin.getGlobalSettings().setDataStorageType("MYSQL");
                                    ConfigManager.saveConfig("config.yml");
                                    MySQLManager.reload();
                                    plugin.getUserController()
                                            .convert(UserControllerType.MYSQL)
                                            .thenAcceptAsync(users -> {
                                                plugin.setUserController(new MySQLUserController(plugin));
                                                plugin.getUserController().setUsers(users);
                                            })
                                            .thenRun(() -> Messages.sendMessage(sender,
                                                    Messages.getDataConversionSuccess(),
                                                    s -> s.replace("%type%", "MySQL")))
                                            .exceptionally(throwable -> {
                                                Messages.sendMessage(sender, Messages.getDataConversionFail());
                                                throwable.printStackTrace();
                                                return null;
                                            });
                                }).orElse(() -> {
                                    sendMsg(sender,
                                            "&cAre you sure you want to convert data to &bMySQL&c? Write the command again to confirm "
                                                    + "(5 seconds and it will be cancelled).");
                                });
                                return true;
                            case "YAML":
                            case "YML":
                                Confirmation.getState("conversion_yaml", sender.getName()).ifConfirmed(() -> {
                                    Messages.sendMessage(sender, Messages.getDataConversion());
                                    ConfigManager.getConfig().set("MySQL.enable", false);
                                    ConfigManager.getConfig().set("Options.data-storage-type", "YAML");
                                    ConfigManager.saveConfig("config.yml");
                                    plugin.getUserController()
                                            .convert(UserControllerType.YAML)
                                            .thenAcceptAsync(users -> {
                                                plugin.setUserController(new YamlUserController(plugin));
                                                plugin.getUserController().setUsers(users);
                                                MySQLManager.closeConnection();
                                            })
                                            .thenRun(() -> Messages.sendMessage(sender,
                                                    Messages.getDataConversionSuccess(),
                                                    s -> s.replace("%type%", "Yaml")))
                                            .exceptionally(throwable -> {
                                                Messages.sendMessage(sender, Messages.getDataConversionFail());
                                                throwable.printStackTrace();
                                                return null;
                                            });
                                }).orElse(() -> {
                                    sendMsg(sender,
                                            "&cAre you sure you want to convert data to &aYAML&c? Write the command again to confirm "
                                                    + "(5 seconds and it will be cancelled).");
                                });
                                return true;
                            case "YAML_PER_USER":
                            case "YAMLPERUSER":
                                Confirmation.getState("conversion_yamlperuser", sender.getName()).ifConfirmed(() -> {
                                    Messages.sendMessage(sender, Messages.getDataConversion());
                                    ConfigManager.getConfig().set("MySQL.enable", false);
                                    ConfigManager.getConfig().set("Options.data-storage-type", "YAML_PER_USER");
                                    ConfigManager.saveConfig("config.yml");
                                    plugin.getUserController()
                                            .convert(UserControllerType.YAML_PER_USER)
                                            .thenAcceptAsync(users -> {
                                                plugin.setUserController(new YamlPerUserController(plugin));
                                                plugin.getUserController().setUsers(users);
                                                MySQLManager.closeConnection();
                                            })
                                            .thenRun(() -> Messages.sendMessage(sender,
                                                    Messages.getDataConversionSuccess(),
                                                    s -> s.replace("%type%", "Yaml Per User")))
                                            .exceptionally(throwable -> {
                                                Messages.sendMessage(sender, Messages.getDataConversionFail());
                                                throwable.printStackTrace();
                                                return null;
                                            });
                                }).orElse(() -> {
                                    sendMsg(sender,
                                            "&cAre you sure you want to convert data to &aYAML &7PER &aUSER&c? Write the command again to confirm "
                                                    + "(5 seconds and it will be cancelled).");
                                });
                                return true;
                        }
                        return true;
                }
            case 3:
                subCommand = testSubCommand(sender, args[0]);
                if (subCommand == null) return true;
                switch (subCommand) {
                    case MSG:
                        readTarget(sender, args[1], target -> sendMsg(target, args[2]));
                        return true;
                    case SET_RANK: {
                        readTarget(sender, args[1], target -> {
                            String rankName = testRankName(sender, args[2], PRXAPI.getPlayerPathOrDefault(target));
                            if (rankName == null) return;
                            plugin.getAdminExecutor().setPlayerRank(UniqueId.getUUID(target), rankName);
                            Messages.sendMessage(sender, Messages.getSetRank(),
                                    s -> s.replace("%player%", target.getName()).replace("%rank%", rankName));
                        });
                        return true;
                    }
                    case SET_PRESTIGE: {
                        readTarget(sender, args[1], target -> {
                            String prestigeName = testPrestigeName(sender, args[2]);
                            boolean deletePrestige = args[2].equals("0") || args[2].equals("-1");
                            if (prestigeName == null && !deletePrestige) return;
                            if (deletePrestige)
                                plugin.getUserController().getUser(UniqueId.getUUID(target)).setPrestigeName(null);
                            else
                                plugin.getAdminExecutor().setPlayerPrestige(UniqueId.getUUID(target), prestigeName);
                            Messages.sendMessage(sender, Messages.getSetPrestige(),
                                    s -> s.replace("%player%", target.getName())
                                            .replace("%prestige%", deletePrestige ? "none" : prestigeName));
                        });
                        return true;
                    }
                    case CREATE_RANK: {
                        double cost = testDouble(sender, args[2]);
                        if (cost == invalidDouble) return true;
                        plugin.getAdminExecutor()
                                .createRank(args[1], cost, RankStorage.getDefaultPath(), "[" + args[1] + "]");
                        Messages.sendMessage(sender, Messages.getCreateRank(),
                                s -> s.replace("%rank%", args[1]).replace("%cost%", args[2]));
                        return true;
                    }
                    case SET_RANK_COST: {
                        String rankName = testRankName(sender, args[1], RankStorage.getDefaultPath());
                        if (rankName == null) return true;
                        double cost = testDouble(sender, args[2]);
                        if (cost == invalidDouble) return true;
                        plugin.getAdminExecutor().setRankCost(rankName, RankStorage.getDefaultPath(), cost);
                        Messages.sendMessage(sender, Messages.getSetRankCost(),
                                s -> s.replace("%args1%", args[1]).replace("%args2%", args[2]));
                        return true;
                    }
                    case DELETE_RANK:
                        String pathName = testPathName(sender, args[2]);
                        if (pathName == null) return true;
                        String rankName = testRankName(sender, args[1], pathName);
                        if (rankName == null) return true;
                        plugin.getAdminExecutor().deleteRank(rankName, pathName);
                        Messages.sendMessage(sender, Messages.getDeleteRank(), s -> s.replace("%args1%", rankName));
                        return true;
                    case SET_RANK_PATH:
                        sender.sendMessage(StringManager
                                .parseColors("&4Syntax: &7/prx &cmoverankpath &f<rank> <currentpath> <newpath>"));
                        return true;
                }
            case 4:
                subCommand = testSubCommand(sender, args[0]);
                if (subCommand == null) return true;
                switch (subCommand) {
                    case MSG:
                        readTarget(sender, args[1], target -> sendMsg(target, args[2] + " " + args[3]));
                        return true;
                    case SET_RANK: {
                        Player target = testTarget(sender, args[1]);
                        if (target == null) return true;
                        String pathName = testPathName(sender, args[3]);
                        if (pathName == null) return true;
                        String rankName = testRankName(sender, args[2], pathName);
                        if (rankName == null) return true;
                        plugin.getAdminExecutor().setPlayerRank(UniqueId.getUUID(target), rankName, pathName);
                        Messages.sendMessage(sender, Messages.getSetRank(),
                                s -> s.replace("%player%", target.getName()).replace("%rank%", rankName));
                        return true;
                    }
                    case SET_RANK_COST: {
                        String pathName = testPathName(sender, args[3]);
                        if (pathName == null) return true;
                        String rankName = testRankName(sender, args[1], pathName);
                        if (rankName == null) return true;
                        double cost = testDouble(sender, args[2]);
                        if (cost == invalidDouble) return true;
                        plugin.getAdminExecutor().setRankCost(rankName, pathName, cost);
                        Messages.sendMessage(sender, Messages.getSetRankCost(),
                                s -> s.replace("%args1%", args[1]).replace("%args2%", args[2]));
                        return true;
                    }
                    case SET_RANK_PATH:
                        String pathName = testPathName(sender, args[2]);
                        if (pathName == null) return true;
                        String rankName = testRankName(sender, args[1], pathName);
                        if (rankName == null) return true;
                        String newPathName = args[3].toLowerCase();
                        plugin.getAdminExecutor().moveRankPath(rankName, pathName, newPathName);
                        Messages.sendMessage(sender, Messages.getSetRankPath(),
                                s -> s.replace("%args1%", rankName)
                                        .replace("%args2%", newPathName)
                                        .replace("%args3%", pathName));
                        return true;
                }
            default:
                // Commands with arguments that allow spaces, like display name.
                subCommand = testSubCommand(sender, args[0]);
                if (subCommand == null) return true;
                switch (subCommand) {
                    case MSG:
                        readTarget(sender, args[1], target -> sendMsg(target, StringManager.getArgs(args, 2)));
                        return true;
                    case CREATE_RANK: {
                        String lastArg = StringManager.getArgs(args, 3);
                        String[] spaces = lastArg.split(" ");
                        String pathName = RankStorage.getDefaultPath();
                        StringBuilder initialDisplayName = new StringBuilder();
                        for (String arg : spaces) if (arg.startsWith("-path:")) pathName = arg.replace("-path:", "");
                        initialDisplayName.append(lastArg.replace(" -path:" + pathName, ""));
                        String displayName = initialDisplayName.length() == 0 ? args[2] : initialDisplayName.toString();
                        double cost = testDouble(sender, args[2]);
                        if (cost == invalidDouble) return true;
                        plugin.getAdminExecutor().createRank(args[1], cost, pathName, displayName);
                        Messages.sendMessage(sender, Messages.getCreateRank(),
                                s -> s.replace("%rank%", StringManager.parseColors(displayName))
                                        .replace("%cost%", args[2]));
                        return true;
                    }
                    case SET_RANK_DISPLAY:
                        String lastArg = StringManager.getArgs(args, 2);
                        String[] spaces = lastArg.split(" ");
                        String pathName = RankStorage.getDefaultPath();
                        StringBuilder initialDisplayName = new StringBuilder();
                        // allows user to place -path: anywhere, not necessarily in the end.
                        for (String arg : spaces) if (arg.startsWith("-path:")) pathName = arg.replace("-path:", "");
                        pathName = testPathName(sender, pathName);
                        if (pathName == null) return true;
                        String rankName = testRankName(sender, args[1], pathName);
                        if (rankName == null) return true;
                        initialDisplayName.append(lastArg.replace(" -path:" + pathName, ""));
                        String displayName = initialDisplayName.length() == 0 ? args[2] : initialDisplayName.toString();
                        plugin.getAdminExecutor().setRankDisplayName(rankName, pathName, displayName);
                        Messages.sendMessage(sender, Messages.getSetRankDisplay(), s -> s.replace("%args1%", rankName)
                                .replace("%args2%", StringManager.parseColors(displayName)));
                        return true;
                    case _EXTERNAL_:
                        externalCommands.get(args[0]).accept(args);
                        return true;
                }
                break;
        }
        return true;
    }

}
