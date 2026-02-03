/*    */ package aggressor.dialogs;
/*    */ 
/*    */ import aggressor.MultiFrame;
/*    */ import aggressor.Prefs;
/*    */
/*    */ import aggressor.browsers.Connect;
import common.Starter2;
/*    */ import dialog.DialogUtils;
/*    */ import java.awt.Component;
/*    */ import java.awt.event.ActionEvent;
/*    */ import java.awt.event.ActionListener;
/*    */ import java.awt.event.WindowAdapter;
/*    */ import java.awt.event.WindowEvent;
/*    */ import java.util.Iterator;
/*    */ import java.util.List;
/*    */ import javax.swing.Icon;
/*    */ import javax.swing.JButton;
/*    */ import javax.swing.JFrame;
/*    */ import ui.Navigator;
/*    */ 
/*    */ public class ConnectDialog extends Starter2 {
/*    */   protected MultiFrame window;
/* 22 */   protected Navigator options = null;
/*    */   protected boolean useAliasName;
/* 24 */   protected JButton viewAliasName = null;
/* 25 */   protected JButton viewHostName = null;
/*    */   
/*    */   public ConnectDialog(MultiFrame var1) {
/* 28 */     this.window = var1;
/* 29 */     this.useAliasName = Prefs.getPreferences().isSet("connection.view.alias.boolean", false);
/* 30 */     initialize(getClass());
/*    */   }
/*    */ 
/*    */   
/*    */   public void show() {
/* 35 */     boolean var1 = false;
/* 36 */     String var2 = Prefs.getPreferences().getString("connection.last", "New Profile");
/*    */     
/* 38 */     JFrame var3 = DialogUtils.dialog("Cobalt Strike[Bull 🐂] by:l140w4n9[精卫攻防实验室]", 640, 480);
/* 39 */     var3.addWindowListener(new WindowAdapter() {
/*    */           public void windowClosing(WindowEvent var1) {
/* 41 */             ConnectDialog.this.window.closeConnect();
/*    */           }
/*    */         });
/* 44 */     this.options = new Navigator();
/* 45 */     this.options.addPage("New Profile", (Icon)null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", (new Connect(this.window)).getContent(var3, "neo", "password", "127.0.0.1", "50050", "neo@127.0.0.1"));
/* 46 */     List var4 = Prefs.getPreferences().getList("connection.profiles");
/* 47 */     Iterator<String> var5 = var4.iterator();
/*    */     
/* 49 */     while (var5.hasNext()) {
/* 50 */       String var6 = var5.next();
/* 51 */       String var7 = Prefs.getPreferences().getString("connection.profiles." + var6 + ".user", "neo");
/* 52 */       String var8 = Prefs.getPreferences().getString("connection.profiles." + var6 + ".password", "password");
/*    */       
/* 54 */       String var9 = Prefs.getPreferences().getString("connection.profiles." + var6 + ".port", "50050");
/* 55 */       String var10 = Prefs.getPreferences().getString("connection.profiles." + var6 + ".alias", var7 + "@" + var7);
/* 56 */       String var11 = this.window.isConnected(var6) ? "*" : "";
/* 57 */       String var12 = var11 + var11 + "!!" + var6 + var11;
/* 58 */       this.options.addPage(var12, (Icon)null, "This is the connect dialog. You should use it to connect to a Cobalt Strike (Aggressor) team server.", (new Connect(this.window)).getContent(var3, var7, var8, var6, var9, var10));
/* 59 */       if (var2.equals(var6)) {
/* 60 */         var1 = true;
/* 61 */         var2 = var12;
/*    */       } 
/*    */     } 
/*    */     
/* 65 */     this.options.set(var1 ? var2 : "New Profile");
/* 66 */     this.options.useAlternateValue(this.useAliasName);
/* 67 */     this.viewAliasName = new JButton("Alias Names");
/* 68 */     this.viewAliasName.addActionListener(new ActionListener() {
/*    */           public void actionPerformed(ActionEvent var1) {
/* 70 */             ConnectDialog.this.options.useAlternateValue(true);
/* 71 */             ConnectDialog.this.viewAliasName.setFont(ConnectDialog.this.viewAliasName.getFont().deriveFont(1));
/* 72 */             ConnectDialog.this.viewHostName.setFont(ConnectDialog.this.viewHostName.getFont().deriveFont(0));
/*    */           }
/*    */         });
/* 75 */     this.viewHostName = new JButton("Host Names");
/* 76 */     this.viewHostName.addActionListener(new ActionListener() {
/*    */           public void actionPerformed(ActionEvent var1) {
/* 78 */             ConnectDialog.this.options.useAlternateValue(false);
/* 79 */             ConnectDialog.this.viewHostName.setFont(ConnectDialog.this.viewHostName.getFont().deriveFont(1));
/* 80 */             ConnectDialog.this.viewAliasName.setFont(ConnectDialog.this.viewAliasName.getFont().deriveFont(0));
/*    */           }
/*    */         });
/* 83 */     var3.add(DialogUtils.center(this.viewAliasName, this.viewHostName), "North");
/* 84 */     var3.add((Component)this.options, "Center");
/* 85 */     var3.pack();
/* 86 */     if (this.useAliasName) {
/* 87 */       this.viewAliasName.setFont(this.viewAliasName.getFont().deriveFont(1));
/* 88 */       this.viewAliasName.requestFocusInWindow();
/*    */     } else {
/* 90 */       this.viewHostName.setFont(this.viewHostName.getFont().deriveFont(1));
/* 91 */       this.viewHostName.requestFocusInWindow();
/*    */     } 
/*    */     
/* 94 */     var3.setVisible(true);
/*    */   }
/*    */ }


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\aggressor\dialogs\ConnectDialog.class
 * Java compiler version: 11 (55.0)
 * JD-Core Version:       1.1.3
 */