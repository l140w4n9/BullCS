import bean.CobaltstrikeBean;
import c2profile.Loader;
import common.Authorization;
import common.CommonUtils;
import db.SQLiteJDBC;
import java.io.File;
import java.io.IOException;
import server.TeamServer;

public class ServerMain {
   public static void main(String[] args) {
      CobaltstrikeBean cobaltstrike = CobaltstrikeBean.getInstance();
      File file = new File("beacon.db");
      if (!file.exists()) {
         CommonUtils.print_error("[Bull] 未发现数据库文件.");
         SQLiteJDBC.InitDb();
      } else {
         CommonUtils.print_info("[Bull] 数据库文件已存在.");
      }

      try {
         (new TeamServer(cobaltstrike.getHost(), cobaltstrike.getPort(), cobaltstrike.getPassword(), Loader.LoadProfile(cobaltstrike.getProfileName(), CommonUtils.resource(cobaltstrike.getProfile())), new Authorization())).B();
      } catch (IOException var4) {
         System.out.println("Cobaltstrike Boot failure!" + var4);
      }

   }
}
