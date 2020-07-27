package cat.nyaa.lobby.lobby;

import cat.nyaa.lobby.I18n;
import cat.nyaa.lobby.LobbyPlugin;
import cat.nyaa.lobby.util.Utils;
import cat.nyaa.nyaacore.ILocalizer;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.cmdreceiver.Arguments;
import cat.nyaa.nyaacore.cmdreceiver.CommandReceiver;
import cat.nyaa.nyaacore.cmdreceiver.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

public class LobbyCommands extends CommandReceiver {
    /**
     * @param plugin for logging purpose only
     * @param _i18n
     */
    public LobbyCommands(Plugin plugin, ILocalizer _i18n) {
        super(plugin, _i18n);
    }

    @SubCommand(value = "create", permission = "nyaa.lobby.admin")
    public void onCreate(CommandSender sender, Arguments arguments){
        Player player = asPlayer(sender);
        String lobbyName = arguments.nextString();
        double radius = Utils.getRadius(arguments);
        LobbyManager instance = LobbyManager.getInstance();
        SerializedSpawnPoint region = new SerializedSpawnPoint(new SerializedLocation(player.getLocation()), radius);
        instance.setLobby(lobbyName, region);
        new Message(I18n.format("lobby.create.success")).send(sender);
    }

    @SubCommand(value = "remove", permission = "nyaa.lobby.admin")
    public void onRemove(CommandSender sender, Arguments arguments) {
        String lobbyName = arguments.nextString();
        LobbyManager instance = LobbyManager.getInstance();
        if (instance.removeLobby(lobbyName)){
            new Message(I18n.format("lobby.remove.success", lobbyName)).send(sender);
        }else {
            new Message(I18n.format("lobby.remove.no_lobby", lobbyName)).send(sender);
        }
    }

    @SubCommand(value = "default", permission = "nyaa.lobby.admin")
    public void onDefault(CommandSender sender, Arguments arguments){
        String def = arguments.nextString();
        LobbyManager instance = LobbyManager.getInstance();
        if (!instance.hasLobby(def)) {
            new Message(I18n.format("lobby.default.no_lobby", def)).send(sender);
            return;
        }
        LobbyManager.getInstance().setDefaultLobby(def);
        new Message(I18n.format("lobby.default.success", def)).send(sender);
    }

    private boolean confirm = false;

    @SubCommand(value = "tpAll", permission = "nyaa.lobby.admin")
    public void onTpAll(CommandSender sender, Arguments arguments){
        String lobby = arguments.nextString();
        LobbyManager instance = LobbyManager.getInstance();
        if (!instance.hasLobby(lobby)) {
            new Message(I18n.format("lobby.default.no_lobby", lobby)).send(sender);
            return;
        }
        if (!confirm){
            Message send = new Message(I18n.format("lobby.tpall.confirm", lobby)).send(sender);
            new BukkitRunnable(){
                @Override
                public void run() {
                    confirm = false;
                    new Message(I18n.format("lobby.tpall.abort")).send(sender);
                }
            }.runTaskLater(LobbyPlugin.plugin, 200);
            return;
        }

        //tp all.
        Lobby lobby1 = instance.getLobby(lobby);
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        Iterator<? extends Player> iterator = onlinePlayers.iterator();
        int batchSize = onlinePlayers.size() / 20;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < batchSize; i++) {
                        if (!iterator.hasNext()) {
                            this.cancel();
                            new Message(I18n.format("lobby.tpall.stopped")).send(sender);
                        }
                        lobby1.teleportPlayer(iterator.next());
                    }
                }catch (Exception e){
                    Bukkit.getLogger().log(Level.WARNING, "error teleporting players", e);
                    this.cancel();
                    new Message(I18n.format("lobby.tpall.stopped")).send(sender);
                }
            }
        }.runTaskTimer(LobbyPlugin.plugin, 0, 1);
        new Message(I18n.format("lobby.tpall.started")).send(sender);
    }


    @Override
    public String getHelpPrefix() {
        return "lobby";
    }
}
