package db;

import common.CommonUtils;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class SQLiteJDBC {
   public static void InitDb() {
      Connection c = null;
      Statement stmt = null;

      try {
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:beacon.db");
         CommonUtils.print_info("[Bull] 数据库创建成功!");
         stmt = c.createStatement();
         String sql = "CREATE TABLE Beacon (Id INTEGER PRIMARY KEY AUTOINCREMENT, BeaconId        CHAR(50),  Hash        CHAR(50),  StartTime        CHAR(50),  External       CHAR(50),  Internal       CHAR(50),  Computer       CHAR(50),  Process        CHAR(50),  User        CHAR(50),  Arch        CHAR(50),  Note         CHAR(50), UpdateNoteTime         CHAR(50))";
         stmt.executeUpdate(sql);
         if (stmt != null) {
            stmt.close();
         }

         if (c != null) {
            c.close();
         }
      } catch (Exception var3) {
         PrintStream var10000 = System.err;
         String var10001 = var3.getClass().getName();
         var10000.println(var10001 + ": " + var3.getMessage());
         System.exit(0);
      }

      CommonUtils.print_info("[Bull] 数据库初始化成功!");
   }

   public static Connection OpenDb() {
      Connection c = null;

      try {
         Class.forName("org.sqlite.JDBC");
         c = DriverManager.getConnection("jdbc:sqlite:beacon.db");
      } catch (Exception var2) {
         PrintStream var10000 = System.err;
         String var10001 = var2.getClass().getName();
         var10000.println(var10001 + ": " + var2.getMessage());
         System.exit(0);
      }

      return c;
   }

   public static HashMap<String, String> CheckBeaconHash(Connection c, String hash) {
      HashMap Beacon = new HashMap();

      try {
         String sql = "SELECT * FROM Beacon WHERE Hash = (?) ORDER BY UpdateNoteTime DESC;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, hash);
         ResultSet rs = ps.executeQuery();

         do {
            if (!rs.next()) {
               rs.close();
               ps.close();
               c.close();
               return null;
            }
         } while(!rs.getString("Hash").equals(hash));

         Beacon.put("StartTime", rs.getString("StartTime"));
         Beacon.put("Note", rs.getString("Note"));
         ps.close();
         c.close();
         return Beacon;
      } catch (SQLException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static HashMap<String, String> CheckBeacon(Connection c, String hash, String BeaconId) {
      HashMap Beacon = new HashMap();

      try {
         String sql = "SELECT * FROM Beacon WHERE Hash = (?) AND BeaconId = (?) ORDER BY UpdateNoteTime DESC;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, hash);
         ps.setString(2, BeaconId);
         ResultSet rs = ps.executeQuery();

         do {
            if (!rs.next()) {
               rs.close();
               ps.close();
               c.close();
               return null;
            }
         } while(!rs.getString("Hash").equals(hash));

         Beacon.put("StartTime", rs.getString("StartTime"));
         Beacon.put("Note", rs.getString("Note"));
         ps.close();
         c.close();
         return Beacon;
      } catch (SQLException var7) {
         throw new RuntimeException(var7);
      }
   }

   public static HashMap<String, String> CheckSShBeacon(Connection c, info BeaconInfo) {
      HashMap Beacon = new HashMap();

      try {
         String sql = "SELECT * FROM Beacon WHERE Computer = (?) and User = (?) and Arch = (?) and Process = (?) and External = (?) ORDER BY id DESC;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, BeaconInfo.Computer);
         ps.setString(2, BeaconInfo.User);
         ps.setString(3, BeaconInfo.Arch);
         ps.setString(4, BeaconInfo.Process);
         ps.setString(5, BeaconInfo.External);
         ResultSet rs = ps.executeQuery();

         do {
            if (!rs.next()) {
               rs.close();
               ps.close();
               c.close();
               return null;
            }
         } while(rs == null);

         Beacon.put("Hash", rs.getString("Hash"));
         Beacon.put("StartTime", rs.getString("StartTime"));
         Beacon.put("Note", rs.getString("Note"));
         Beacon.put("Id", rs.getString("Id"));
         Beacon.put("External", rs.getString("External"));
         Beacon.put("Internal", rs.getString("Internal"));
         Beacon.put("Process", rs.getString("Process"));
         Beacon.put("Arch", rs.getString("Arch"));
         Beacon.put("User", rs.getString("User"));
         Beacon.put("Computer", rs.getString("Computer"));
         ps.close();
         c.close();
         return Beacon;
      } catch (SQLException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static void AddBeacon(Connection c, info BeaconInfo) {
      SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
      String times = df.format(new Date());

      try {
         String sql = "INSERT INTO Beacon (Hash,StartTime,Note,BeaconId,External,Process,Arch,User,Computer,Internal,UpdateNoteTime) VALUES (?, ?, \"\",?,?,?,?,?,?,?,?);";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, BeaconInfo.Hash);
         ps.setString(2, times);
         ps.setString(3, BeaconInfo.BeaconId);
         ps.setString(4, BeaconInfo.External);
         ps.setString(5, BeaconInfo.Process);
         ps.setString(6, BeaconInfo.Arch);
         ps.setString(7, BeaconInfo.User);
         ps.setString(8, BeaconInfo.Computer);
         ps.setString(9, BeaconInfo.Internal);
         ps.setString(10, times);
         ps.execute();
         if (ps != null) {
            ps.close();
         }

         if (c != null) {
            c.close();
         }

      } catch (SQLException var6) {
         throw new RuntimeException(var6);
      }
   }

   public static void AddBeacon2(Connection c, info BeaconInfo) {
      try {
         String sql = "INSERT INTO Beacon (Hash,StartTime,Note,BeaconId,External,Process,Arch,User,Computer,Internal,UpdateNoteTime) VALUES (?, ?, ?,?,?,?,?,?,?,?,\"\");";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, BeaconInfo.Hash);
         ps.setString(2, BeaconInfo.StartTime);
         ps.setString(3, BeaconInfo.Note);
         ps.setString(4, BeaconInfo.BeaconId);
         ps.setString(5, BeaconInfo.External);
         ps.setString(6, BeaconInfo.Process);
         ps.setString(7, BeaconInfo.Arch);
         ps.setString(8, BeaconInfo.User);
         ps.setString(9, BeaconInfo.Computer);
         ps.setString(10, BeaconInfo.Internal);
         ps.execute();
         if (ps != null) {
            ps.close();
         }

         if (c != null) {
            c.close();
         }

      } catch (SQLException var4) {
         throw new RuntimeException(var4);
      }
   }

   public static void UpBeaconNote(Connection c, String BeaconId, String Note) {
      SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
      String times = df.format(new Date());

      try {
         String sql = "UPDATE Beacon set Note = ? , UpdateNoteTime = ? where BeaconId=?;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, Note);
         ps.setString(2, times);
         ps.setString(3, BeaconId);
         ps.executeUpdate();
         ps.close();
         c.close();
      } catch (SQLException var7) {
         throw new RuntimeException(var7);
      }
   }

   public static void UpBeaconLastTime(Connection c, String BeaconId, String Note) {
      SimpleDateFormat df = new SimpleDateFormat("MM-dd HH:mm:ss");
      String times = df.format(new Date());

      try {
         String sql = "UPDATE Beacon set LastTime = ? where BeaconId=?;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, times);
         ps.setString(2, BeaconId);
         ps.executeUpdate();
         ps.close();
         c.close();
      } catch (SQLException var7) {
         throw new RuntimeException(var7);
      }
   }

   public static void UpBeaconId(Connection c, String Hash, String BeaconId) {
      try {
         String sql = "UPDATE Beacon set BeaconId = ? where Hash=?;";
         PreparedStatement ps = c.prepareStatement(sql);
         ps.setString(1, BeaconId);
         ps.setString(2, Hash);
         ps.executeUpdate();
         ps.close();
         c.close();
      } catch (SQLException var5) {
         throw new RuntimeException(var5);
      }
   }
}
