package com.zzzmode.appopsx.remote;

import com.zzzmode.appopsx.common.Shell;
import java.io.IOException;
import java.util.Locale;

/**
 * iptables -N appopsx_wifi_reject_list iptables -N appopsx_mobile_reject_list iptables -N
 * appopsx_bw
 *
 * iptables -A INPUT -j appopsx_bw iptables -A OUTPUT -j appopsx_bw
 *
 * iptables -A appopsx_bw -o wlan+ -j appopsx_wifi_reject_list iptables -A appopsx_bw -o tiwlan+ -j
 * appopsx_wifi_reject_list
 *
 * iptables -A appopsx_bw -o rmnet+ -j appopsx_mobile_reject_list iptables -A appopsx_bw -o
 * rmnet_data+ -j appopsx_mobile_reject_list iptables -A appopsx_bw -o pdp+ -j
 * appopsx_mobile_reject_list iptables -A appopsx_bw -o ppp+ -j appopsx_mobile_reject_list iptables
 * -A appopsx_bw -o uwbr+ -j appopsx_mobile_reject_list iptables -A appopsx_bw -o wimax+ -j
 * appopsx_mobile_reject_list iptables -A appopsx_bw -o vsnet+ -j appopsx_mobile_reject_list
 * iptables -A appopsx_bw -o ccmni+ -j appopsx_mobile_reject_list iptables -A appopsx_bw -o eth_x+
 * -j appopsx_mobile_reject_list
 *
 * iptables -A appopsx_wifi_reject_list -m owner --uid-owner 10255 -j REJECT --reject-with
 * icmp-port-unreachable
 *
 * iptables -D appopsx_wifi_reject_list -m owner --uid-owner 10255 -j REJECT --reject-with
 * icmp-port-unreachable
 *
 *
 * Created by zl on 2017/2/24.
 */

class IptablesController {


  private static final String RULE_REJECT = " -m owner --uid-owner %d -j REJECT --reject-with icmp-port-unreachable";

  private static final String BW_CHAIN = "appopsx_bw";
  private static final String MOBILE_DATA = "appopsx_mobile_reject_list";
  private static final String WIFI_DATA = "appopsx_wifi_reject_list";

  private static final String[] INIT_CHAIN;
  private static final String[] CHAIN_NAMES = {"-N " + MOBILE_DATA, "-N " + WIFI_DATA,
      "-N " + BW_CHAIN};

  static {
    INIT_CHAIN = new String[]{
        "INPUT -j " + BW_CHAIN,
        "OUTPUT -j " + BW_CHAIN,
        BW_CHAIN + " -o wlan+ -j " + WIFI_DATA,
        BW_CHAIN + " -o tiwlan+ -j " + WIFI_DATA,
        BW_CHAIN + " -o rmnet+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o rmnet_data+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o pdp+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o ppp+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o uwbr+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o wimax+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o vsnet+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o ccmni+ -j " + MOBILE_DATA,
        BW_CHAIN + " -o eth_x+ -j " + MOBILE_DATA,
    };
  }

  private Shell shell;

  IptablesController(Shell shell) {
    this.shell = shell;

    try {
      Shell.Result result = runIptablesCmd("-C INPUT -j " + BW_CHAIN);
      if (result.getStatusCode() != 0) {
        //
        for (String chainName : CHAIN_NAMES) {
          runIptablesCmd(chainName);
        }

        for (String s : INIT_CHAIN) {
          //first delete
          runIptablesCmd("-D " + s);
          //second add
          runIptablesCmd("-A " + s);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      saveIptables();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private boolean check(String rejectList, int uid) {
    try {
      Shell.Result result = runIptablesCmd(
          " -C " + String.format(Locale.ENGLISH, rejectList + RULE_REJECT, uid));
      System.out.println("check  " + result.getMessage() + "   " + result.getStatusCode());
      return result.getStatusCode() == 0;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return false;
  }

  private Shell.Result runIptablesCmd(String cmd) throws IOException {
    return shell.exec("iptables " + cmd);
  }


  boolean isMobileDataEnable(int uid) {
    return !check(MOBILE_DATA, uid);
  }

  boolean isWifiDataEnable(int uid) {
    return !check(WIFI_DATA, uid);
  }

  void setMobileData(int uid, boolean enable) {
    change(MOBILE_DATA, uid, enable);
  }

  void setWifiData(int uid, boolean enable) {
    change(WIFI_DATA, uid, enable);
  }

  private void change(String list, int uid, boolean enable) {
    if (enable) {
      //启用，直接删除
      try {
        runIptablesCmd("-D " + String.format(Locale.ENGLISH, list + RULE_REJECT, uid));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      //关闭
      if (!check(list, uid)) {
        try {
          runIptablesCmd("-A " + String.format(Locale.ENGLISH, list + RULE_REJECT, uid));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

    }
  }


  private void saveIptables() {
    try {
      System.out.println("saveIptables  -->>> ");
      Shell.Result result = shell.exec("iptables-save");
      if (result.getStatusCode() == 0) {
        //System.out.println(result.getMessage());

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void restoreIptables(String rules) {

  }
}
