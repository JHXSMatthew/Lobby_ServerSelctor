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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
                try {
                    JedisUtils.get().subscribe(new JedisSubPubHandler(),"ServerManage.ServerNameQuery." + AddressUtils.getLocalIp());
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            JedisUtils.get().publish("ServerManage.ServerNameQuery",AddressUtils.getLocalIp());
            System.out.print("ServerManage.ServerNameQuery" + AddressUtils.getLocalIp());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }


    public void countSub(final String type){
        thread.execute(new Runnable() {
            @Override
            public void run() {
                if(count.isSubscribed()){
                    count.subscribe("QueueCount." + type);
                }else{
                    JedisUtils.get().subscribe(count,"QueueCount." + type);
                }

            }
        });

    }


    public void addToQueue(Player p,String type){
        JedisUtils.get().publish("QueueJoin." + type ,JSONUtils.encodePlayer(p,lobbyTypeName + number,LobbyVip.getApi().isVip(p.getName())));
    }

    public void sendPlayerQuit(Player p , String type){
        JedisUtils.get().publish("playerOffline." + type ,JSONUtils.encodePlayer(p,lobbyTypeName + number,LobbyVip.getApi().isVip(p.getName())));

    }

    public void unsubscribe(){
        for(JedisPubSub h : subs){
            h.unsubscribe();
        }

    }

    private void createLobbySendListener(){
        thread.execute(new Runnable() {
            @Override
            public void run() {
                sendSubPubHandler temp = new sendSubPubHandler();
                subs.add(temp);
                JedisUtils.get().subscribe(temp,"ServerSend."+ lobbyTypeName +number);
            }
        });
    }





    public class JedisSubPubHandler extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
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
                LobbyServerSelector.getInstance().removeFromQueue(Bukkit.getPlayer((String)obj.get("name")));
                LobbyServerSelector.getInstance().sendPlayerTo(Bukkit.getPlayer((String)obj.get("name")),(String)obj.get("server"),false);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    public class CountPubSub extends JedisPubSub{
        @Override
        public void onMessage(String channel, String message) {
            StringTokenizer stoken = new StringTokenizer(channel, ".");
            if(!stoken.hasMoreTokens()){
                return;
            }
            stoken.nextToken();
            String typeName = stoken.nextToken();

            int count = Integer.parseInt(message);
            LobbyServerSelector.getInstance().getSignController().updateSignInfo(typeName,count);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent evt){
        LobbyServerSelector.Request r = LobbyServerSelector.getInstance().removeFromQueue(evt.getPlayer());
        if(r != null){
            sendPlayerQuit(evt.getPlayer(),r.lastType);
        }
    }

}
