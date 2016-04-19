package com.mcndsj.LobbyServerSelector.Utils;

import com.mcndsj.LobbyServerSelector.LobbyServerSelector;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by Matthew on 2016/4/19.
 */
public class AddressUtils {
    public static String getLocalIp() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while(e.hasMoreElements()){
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()){
                InetAddress i = (InetAddress) ee.nextElement();
                if(i.getHostAddress().contains("192")){
                    return i.getHostAddress()+":"+ LobbyServerSelector.getInstance().getServer().getPort();
                }
            }
        }
        return null;
    }
}
