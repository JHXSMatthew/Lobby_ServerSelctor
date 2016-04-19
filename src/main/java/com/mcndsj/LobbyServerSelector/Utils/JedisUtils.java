package com.mcndsj.LobbyServerSelector.Utils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Matthew on 2016/4/19.
 */
public class JedisUtils {
    private static JedisPool pool =null;
    public static redis.clients.jedis.Jedis get(){
        if(pool == null){
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(10);
            pool = new JedisPool(config,"192.168.123.2",6379,0,"l8VZl0HZ#fYKERkhfjyv(aezzN__WT^b");
        }

        return pool.getResource();
    }
}
