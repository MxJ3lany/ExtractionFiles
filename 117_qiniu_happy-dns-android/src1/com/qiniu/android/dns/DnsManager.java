package com.qiniu.android.dns;

import android.os.Build;

import com.qiniu.android.dns.http.DomainNotOwn;
import com.qiniu.android.dns.local.Hosts;
import com.qiniu.android.dns.util.LruCache;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * DNS解析管理类，可以重复使用
 */
public final class DnsManager {

    private final IResolver[] resolvers;
    private final LruCache<String, Record[]> cache;
    private final Hosts hosts = new Hosts();
    private final IpSorter sorter;
    private volatile NetworkInfo info = null;
    private volatile int index = 0;

    /**
     * @param info      当前的网络信息，从Android context中获取
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     */
    public DnsManager(NetworkInfo info, IResolver[] resolvers) {
        this(info, resolvers, null);
    }

    /**
     * @param info      当前的网络信息，从Android context中获取
     * @param resolvers 具体的dns 解析示例，可以是local或者httpdns
     * @param sorter    外部接口，对获取的IP数组进行排序
     */
    public DnsManager(NetworkInfo info, IResolver[] resolvers, IpSorter sorter) {
        this.info = info == null ? NetworkInfo.normal : info;
        this.resolvers = resolvers.clone();
        cache = new LruCache<>();
        this.sorter = sorter == null ? new DummySorter() : sorter;
    }

    private static Record[] trimCname(Record[] records) {
        ArrayList<Record> a = new ArrayList<>(records.length);
        for (Record r : records) {
            if (r != null && r.type == Record.TYPE_A) {
                a.add(r);
            }
        }
        return a.toArray(new Record[a.size()]);
    }

    private static void rotate(Record[] records) {
        if (records != null && records.length > 1) {
            Record first = records[0];
            System.arraycopy(records, 1, records, 0, records.length - 1);
            records[records.length - 1] = first;
        }
    }

    private static String[] records2Ip(Record[] records) {
        if (records == null || records.length == 0) {
            return null;
        }
        ArrayList<String> a = new ArrayList<>(records.length);
        for (Record r : records) {
            a.add(r.value);
        }
        if (a.size() == 0) {
            return null;
        }
        return a.toArray(new String[a.size()]);
    }

    public static boolean validIP(String ip) {
        if (ip == null || ip.length() < 7 || ip.length() > 15) return false;
        if (ip.contains("-")) return false;

        try {
            int x = 0;
            int y = ip.indexOf('.');

            if (y != -1 && Integer.parseInt(ip.substring(x, y)) > 255) return false;

            x = ip.indexOf('.', ++y);
            if (x != -1 && Integer.parseInt(ip.substring(y, x)) > 255) return false;

            y = ip.indexOf('.', ++x);
            return !(y != -1 && Integer.parseInt(ip.substring(x, y)) > 255 &&
                    Integer.parseInt(ip.substring(++y, ip.length() - 1)) > 255 &&
                    ip.charAt(ip.length() - 1) != '.');

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 简单通过时区判断是否在大陆
    public static boolean needHttpDns() {
        try {
            TimeZone zone = TimeZone.getDefault();
            String id = zone.getID();
            return ("Asia/Shanghai".equals(id) || "Asia/Chongqing".equals(id) ||
                    "Asia/Harbin".equals(id) || "Asia/Urumqi".equals(id));

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 查询域名
     *
     * @param domain 域名
     * @return ip 列表
     * @throws IOException 网络异常或者无法解析抛出异常
     */
    public String[] query(String domain) throws IOException {
        return query(new Domain(domain));
    }

    public String[] query(Domain domain) throws IOException {
        if (domain == null) {
            throw new IOException("null domain");
        }
        if (domain.domain == null || domain.domain.trim().length() == 0) {
            throw new IOException("empty domain " + domain.domain);
        }

        if (validIP(domain.domain)) {
            return new String[]{domain.domain};
        }

        String[] r = queryInternal(domain);
        if (r == null || r.length <= 1) {
            return r;
        }
        return sorter.sort(r);
    }

    /**
     * 查询域名
     *
     * @param domain 域名参数
     * @return ip 列表
     * @throws IOException 网络异常或者无法解析抛出异常
     */

    private String[] queryInternal(Domain domain) throws IOException {
//        有些手机网络状态可能不对
//        if (info.netStatus == NetworkInfo.NO_NETWORK){
//            return null;
//        }
        Record[] records = null;
        if (domain.hostsFirst) {
            String[] ret = hosts.query(domain, info);
            if (ret != null && ret.length != 0) {
                return ret;
            }
        }
        synchronized (cache) {
            if (info.equals(NetworkInfo.normal) && Network.isNetworkChanged()) {
                cache.clear();
                synchronized (resolvers) {
                    index = 0;
                }
            } else {
                records = cache.get(domain.domain);
                if (records != null && records.length != 0) {
                    if (!records[0].isExpired()) {
                        if (records.length > 1) {
                            rotate(records);
                        }
                        return records2Ip(records);
                    } else {
                        records = null;
                    }
                }
            }
        }

        IOException lastE = null;
        int firstOk = index;
        for (int i = 0; i < resolvers.length; i++) {
            int pos = (firstOk + i) % resolvers.length;
            NetworkInfo before = info;
            String ip = Network.getIp();
            try {
                records = resolvers[pos].resolve(domain, info);
            } catch (DomainNotOwn e) {
                continue;
            } catch (IOException e) {
                lastE = e;
                e.printStackTrace();
            } catch (Exception e2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    lastE = new IOException(e2);
                }
                e2.printStackTrace();
            }
            String ip2 = Network.getIp();
            if (info == before && (records == null || records.length == 0) && ip.equals(ip2)) {
                synchronized (resolvers) {
                    if (index == firstOk) {
                        index++;
                        if (index == resolvers.length) {
                            index = 0;
                        }
                    }
                }
            } else {
                break;
            }
        }

        if (records == null || records.length == 0) {
            if (!domain.hostsFirst) {
                String[] rs = hosts.query(domain, info);
                if (rs != null && rs.length != 0) {
                    return rs;
                }
            }
            if (lastE != null) {
                throw lastE;
            }
            throw new UnknownHostException(domain.domain);
        }
        records = trimCname(records);
        if (records.length == 0) {
            throw new UnknownHostException("no A records");
        }
        synchronized (cache) {
            cache.put(domain.domain, records);
        }
        return records2Ip(records);
    }

    public InetAddress[] queryInetAdress(Domain domain) throws IOException {
        String[] ips = query(domain);
        InetAddress[] addresses = new InetAddress[ips.length];
        for (int i = 0; i < ips.length; i++) {
            addresses[i] = InetAddress.getByName(ips[i]);
        }
        return addresses;
    }

    /**
     * 当网络发生变化时，通知当前的网络信息
     *
     * @param info 网络信息
     */
    public void onNetworkChange(NetworkInfo info) {
        clearCache();
        this.info = info == null ? NetworkInfo.normal : info;
        synchronized (resolvers) {
            index = 0;
        }
    }

    private void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain   域名
     * @param ip       ip
     * @param provider 运营商，见 NetworkInfo
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, String ip, int provider) {
        hosts.put(domain, new Hosts.Value(ip, provider));
        return this;
    }

    /**
     * 插入指定运营商的hosts配置
     *
     * @param domain 域名
     * @param ip     ip
     * @return 当前的DnsManager，便于链式调用
     */
    public DnsManager putHosts(String domain, String ip) {
        hosts.put(domain, ip);
        return this;
    }

    private static class DummySorter implements IpSorter {
        private AtomicInteger pos = new AtomicInteger();

        @Override
        public String[] sort(String[] ips) {
            return ips;
        }
    }
}
