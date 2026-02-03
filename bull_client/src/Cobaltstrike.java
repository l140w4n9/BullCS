/*    */ import aggressor.Aggressor;
/*    */ import bean.CobaltstrikeBean;
/*    */ import bean.StagerBean;
/*    */ import common.CommonUtils;
/*    */ import java.util.Properties;
/*    */ import util.Utils;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Cobaltstrike
/*    */ {
/*    */   public static void printUsage() {
/* 14 */     System.out.println("usage: [client|version]");
/*    */   }
/*    */   
/*    */   public static void main(String[] args) {
/* 18 */     if (args.length < 1) {
/* 19 */       printUsage();
/* 20 */       System.exit(0);
/*    */     } 
/* 22 */     String exec = args[0];
/* 23 */     if (exec.equals("client")) {
/* 24 */       Properties properties = Utils.loadProperties("BullClient.properties");
/* 25 */       CobaltstrikeBean.init(properties);
/* 26 */       StagerBean.init(properties);
/* 27 */       Aggressor.main(args);
/* 28 */     } else if (exec.equals("version")) {
/* 29 */       CommonUtils.print_info("[Bull] 2025.05.01(db)");
/*    */     } else {
/* 31 */       printUsage();
/*    */     } 
/*    */   }
/*    */ }


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\Cobaltstrike.class
 * Java compiler version: 11 (55.0)
 * JD-Core Version:       1.1.3
 */