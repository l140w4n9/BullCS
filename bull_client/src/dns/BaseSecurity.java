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
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public abstract class BaseSecurity {
  public static final short CRYPTO_LICENSED_PRODUCT = 0;
  
  public static final short CRYPTO_TRIAL_PRODUCT = 1;
  
  protected IvParameterSpec ivspec;
  
  protected Cipher in;
  
  protected Cipher out;
  
  protected Mac mac;
  
  private static Map A = new HashMap<>();
  
  protected SecretKey getKey(String paramString) {
    _A _A = getSession(paramString);
    return (_A != null) ? _A.C : null;
  }
  
  protected SecretKey getHashKey(String paramString) {
    _A _A = getSession(paramString);
    return (_A != null) ? _A.B : null;
  }
  
  public boolean isReady(String paramString) {
    return (getSession(paramString) != null);
  }
  
  protected _A getSession(String paramString) {
    synchronized (this) {
      return (_A)A.get(paramString);
    } 
  }
  
  public void registerKey(String paramString, byte[] paramArrayOfbyte) {
    synchronized (this) {
      if (A.containsKey(paramString))
        return; 
    } 
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] arrayOfByte1 = messageDigest.digest(paramArrayOfbyte);
      byte[] arrayOfByte2 = Arrays.copyOfRange(arrayOfByte1, 0, 16);
      byte[] arrayOfByte3 = Arrays.copyOfRange(arrayOfByte1, 16, 32);
      _A _A = new _A();
      _A.C = new SecretKeySpec(arrayOfByte2, "AES");
      _A.B = new SecretKeySpec(arrayOfByte3, "HmacSHA256");
      synchronized (this) {
        A.put(paramString, _A);
      } 
    } catch (Exception exception) {
      exception.printStackTrace();
    } 
  }
  
  public BaseSecurity() {
    try {
      byte[] arrayOfByte = "EO74nTCmHDCjYzVE".getBytes();
      this.ivspec = new IvParameterSpec(arrayOfByte);
      this.in = Cipher.getInstance("AES/CBC/NoPadding");
      this.out = Cipher.getInstance("AES/CBC/NoPadding");
      this.mac = Mac.getInstance("HmacSHA256");
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    } 
  }
  
  protected void pad(ByteArrayOutputStream paramByteArrayOutputStream) {
    for (int i = paramByteArrayOutputStream.size() % 16; i < 16; i++)
      paramByteArrayOutputStream.write(65); 
  }
  
  public void debugFrame(String paramString, byte[] paramArrayOfbyte) {
    try {
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append("== " + paramString + " ==\n");
      DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(paramArrayOfbyte));
      int i = dataInputStream.readInt();
      stringBuffer.append("\tReplay Counter: " + i + "\n");
      int j = dataInputStream.readInt();
      stringBuffer.append("\tMessage Length: " + j + "\n");
      byte[] arrayOfByte = new byte[j];
      dataInputStream.readFully(arrayOfByte, 0, j);
      stringBuffer.append("\tPlain Text:     " + CommonUtils.toHexString(arrayOfByte) + "\n");
      CommonUtils.print_good(stringBuffer.toString());
    } catch (Exception exception) {
      MudgeSanity.logException("foo", exception, false);
    } 
  }
  
  public byte[] encrypt(String paramString, byte[] paramArrayOfbyte) {
    try {
      if (!isReady(paramString)) {
        CommonUtils.print_error("encrypt: No session for '" + paramString + "'");
        return new byte[0];
      } 
      ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream(paramArrayOfbyte.length + 1024);
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream1);
      SecretKey secretKey1 = getKey(paramString);
      SecretKey secretKey2 = getHashKey(paramString);
      byteArrayOutputStream1.reset();
      dataOutputStream.writeInt((int)(System.currentTimeMillis() / 1000L));
      dataOutputStream.writeInt(paramArrayOfbyte.length);
      dataOutputStream.write(paramArrayOfbyte, 0, paramArrayOfbyte.length);
      pad(byteArrayOutputStream1);
      byte[] arrayOfByte1 = null;
      synchronized (this.in) {
        arrayOfByte1 = do_encrypt(secretKey1, byteArrayOutputStream1.toByteArray());
      } 
      byte[] arrayOfByte2 = null;
      synchronized (this.mac) {
        this.mac.init(secretKey2);
        arrayOfByte2 = this.mac.doFinal(arrayOfByte1);
      } 
      ByteArrayOutputStream byteArrayOutputStream2 = new ByteArrayOutputStream();
      byteArrayOutputStream2.write(arrayOfByte1);
      byteArrayOutputStream2.write(arrayOfByte2, 0, 16);
      return byteArrayOutputStream2.toByteArray();
    } catch (InvalidKeyException invalidKeyException) {
      MudgeSanity.logException("encrypt failure for: " + paramString, invalidKeyException, false);
      CommonUtils.print_error_file("resources/crypto.txt");
      MudgeSanity.debugJava();
      SecretKey secretKey = getKey(paramString);
      if (secretKey != null)
        CommonUtils.print_info("Key's algorithm is: '" + secretKey.getAlgorithm() + "' ivspec is: " + this.ivspec); 
    } catch (Exception exception) {
      MudgeSanity.logException("encrypt failure for: " + paramString, exception, false);
    } 
    return new byte[0];
  }
  
  public byte[] decrypt(String paramString, byte[] paramArrayOfbyte) {
    try {
      if (!isReady(paramString)) {
        CommonUtils.print_error("decrypt: No session for '" + paramString + "'");
        return new byte[0];
      } 
      _A _A = getSession(paramString);
      SecretKey secretKey1 = getKey(paramString);
      SecretKey secretKey2 = getHashKey(paramString);
      byte[] arrayOfByte1 = Arrays.copyOfRange(paramArrayOfbyte, 0, paramArrayOfbyte.length - 16);
      byte[] arrayOfByte2 = Arrays.copyOfRange(paramArrayOfbyte, paramArrayOfbyte.length - 16, paramArrayOfbyte.length);
      byte[] arrayOfByte3 = null;
      synchronized (this.mac) {
        this.mac.init(secretKey2);
        arrayOfByte3 = this.mac.doFinal(arrayOfByte1);
      } 
      byte[] arrayOfByte4 = Arrays.copyOfRange(arrayOfByte3, 0, 16);
      if (!MessageDigest.isEqual(arrayOfByte2, arrayOfByte4)) {
        CommonUtils.print_error("[Session Security] Bad HMAC on " + paramArrayOfbyte.length + " byte message from Beacon " + paramString);
        return new byte[0];
      } 
      byte[] arrayOfByte5 = null;
      synchronized (this.out) {
        arrayOfByte5 = do_decrypt(secretKey1, arrayOfByte1);
      } 
      DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrayOfByte5));
      int i = dataInputStream.readInt();
      if (i <= _A.A) {
        CommonUtils.print_error("[Session Security] Bad counter (replay attack?, duplicate message?) " + i + " <= " + _A.A + " message from Beacon " + paramString);
        return new byte[0];
      } 
      int j = dataInputStream.readInt();
      if (j < 0 || j > paramArrayOfbyte.length) {
        CommonUtils.print_error("[Session Security] Impossible message length: " + j + " from Beacon " + paramString);
        return new byte[0];
      } 
      byte[] arrayOfByte6 = new byte[j];
      dataInputStream.readFully(arrayOfByte6, 0, j);
      _A.A = i;
      return arrayOfByte6;
    } catch (Exception exception) {
      exception.printStackTrace();
      return new byte[0];
    } 
  }
  
  public static void main(String[] paramArrayOfString) {
    QuickSecurity quickSecurity = new QuickSecurity();
    quickSecurity.registerKey("1234", CommonUtils.randomData(16));
    String str = "This is a test string, I want to see what happens.";
    byte[] arrayOfByte1 = CommonUtils.toBytes(str);
    byte[] arrayOfByte2 = quickSecurity.encrypt("1234", arrayOfByte1);
    byte[] arrayOfByte3 = quickSecurity.decrypt("1234", arrayOfByte2);
    CommonUtils.print_info("Cipher [H]:  " + CommonUtils.toHexString(arrayOfByte2));
    CommonUtils.print_info("Plain  [H]:  " + CommonUtils.toHexString(arrayOfByte3));
    CommonUtils.print_info("Cipher:      " + CommonUtils.bString(arrayOfByte2).replaceAll("\\P{Print}", "."));
    CommonUtils.print_info("Plain:       " + CommonUtils.bString(arrayOfByte3));
    CommonUtils.print_info("[Cipher]:    " + arrayOfByte2.length);
    CommonUtils.print_info("[Plain]:     " + arrayOfByte3.length);
    System.out.println("SCHEME" + QuickSecurity.getCryptoScheme());
  }
  
  protected abstract byte[] do_encrypt(SecretKey paramSecretKey, byte[] paramArrayOfbyte) throws Exception;
  
  protected abstract byte[] do_decrypt(SecretKey paramSecretKey, byte[] paramArrayOfbyte) throws Exception;
  
  private static class _A {
    public SecretKey C = null;
    
    public SecretKey B = null;
    
    public long A = 0L;
    
    private _A() {}
  }
}


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\dns\BaseSecurity.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */