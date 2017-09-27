package com.mcndsj.LobbyServerSelector;

import com.mcndsj.LobbyServerSelector.Utils.AddressUtils;
import com.mcndsj.LobbyServerSelector.Utils.JSONUtils;
import com.mcndsj.LobbyServerSelector.Utils.JedisUtils;
import com.mcndsj.LobbyServerSelector.Utils.WordUtils;
import com.mcndsj.lobby_Vip.LobbyVip;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Matthew on 2016/4/18.
 */
public class JedisMessager implements org.bukkit.event.Listener{

    ExecutorService thread;
    Set<JedisPubSub> subs;
    String lobbyTypeName = null;
    int number = 0;
    CountPubSub count ;


    public JedisMessager(){
        thread = Executors.newCachedThreadPool();
        subs = new HashSet<>();
        count = new CountPubSub();
        subs.add(count);

        thread.execute(new Runnable() {
            @Override
            public void run() {
                try (Jedis j  =  JedisUtils.get()){
                    j.subscribe(new JedisSubPubHandler(),"ServerManage.ServerNameQuery." + AddressUtils.getLocalIp());
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        new BukkitRunnable(){

            @Override
            public void run() {
                if(lobbyTypeName != null){
                    cancel();
                }

                try {
                    Jedis j = JedisUtils.get();
                    j.publish("ServerManage.ServerNameQuery",AddressUtils.getLocalIp());
                    j.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimer(LobbyServerSelector.getInstance(),0,20);


        System.out.print("Init JedisManager done!");

    }


    public void countSub(final String type){
        thread.execute(new Runnable() {
            @Override
            public void run() {
                if(count.isSubscribed()){
                    count.subscribe("QueueCount." + type);
                }else{
                    try (Jedis j  =  JedisUtils.get()) {
                        j.subscribe(count, "QueueCount." + type);
                        System.out.print("-------------------- QueueCount Timeout ---------------------");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

            }
        });

    }


    public void addToQueue(Player p,String type){
        try {
            String msg = JSONUtils.encodePlayer(p, lobbyTypeName + number, LobbyVip.getApi().isVip(p.getName()));
            JedisUtils.publish("QueueJoin." + type ,msg);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void sendPlayerQuit(Player p , String type){
        try {
            String msg = JSONUtils.encodePlayer(p,lobbyTypeName + number,LobbyVip.getApi().isVip(p.getName()));
            JedisUtils.publish("playerOffline." + type ,msg);
        }catch(Exception e){
            e.printStackTrace();
        }


    }

    public void unsubscribe(){
        for(JedisPubSub h : subs){
            try {
                h.unsubscribe();
            }catch(Exception e){

            }
        }

    }

    private void createLobbySendListener(){
        thread.execute(new Runnable() {
            @Override
            public void run() {
                sendSubPubHandler temp = new sendSubPubHandler();
                subs.add(temp);
                try (Jedis j  =  JedisUtils.get()){
                    j.subscribe(temp, "ServerSend." + lobbyTypeName + number);
                    System.out.print("-------------------- ServerSend Timeout ---------------------");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }





    public class JedisSubPubHandler extends JedisPubSub{
        boolean isReceived = false;
        @Override
        public void onMessage(String channel, String message) {
            if(isReceived){
                return;
            }
            isReceived = true;
            lobbyTypeName = message.replace(String.valueOf(WordUtils.getIntFromString(message)),"");
            number = WordUtils.getIntFromString(message);
            System.out.print(" ----------- ServerName " + lobbyTypeName + " number: " + number + " -----------------");

            unsubscribe();
            subs.remove(this);
            createLobbySendListener();
        }
    }

    public class sendSubPubHandler extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
            JSONParser parser = new JSONParser();
            try {
                Object ojb = parser.parse(message);
                JSONObject obj = (JSONObject) ojb;
                Player p = Bukkit.getPlayer((String)obj.get("name"));
                LobbyServerSelector.getInstance().removeFromQueue(p);
                //System.out.print("Message received " +channel + " msg" + message);
                LobbyServerSelector.getInstance().sendPlayerTo(Bukkit.getPlayer((String)obj.get("name")),(String)obj.get("server"),false);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public class CountPubSub extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
            StringTokenizer token = new StringTokenizer(channel, ".");
            if(!token.hasMoreTokens()){
                return;
            }
            token.nextToken();
            String typeName = token.nextToken();
            int count = Integer.parseInt(message);

            LobbyServerSelector.getInstance().getSignController().updateSignInfo(typeName,count);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt){
        try {
            LobbyServerSelector.Request r = LobbyServerSelector.getInstance().removeFromQueue(evt.getPlayer());
            if(r != null){
                sendPlayerQuit(evt.getPlayer(),r.lastType);
            }
        }catch(Exception e){
        //TODO: add handles ?
        }

    }

}
