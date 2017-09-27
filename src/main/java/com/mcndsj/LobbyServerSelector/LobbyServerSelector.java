package com.mcndsj.LobbyServerSelector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcndsj.GameEvent.Events.GameInitReadyEvent;
import com.mcndsj.GameEvent.Events.GameStartEvent;
import com.mcndsj.LobbyServerSelector.Api.API;
import com.mcndsj.LobbyServerSelector.Sign.SignController;
import com.mcndsj.lobby_Vip.LobbyVip;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matthew on 2016/4/18.
 */
public class LobbyServerSelector extends JavaPlugin{

    private Map<String,Request> cacheQueue;


    private JedisMessager messager;
    private SignController signController = null;

    private static LobbyServerSelector instance;
    private API api ;

    public void onEnable(){
        instance = this;
        cacheQueue = new HashMap<String,Request>();
        api = new API();
        messager = new JedisMessager();
        signController = new SignController();

        this.getServer().getPluginManager().registerEvents(messager,this);
        this.getServer().getPluginManager().registerEvents(signController,this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getPluginManager().callEvent(new GameInitReadyEvent());
    }



    public void sendPlayerTo(Player p , String server, boolean force){
        if(force) {

        }else{
            System.out.print("Send" + p.getName() + " to server" + server);
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(server);
            p.sendPluginMessage(this, "BungeeCord", out.toByteArray());
        }
    }

    public API getApi(){
        return api;
    }

    public void onDisable(){

        messager.unsubscribe();
        Bukkit.getPluginManager().callEvent(new GameStartEvent());

    }

    public SignController getSignController(){
        return signController;
    }

    public void addToQueue(Player p , String typename){
        if(cacheQueue.containsKey(p.getName())) {
            if(cacheQueue.get(p.getName()).lastType.equals(typename)){
                if (cacheQueue.get(p.getName()).time > System.currentTimeMillis()) {
                    if(LobbyVip.getApi().isVip(p.getName())){
                        p.sendMessage(ChatColor.AQUA + "队列 >> " + ChatColor.GRAY + "尊贵的会员,您已经在我们的匹配队列前排绿色通道,请您耐心等待!");

                    }else{
                        p.sendMessage(ChatColor.AQUA + "队列 >> " + ChatColor.GRAY + "您已在队列中,稍后会将您自动匹配至对应房间中....");
                    }
                    return;
                }
            }else {
                messager.sendPlayerQuit(p, cacheQueue.get(p.getName()).lastType);
            }
        }
        cacheQueue.put(p.getName(),new Request(System.currentTimeMillis() + 10 * 1000, typename));
        if(LobbyVip.getApi().isVip(p.getName())){
            p.sendMessage(ChatColor.AQUA+ "队列 >> "+ ChatColor.GRAY + "尊贵的会员,已帮您加入配队列前排绿色通道,很快您将会加入游戏!");

        }else{
            p.sendMessage(ChatColor.AQUA+ "队列 >> "+ ChatColor.GRAY + "加入匹配队列,稍后会将您自动匹配至对应房间中,请在大厅内耐心等待...");
            p.sendMessage(ChatColor.RED + "等待时间太长?开通购买永久会员即可优先加入游戏!详情查看www.mcndsj.com");
        }
        messager.addToQueue(p,typename);
    }

    public Request removeFromQueue(Player p){
        try {
            Request value = cacheQueue.remove(p.getName());
            return value;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void countUpdateSub(String type){
        messager.countSub(type);
    }


    public static LobbyServerSelector getInstance(){
        return instance;
    }


    public class Request {
        public long time;
        public String lastType = null;

        public Request(long time, String lastType){
            this.time = time;
            this.lastType = lastType;
        }
    }

}
