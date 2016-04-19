package com.mcndsj.LobbyServerSelector.Sign;

import com.mcndsj.LobbyServerSelector.LobbyServerSelector;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Matthew on 2016/4/19.
 */
public class SignController implements Listener{

    /**
     * sign protocol
     *  &a[加入游戏]
     *  &l 游戏名称
     *  [count] 队列中
     *  附加
     */

    //翻译表
    // 游戏名称.typeName
    HashMap<String,String> name_type;
    HashMap<String,String> type_name;
    List<Sign> signs;

    public SignController(){
        name_type = new HashMap<>();
        type_name = new HashMap<>();
        signs = new ArrayList<Sign>();
    }

    public void register(String type ,String displayName){
        name_type.put(displayName,type);
        type_name.put(type,displayName);
        LobbyServerSelector.getInstance().countUpdateSub(type);
    }

    public void updateSignInfo(String type, int count){
        for(Sign s : signs){
            if(ChatColor.stripColor(s.getLine(1)).equals(type_name.get(type))){
                s.setLine(2,count + " 队列中");
            }
        }
    }

    @EventHandler
    public void onPlace(SignChangeEvent evt){
        if(evt.isCancelled()){
            return;
        }
        int index = 0;
        for(int i = 0 ; i < evt.getLines().length ; i ++){
            evt.setLine(i, ChatColor.translateAlternateColorCodes('&',evt.getLine(i)));
        }

    }

    @EventHandler
    public void onTouch(PlayerInteractEvent evt){
        if(evt.getClickedBlock() != null && evt.getClickedBlock().getType() == Material.WALL_SIGN){
            Sign sign = (Sign) evt.getClickedBlock().getState();
            if(sign.getLines().length < 2){
                return;
            }else{
                if(ChatColor.stripColor(sign.getLines()[0]).contains("加入游戏")){
                    if(!signs.contains(sign))
                        signs.add(sign);

                    String serverDisplayName = ChatColor.stripColor(sign.getLines()[1]);
                    if(name_type.containsKey(serverDisplayName)){
                        String typeName = name_type.get(serverDisplayName);
                        LobbyServerSelector.getInstance().addToQueue(evt.getPlayer(),typeName);
                    }else{
                        System.out.print("NOT EXIST " + serverDisplayName);
                    }
                }
            }
        }
    }

}
