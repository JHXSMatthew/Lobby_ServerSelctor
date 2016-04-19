package com.mcndsj.LobbyServerSelector;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mcndsj.LobbyServerSelector.Api.API;
import com.mcndsj.LobbyServerSelector.Sign.SignController;
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
        this.getServer().getMessenger().registerOutgoingPluginChannel(this,"BungeeCord");
    }

    public void sendPlayerTo(Player p , String server, boolean force){
        if(force) {

        }else{
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
    }

    public SignController getSignController(){
        return signController;
    }

    public void addToQueue(Player p , String typename){
        if(cacheQueue.containsKey(p.getName())) {
            if(cacheQueue.get(p.getName()).lastType.equals(typename)){
                if (cacheQueue.get(p.getName()).time > System.currentTimeMillis())
                    p.sendMessage("匹配中....");
                    return;
            }else
                messager.sendPlayerQuit(p,cacheQueue.get(p.getName()).lastType);
        }
        cacheQueue.put(p.getName(),new Request(System.currentTimeMillis() + 10 * 1000, typename));
        p.sendMessage("加入队列...");
        messager.addToQueue(p,typename);
    }

    public Request removeFromQueue(Player p){
        return cacheQueue.remove(p.getName());
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
