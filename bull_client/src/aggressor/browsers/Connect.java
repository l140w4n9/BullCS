/*     */ package aggressor.browsers;
/*     */ 
/*     */ import aggressor.Aggressor;
/*     */
/*     */ import aggressor.AggressorClient;
import aggressor.MultiFrame;
/*     */ import aggressor.Prefs;
/*     */
/*     */ import aggressor.dialogs.ConnectDialog;
import bean.CobaltstrikeBean;
/*     */ import common.Callback;
/*     */ import common.CommonUtils;
/*     */ import common.TeamQueue;
/*     */ import common.TeamSocket;
/*     */ import dialog.DialogListener;
/*     */ import dialog.DialogManager;
/*     */ import dialog.DialogUtils;
/*     */ import dialog.SafeDialogCallback;
/*     */ import dialog.SafeDialogs;
/*     */ import java.awt.BorderLayout;
/*     */ import java.awt.Component;
/*     */ import java.awt.event.ActionEvent;
/*     */ import java.awt.event.ActionListener;
/*     */ import java.util.HashSet;
/*     */ import java.util.Map;
/*     */ import javax.swing.JButton;
/*     */ import javax.swing.JComponent;
/*     */ import javax.swing.JFrame;
/*     */ import javax.swing.JOptionPane;
/*     */ import javax.swing.JPanel;
/*     */ import ssl.ArmitageTrustListener;
/*     */ import ssl.SecureSocket;
/*     */ 
/*     */ public class Connect
/*     */   implements DialogListener, Callback, ArmitageTrustListener
/*     */ {
/*  35 */   CobaltstrikeBean cobaltstrike = CobaltstrikeBean.getInstance();
/*     */   protected MultiFrame window;
/*  37 */   protected JButton viewUpData = null;
/*  38 */   protected TeamQueue tqueue = null;
/*  39 */   protected String alias = "";
/*  40 */   protected Map m_options = null;
/*     */   private boolean Ď = false;
/*  42 */   private String č = null;
/*  43 */   private String ď = null;
/*     */   
/*     */   public Connect(MultiFrame var1) {
/*  46 */     this.window = var1;
/*     */   }
/*     */   
/*     */   public Connect(MultiFrame var1, boolean var2, String var3) {
/*  50 */     this.window = var1;
/*  51 */     this.Ď = var2;
/*  52 */     this.č = var3;
/*     */   }
/*     */   
/*     */   public boolean trust(String var1) {
/*  56 */     HashSet var2 = new HashSet(Prefs.getPreferences().getList("trusted.servers"));
/*  57 */     if (var2.contains(var1)) {
/*  58 */       return true;
/*     */     }
/*  60 */     int var3 = JOptionPane.showConfirmDialog((Component)null, "The team server's fingerprint is:\n\n<html><body><b>" + var1 + "</b></body></html>\n\nDoes this match the fingerprint shown when the team server started?", "VerifyFingerprint", 0);
/*  61 */     if (var3 == 0) {
/*  62 */       Prefs.getPreferences().appendList("trusted.servers", var1);
/*  63 */       Prefs.getPreferences().save();
/*  64 */       return true;
/*     */     } 
/*  66 */     return false;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean A(Map var1) {
/*  72 */     String var2 = "" + var1.get("user");
/*  73 */     String var3 = "" + var1.get("host");
/*  74 */     String var4 = "" + var1.get("port");
/*  75 */     String var5 = "" + var1.get("alias");
/*  76 */     StringBuilder var6 = new StringBuilder();
/*  77 */     if (CommonUtils.isNullOrEmpty(var5)) {
/*  78 */       var6.append(((var6.length() > 0) ? "\n" : "") + "Alias name can not be empty.");
/*     */     } else {
/*  80 */       if ('*' == var5.charAt(0)) {
/*  81 */         var6.append(((var6.length() > 0) ? "\n" : "") + "Alias name can not start with *.");
/*     */       }
/*     */       
/*  84 */       if (this.window.checkCollision(var5)) {
/*  85 */         var6.append(((var6.length() > 0) ? "\n" : "") + "Alias name already in use.");
/*     */       }
/*     */     } 
/*     */     
/*  89 */     if (CommonUtils.isNullOrEmpty(var3)) {
/*  90 */       var6.append(((var6.length() > 0) ? "\n" : "") + "Host name can not be empty.");
/*     */     }
/*     */     
/*  93 */     if (CommonUtils.isNullOrEmpty(var4)) {
/*  94 */       var6.append(((var6.length() > 0) ? "\n" : "") + "Port can not be empty.");
/*  95 */     } else if (!CommonUtils.isNumber(var4)) {
/*  96 */       var6.append(((var6.length() > 0) ? "\n" : "") + "Port needs to be a number.");
/*     */     } 
/*     */     
/*  99 */     if (CommonUtils.isNullOrEmpty(var2)) {
/* 100 */       var6.append(((var6.length() > 0) ? "\n" : "") + "User name can not be empty.");
/*     */     }
/*     */     
/* 103 */     if (var6.length() > 0) {
/* 104 */       Prefs.getPreferences().set("connection.last", (this.ď != null) ? this.ď : "New Profile");
/* 105 */       (new ConnectDialog(this.window)).show();
/* 106 */       DialogUtils.showError(var6.toString());
/* 107 */       return false;
/*     */     } 
/* 109 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   public void dialogAction(ActionEvent var1, Map var2) {
/* 114 */     if (A(var2)) {
/* 115 */       this.m_options = var2;
/* 116 */       String var3 = "" + var2.get("user");
/* 117 */       String var4 = "" + var2.get("host");
/* 118 */       String var5 = "" + var2.get("port");
/* 119 */       String var6 = "" + var2.get("pass");
/* 120 */       this.alias = "" + var2.get("alias");
/* 121 */       Prefs.getPreferences().set("connection.last", var4);
/* 122 */       Prefs.getPreferences().appendList("connection.profiles", var4);
/* 123 */       Prefs.getPreferences().set("connection.profiles." + var4 + ".user", var3);
/* 124 */       Prefs.getPreferences().set("connection.profiles." + var4 + ".port", var5);
/* 125 */       Prefs.getPreferences().set("connection.profiles." + var4 + ".password", var6);
/* 126 */       Prefs.getPreferences().set("connection.profiles." + var4 + ".alias", this.alias);
/* 127 */       Prefs.getPreferences().save();
/*     */       
/*     */       try {
/* 130 */         SecureSocket var7 = new SecureSocket(var4, Integer.parseInt(var5), this);
/*     */         
/* 132 */         if (this.cobaltstrike.getOpenAuth().booleanValue()) {
/*     */           
/* 134 */           String var99 = "" + var2.get("code");
/* 135 */           var6 = var6 + "@::@" + var6;
/*     */         } 
/* 137 */         var7.authenticate(var6);
/* 138 */         this.tqueue = new TeamQueue(new TeamSocket(var7.getSocket()));
/* 139 */         this.tqueue.call("aggressor.authenticate", CommonUtils.args(var3, var6, Aggressor.VERSION), this);
/* 140 */       } catch (Exception var9) {
/* 141 */         String var8 = this.Ď ? "again?" : "another connection?";
/* 142 */         SafeDialogs.askYesNoBoth(var9.getMessage() + "\n\nA Cobalt Strike team server is not available on\nthe specified host and port. You must start a\nCobalt Strike team server first.\n\nWould you like to try " + var9.getMessage(), "Connection Error", new SafeDialogCallback() {
/*     */               public void dialogResult(String var1) {
/* 144 */                 if ("no".equals(var1)) {
/* 145 */                   CommonUtils.runSafe(new Runnable() {
/*     */                         public void run() {
/* 147 */                           Connect.this.window.quit(Connect.this.m_options);
/*     */                         }
/*     */                       });
/*     */                 }
/* 151 */                 else if (Connect.this.Ď) {
/* 152 */                   (new Connect(Connect.this.window, true, Connect.this.alias)).dialogAction((ActionEvent)null, Connect.this.m_options);
/*     */                 } else {
/* 154 */                   (new ConnectDialog(Connect.this.window)).show();
/*     */                 } 
/*     */               }
/*     */             });
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void result(String var1, Object var2) {
/* 166 */     if ("aggressor.authenticate".equals(var1)) {
/* 167 */       String var3 = "" + var2;
/* 168 */       if (var3.equals("SUCCESS")) {
/* 169 */         this.tqueue.call("aggressor.metadata", CommonUtils.args(Long.valueOf(System.currentTimeMillis())), this);
/*     */       } else {
/* 171 */         DialogUtils.showError(var3);
/* 172 */         this.tqueue.close();
/*     */       } 
/* 174 */     } else if ("aggressor.metadata".equals(var1)) {
/* 175 */       final AggressorClient var4 = new AggressorClient(this.window, this.tqueue, (Map)var2, this.m_options);
/* 176 */       CommonUtils.runSafe(new Runnable() {
/*     */             public void run() {
/* 178 */               Connect.this.window.addButton(Connect.this.alias, var4, (Connect.this.č == null) ? Connect.this.alias : Connect.this.č);
/* 179 */               var4.showTime();
/*     */             }
/*     */           });
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public JComponent getContent(JFrame var1, String var2, String var3, String var4, String var5, String var6) {
/* 188 */     JPanel var7 = new JPanel();
/* 189 */     var7.setLayout(new BorderLayout());
/* 190 */     this.ď = var4;
/* 191 */     DialogManager var8 = new DialogManager(var1);
/* 192 */     var8.addDialogListener(this);
/* 193 */     var8.set("user", var2);
/* 194 */     var8.set("pass", var3);
/* 195 */     var8.set("host", var4);
/* 196 */     var8.set("port", var5);
/* 197 */     var8.set("alias", var6);
/* 198 */     var8.text("alias", "Alias:", 20);
/* 199 */     var8.text("host", "Host:", 20);
/* 200 */     var8.text("port", "Port:", 10);
/* 201 */     var8.text("user", "User:", 20);
/* 202 */     var8.password("pass", "Password:", 20);
/* 203 */     if (this.cobaltstrike.getOpenAuth().booleanValue()) {
/* 204 */       var8.text("code", "Auth:", 10);
/*     */     }
/* 206 */     this.viewUpData = new JButton("README.md");
/* 207 */     this.viewUpData.addActionListener(new ActionListener()
/*     */         {
/*     */           public void actionPerformed(ActionEvent e) {
/* 210 */             JOptionPane.showMessageDialog(null, "原catcs二开功能移植\n公牛Cs:基于Cobalt Strike[4.5]二开\n[内部使用]\nps:精卫攻防实验室.\n\nBy:l140w4n9\nTime:2025-05-01");
/*     */           }
/*     */         });
/*     */ 
/*     */ 
/*     */     
/* 216 */     JButton var9 = var8.action("Connect");
/* 217 */     JButton var10 = var8.help("https://mp.weixin.qq.com/s/zOtqFnWFRdX64E2KxV5Rpg");
/* 218 */     var7.add(var8.layout(), "Center");
/* 219 */     var7.add(DialogUtils.center(var9, this.viewUpData, var10), "South");
/* 220 */     return var7;
/*     */   }
/*     */ }


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\aggressor\browsers\Connect.class
 * Java compiler version: 11 (55.0)
 * JD-Core Version:       1.1.3
 */