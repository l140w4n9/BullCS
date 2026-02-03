/*     */ package tap;
/*     */ import java.io.ByteArrayInputStream;
/*     */ import java.io.ByteArrayOutputStream;
/*     */ import java.io.DataInputStream;
/*     */ import java.io.DataOutputStream;
/*     */ import javax.crypto.Cipher;
/*     */ import javax.crypto.SecretKey;
/*     */ import javax.crypto.spec.IvParameterSpec;
/*     */ import javax.crypto.spec.SecretKeySpec;
/*     */ 
/*     */ public class EncryptedTap extends TapProtocol {
/*     */   protected SecretKey key;
/*     */   protected IvParameterSpec ivspec;
/*  14 */   protected byte[] out_buffer = new byte[65536]; protected Cipher in; protected Cipher out;
/*  15 */   protected byte[] in_buffer = new byte[65536];
/*     */   
/*     */   protected ByteArrayOutputStream out_bytes;
/*     */   protected DataOutputStream out_handle;
/*     */   
/*     */   public EncryptedTap(String paramString, byte[] paramArrayOfbyte) {
/*  21 */     super(paramString);
/*     */ 
/*     */     
/*  24 */     byte[] arrayOfByte = new byte[16];
/*  25 */     for (byte b = 0; b < arrayOfByte.length; b++) {
/*  26 */       arrayOfByte[b] = paramArrayOfbyte[b % paramArrayOfbyte.length];
/*     */     }
/*     */     
/*     */     try {
/*  30 */       this.key = new SecretKeySpec(arrayOfByte, "AES");
/*     */ 
/*     */       
/*  33 */       byte[] arrayOfByte1 = "EO74nTCmHDCjYzVE".getBytes();
/*  34 */       this.ivspec = new IvParameterSpec(arrayOfByte1);
/*     */ 
/*     */       
/*  37 */       this.in = Cipher.getInstance("AES/CBC/NoPadding");
/*  38 */       this.out = Cipher.getInstance("AES/CBC/NoPadding");
/*     */ 
/*     */       
/*  41 */       this.out_bytes = new ByteArrayOutputStream(65536);
/*  42 */       this.out_handle = new DataOutputStream(this.out_bytes);
/*     */     }
/*  44 */     catch (Exception exception) {
/*  45 */       throw new RuntimeException(exception);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void pad(ByteArrayOutputStream paramByteArrayOutputStream) {
/*  51 */     int i = paramByteArrayOutputStream.size() % 16;
/*  52 */     while (i < 16) {
/*  53 */       paramByteArrayOutputStream.write(65);
/*  54 */       i++;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public byte[] protocol(int paramInt, byte[] paramArrayOfbyte) {
/*  60 */     byte[] arrayOfByte = super.protocol(paramInt, paramArrayOfbyte);
/*     */ 
/*     */     
/*     */     try {
/*  64 */       this.out_bytes.reset();
/*  65 */       this.out_handle.write(arrayOfByte, 0, arrayOfByte.length);
/*  66 */       pad(this.out_bytes);
/*     */ 
/*     */       
/*  69 */       this.in.init(1, this.key, this.ivspec);
/*  70 */       return this.in.doFinal(this.out_bytes.toByteArray());
/*     */ 
/*     */ 
/*     */     
/*     */     }
/*  75 */     catch (Exception exception) {
/*  76 */       exception.printStackTrace();
/*     */       
/*  78 */       return new byte[0];
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public int readFrame(byte[] paramArrayOfbyte) {
/*  84 */     int i = super.readFrame(this.out_buffer);
/*     */ 
/*     */     
/*     */     try {
/*  88 */       this.out_bytes.reset();
/*  89 */       this.out_handle.writeShort(i);
/*  90 */       this.out_handle.write(this.out_buffer, 0, i);
/*  91 */       pad(this.out_bytes);
/*     */ 
/*     */       
/*  94 */       this.in.init(1, this.key, this.ivspec);
/*  95 */       byte[] arrayOfByte = this.in.doFinal(this.out_bytes.toByteArray());
/*     */ 
/*     */       
/*  98 */       System.arraycopy(arrayOfByte, 0, paramArrayOfbyte, 0, arrayOfByte.length);
/*  99 */       return arrayOfByte.length;
/*     */     }
/* 101 */     catch (Exception exception) {
/* 102 */       exception.printStackTrace();
/*     */       
/* 104 */       return 0;
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void writeFrame(byte[] paramArrayOfbyte, int paramInt) {
/*     */     try {
/* 111 */       this.out.init(2, this.key, this.ivspec);
/* 112 */       byte[] arrayOfByte = this.out.doFinal(paramArrayOfbyte, 0, paramInt);
/*     */ 
/*     */       
/* 115 */       DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(arrayOfByte));
/* 116 */       int i = dataInputStream.readUnsignedShort();
/* 117 */       dataInputStream.readFully(this.in_buffer, 0, i);
/*     */ 
/*     */       
/* 120 */       writeFrame(this.fd, this.in_buffer, i);
/*     */     }
/* 122 */     catch (Exception exception) {
/* 123 */       exception.printStackTrace();
/*     */     } 
/*     */   }
/*     */ }


/* Location:              C:\Users\liaowang\Desktop\CobaltStrikeModfiy\bullCs\bull_client\cs_bin\cat_client.jar!\tap\EncryptedTap.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */