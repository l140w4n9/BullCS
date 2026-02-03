/*    */ package aggressor.dialogs;
/*    */ import aggressor.Aggressor;
/*    */ import common.AObject;
import common.CommonUtils;
/*    */ import dialog.DialogUtils;
/*    */ import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;
/*    */ import javax.swing.BorderFactory;
/*    */ import javax.swing.JFrame;
/*    */ import javax.swing.JLabel;
/*    */ import javax.swing.JScrollPane;
/*    */ import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/*    */
/*    */ public class AboutDialog extends AObject {
/*    */   public void show() {
/* 14 */     JFrame var1 = DialogUtils.dialog("About", 320, 200);
/* 15 */     var1.setLayout(new BorderLayout());
/* 16 */     JLabel var2 = new JLabel(DialogUtils.getIcon("resources/logo.jpeg"));
/* 17 */     var2.setBackground(Color.black);
/* 18 */     var2.setForeground(Color.gray);
/* 19 */     var2.setOpaque(true);
/* 20 */     JTextArea var3 = new JTextArea();
/* 21 */     var3.setBackground(Color.black);
/* 22 */     var3.setForeground(Color.gray);
/* 23 */     var3.setEditable(false);
/* 24 */     var3.setFocusable(false);
/* 25 */     var3.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
/* 26 */     var3.setOpaque(false);
/* 27 */     var3.setLineWrap(true);
/* 28 */     var3.setWrapStyleWord(true);
/* 29 */     String var4 = new String(CommonUtils.readResource("resources/about.html"), StandardCharsets.UTF_8);
/* 30 */     var2.setText(var4);
/* 31 */     var3.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
/* 32 */     ((DefaultCaret)var3.getCaret()).setUpdatePolicy(1);
/* 33 */     JScrollPane var5 = new JScrollPane(var3, 22, 31);
/* 34 */     var5.setPreferredSize(new Dimension(var5.getWidth(), 100));
/* 35 */     String var6 = CommonUtils.bUTF8String(CommonUtils.readResource("resources/credits.txt"));
/* 36 */     var3.setText(var6);
/* 37 */     var5.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
/* 38 */     var1.add(var2, "Center");
/* 39 */     var1.add(var5, "South");
/* 40 */     var1.pack();
/* 41 */     var1.setLocationRelativeTo((Component)Aggressor.getFrame());
/* 42 */     var1.setVisible(true);
/*    */   }
/*    */ }


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\aggressor\dialogs\AboutDialog.class
 * Java compiler version: 11 (55.0)
 * JD-Core Version:       1.1.3
 */