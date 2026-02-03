import bean.CobaltstrikeBean;
import bean.StagerBean;
import common.CommonUtils;
import java.util.Properties;
import util.Utils;

public class Cobaltstrike {
   public static void printUsage() {
      System.out.println("usage: [server|google|script|version]");
   }

   public static void main(String[] args) {
      if (args.length < 1) {
         printUsage();
         System.exit(0);
      }

      Properties properties = Utils.loadProperties("BullServer.properties");
      CobaltstrikeBean.init(properties);
      StagerBean.init(properties);
      String exec = args[0];
      if (exec.equals("server")) {
         ServerMain.main(args);
      } else if (exec.equals("google")) {
         GoogleMain.main(args);
      } else if (exec.equals("script")) {
         Script.main(args);
      } else if (exec.equals("version")) {
         CommonUtils.print_info("[Bull] 2025.05.01(db)");
      } else {
         printUsage();
      }

   }
}
