package com.mcndsj.LobbyServerSelector.Sign;

import com.mcndsj.LobbyServerSelector.Api.SignClickQuery;
import com.mcndsj.LobbyServerSelector.LobbyServerSelector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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
    HashMap<String,SignClickQuery> query;
    List<Location> signs;

    public SignController(){
        name_type = new HashMap<>();
        type_name = new HashMap<>();
        query = new HashMap<>();
        signs = new ArrayList<Location>();
    }

    public void register(String type ,String displayName){
        name_type.put(displayName,type);
        type_name.put(type,displayName);
        LobbyServerSelector.getInstance().countUpdateSub(type);
    }


    public void register(String type , String displayName, SignClickQuery query){
        name_type.put(displayName,type);
        type_name.put(type,displayName);
        this.query.put(displayName,query);
        LobbyServerSelector.getInstance().countUpdateSub(type);
    }

    public void updateSignInfo(String type, int count){
        for(Location l : signs){
            if(ChatColor.stripColor(((Sign)l.getBlock().getState()).getLine(1)).equals(type_name.get(type))){
                Sign sign = ((Sign) l.getBlock().getState());
                if(count == -1) {
                    sign.setLine(2, ChatColor.RED + "房间全满");
                }else{
                    sign.setLine(2, count + " 队列中");
                }
                sign.update();
            }
        }
    }

    @EventHandler
    public void onPlace(SignChangeEvent evt){
        if(evt.isCancelled()){
            return;
        }
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
                    if(!signs.contains(sign.getLocation()))
                        signs.add(sign.getLocation());

                    String serverDisplayName = ChatColor.stripColor(sign.getLines()[1]);
                    if(name_type.containsKey(serverDisplayName)){
                        String typeName = name_type.get(serverDisplayName);
                        if(query.containsKey(serverDisplayName)){
                            if(!query.get(serverDisplayName).allowJoin(evt.getPlayer())){
                                return;
                            }
                        }
                        LobbyServerSelector.getInstance().addToQueue(evt.getPlayer(), typeName);

                    }else{
                        System.out.print("NOT EXIST " + serverDisplayName);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityTouch(PlayerInteractEntityEvent evt){
        if(evt.getRightClicked() != null){
            if(evt.getRightClicked().getType() == EntityType.PLAYER){
                if(evt.getRightClicked().hasMetadata("NPC")){
                    String name = ChatColor.stripColor(evt.getRightClicked().getName());
                    if(name_type.containsKey(name)){
                        String typeName = name_type.get(name);
                        if(query.containsKey(name)){
                            if(!query.get(name).allowJoin(evt.getPlayer())){
                                return;
                            }
                        }
                        LobbyServerSelector.getInstance().addToQueue(evt.getPlayer(), typeName);
                    }
                }
            }
        }
    }

}
