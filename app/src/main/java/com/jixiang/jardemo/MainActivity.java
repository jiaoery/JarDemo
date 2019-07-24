package com.jixiang.jardemo;

import android.net.IpConfiguration;
import android.net.LinkAddress;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.net.StaticIpConfiguration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.InetAddress;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStaticIp("192.168.1.110","192.168.1.1" , "255.255.255.0", "8.8.8.8", "114.114.114.114");

    }


    public static boolean setStaticIp(String ip, String mask, String gateway, String dns1, String dns2) {
        try {
            //每个IpConfiguration对象内部都包含了一个StaticIpConfiguration对象，对于DHCP方式来说这个对象赋为null
            //用于保存静态IP、dns、gateway、netMask相关参数配置
            StaticIpConfiguration staticIpConfiguration = new StaticIpConfiguration();
            //把192.168.1.1这种格式字符串转化为IP地址对象
            InetAddress mIpAddr = NetworkUtils.numericToInetAddress(ip);
            String[] strs = mask.split("\\.");
            int count = 0;
            for (String str : strs) {
                if ("255".equals(str)) {
                    count++;
                }
            }
            int prefixLength = count * 8;
            //prefixLength就是表示子网掩码字符有几个255，比如255.255.255.0的prefixLength为3
            LinkAddress mIpAddress = new LinkAddress(mIpAddr, prefixLength);
            //默认网关
            InetAddress mGateway = NetworkUtils.numericToInetAddress(gateway);
            //DNS
            ArrayList<InetAddress> mDnsServers = new ArrayList<InetAddress>();
            mDnsServers.add(NetworkUtils.numericToInetAddress(dns1));
            mDnsServers.add(NetworkUtils.numericToInetAddress(dns2));

            staticIpConfiguration.ipAddress = mIpAddress;
            staticIpConfiguration.gateway = mGateway;
            staticIpConfiguration.dnsServers.addAll(mDnsServers);

            //ProxySettings为代理服务配置，主要有STATIC（手动代理）、PAC（自动代理）两种，NONE为不设置代理，UNASSIGNED为未配置代理（framework会使用NONE替代它）
            //ProxyInfo包含代理配置信息
            IpConfiguration
                    config = new IpConfiguration(IpConfiguration.IpAssignment.STATIC, IpConfiguration.ProxySettings.NONE, staticIpConfiguration, ProxyInfo
                    .buildDirectProxy(null, 0));
//            KpApplication.mEthManager.setConfiguration(config);//执行该方法后，系统会先通过EthernetConfigStore保存IP配置到data/misc/ethernet/ipconfig.txt，再更新以太网配置、通过EthernetNetworkFactory重启eth设备（最终通过NetworkManagementService来操作开启关闭设备、更新状态）
            //NetworkManagementService服务中提供了各种直接操作eth设备的API，如开关、列举、读写配置eth设备，都是通过发送指令实现与netd通信
            //Netd 就是Network Daemon 的缩写，表示Network守护进程，Netd负责跟一些涉及网络的配置，操作，管理，查询等相关的功能实现

            return true;
        } catch (Exception e) {
            Log.e("LOGTAG_WEB", "SetIPHandler IP设置异常:"+e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
