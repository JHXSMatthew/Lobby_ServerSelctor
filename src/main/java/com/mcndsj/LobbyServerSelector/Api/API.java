package com.mcndsj.LobbyServerSelector.Api;

import com.mcndsj.LobbyServerSelector.LobbyServerSelector;

/**
 * Created by Matthew on 2016/4/19.
 */
public class API {
    public void register(String type, String displayName){
        LobbyServerSelector.getInstance().getSignController().register(type,displayName);
    }

    public void register(String type, String displayName,SignClickQuery query){
        LobbyServerSelector.getInstance().getSignController().register(type,displayName,query);
    }
}
