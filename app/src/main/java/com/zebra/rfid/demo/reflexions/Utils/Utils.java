package com.zebra.rfid.demo.reflexions.Utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Utils
{
    public static String byteArrayToHex(byte[] a)
    {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String padRight(String inputString,int length)
    {
        return String.format("%-" + length + "s", inputString);
    }

    public static String padLeft(int data,int length)
    {
        return padLeft(String.valueOf(data),length);
    }

    public static String padLeft(String inputString,int length)
    {
        return String.format("%1$" + length + "s", inputString).replace(' ', '0');
    }

    public static String getIpAddress()
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':')<0;
                        if (isIPv4) return sAddr;
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return "";
    }

}
