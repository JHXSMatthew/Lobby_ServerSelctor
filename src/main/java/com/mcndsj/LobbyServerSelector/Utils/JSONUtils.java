package com.mcndsj.LobbyServerSelector.Utils;

import com.mcndsj.lobby_Vip.LobbyVip;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

/**
 * Created by Matthew on 2016/4/19.
 */
public class JSONUtils {

    public static String encodePlayer(Player p, String lobbyName , boolean isVip){
        JSONObject obj = new JSONObject();
        obj.put("name",p.getName());
        obj.put("lobby",lobbyName);
        obj.put("vip", isVip);
        return obj.toJSONString();
    }
}
