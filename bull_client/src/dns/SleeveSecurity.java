package dns;

import common.CommonUtils;
import common.MudgeSanity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SleeveSecurity {
  private IvParameterSpec B;
  
  private Cipher A;
  
  private Cipher C;
  
  private Mac F;
  
  private SecretKeySpec E;
  
  private SecretKeySpec D;
  
  public void registerKey(byte[] paramArrayOfbyte) {
    synchronized (this) {
      try {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] arrayOfByte1 = messageDigest.digest(paramArrayOfbyte);
        byte[] arrayOfByte2 = Arrays.copyOfRange(arrayOfByte1, 0, 16);
        byte[] arrayOfByte3 = Arrays.copyOfRange(arrayOfByte1, 16, 32);
        this.E = new SecretKeySpec(arrayOfByte2, "AES");
        this.D = new SecretKeySpec(arrayOfByte3, "HmacSHA256");
      } catch (Exception exception) {
        exception.printStackTrace();
      } 
    } 
  }
  
  public SleeveSecurity() {
    try {
      byte[] arrayOfByte = "EO74nTCmHDCjYzVE".getBytes();
      this.B = new IvParameterSpec(arrayOfByte);
      this.A = Cipher.getInstance("AES/CBC/NoPadding");
      this.C = Cipher.getInstance("AES/CBC/NoPadding");
      this.F = Mac.getInstance("HmacSHA256");
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
  
  protected byte[] do_encrypt(SecretKey paramSecretKey, byte[] paramArrayOfbyte) throws Exception {
    this.A.init(1, paramSecretKey, this.B);
    return this.A.doFinal(paramArrayOfbyte);
  }
  
  protected byte[] do_decrypt(SecretKey paramSecretKey, byte[] paramArrayOfbyte) throws Exception {
    this.C.init(2, paramSecretKey, this.B);
    return this.C.doFinal(paramArrayOfbyte, 0, paramArrayOfbyte.length);
  }
  
  protected void pad(ByteArrayOutputStream paramByteArrayOutputStream) {
    for (int i = paramByteArrayOutputStream.size() % 16; i < 16; i++)
      paramByteArrayOutputStream.write(65); 
  }
  
  public byte[] encrypt(byte[] paramArrayOfbyte) {
    try {
      ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream(paramArrayOfbyte.length + 1024);
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream1);
      byteArrayOutputStream1.reset();
      dataOutputStream.writeInt(CommonUtils.rand(2147483647));
      dataOutputStream.writeInt(paramArrayOfbyte.length);
      dataOutputStream.write(paramArrayOfbyte, 0, paramArrayOfbyte.length);
      pad(byteArrayOutputStream1);
      byte[] arrayOfByte1 = null;
      synchronized (this) {
        arrayOfByte1 = do_encrypt(this.E, byteArrayOutputStream1.toByteArray());
      } 
      byte[] arrayOfByte2 = null;
      synchronized (this) {
        this.F.init(this.D);
        arrayOfByte2 = this.F.doFinal(arrayOfByte1);
      } 
      ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
      byteArrayOutputStream2.write(arrayOfByte1);
      byteArrayOutputStream2.write(arrayOfByte2, 0, 16);
      return byteArrayOutputStream2.toByteArray();
    } catch (InvalidKeyException invalidKeyException) {
      MudgeSanity.logException("[Sleeve] encrypt failure", invalidKeyException, false);
      CommonUtils.print_error_file("resources/crypto.txt");
      MudgeSanity.debugJava();
      if (this.E != null)
        CommonUtils.print_info("[Sleeve] Key's algorithm is: '" + this.E.getAlgorithm() + "' ivspec is: " + this.B); 
    } catch (Exception exception) {
      MudgeSanity.logException("[Sleeve] encrypt failure", exception, false);
    } 
    return new byte[0];
  }
  
  public byte[] decrypt(byte[] paramArrayOfbyte) {
    try {
      byte[] arrayOfByte1 = Arrays.copyOfRange(paramArrayOfbyte, 0, paramArrayOfbyte.length - 16);
      byte[] arrayOfByte2 = Arrays.copyOfRange(paramArrayOfbyte, paramArrayOfbyte.length - 16, paramArrayOfbyte.length);
      byte[] arrayOfByte3 = null;
      synchronized (this) {
        this.F.init(this.D);
        arrayOfByte3 = this.F.doFinal(arrayOfByte1);
      } 
      byte[] arrayOfByte4 = Arrays.copyOfRange(arrayOfByte3, 0, 16);
      if (!MessageDigest.isEqual(arrayOfByte2, arrayOfByte4)) {
        CommonUtils.print_error("[Sleeve] Bad HMAC on " + paramArrayOfbyte.length + " byte message from resource");
        return new byte[0];
      } 
      byte[] arrayOfByte5 = null;
      synchronized (this) {
        arrayOfByte5 = do_decrypt(this.E, arrayOfByte1);
      } 
      DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrayOfByte5));
      int i = dataInputStream.readInt();
      int j = dataInputStream.readInt();
      if (j < 0 || j > paramArrayOfbyte.length) {
        CommonUtils.print_error("[Sleeve] Impossible message length: " + j);
        return new byte[0];
      } 
      byte[] arrayOfByte6 = new byte[j];
      dataInputStream.readFully(arrayOfByte6, 0, j);
      return arrayOfByte6;
    } catch (Exception exception) {
      exception.printStackTrace();
      return new byte[0];
    } 
  }
}


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\dns\SleeveSecurity.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */