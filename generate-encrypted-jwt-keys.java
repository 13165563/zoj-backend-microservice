import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 一键生成加密JWT密钥文件
 * 使用方法：java generate-encrypted-jwt-keys.java
 */
public class generate-encrypted-jwt-keys {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    
    public static void main(String[] args) {
        try {
            System.out.println("开始生成加密JWT密钥文件...");
            
            // 生成RSA密钥对
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();
            
            // 将密钥转换为Base64字符串
            String privateKeyStr = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            String publicKeyStr = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            
            // 加密密钥
            String password = "zoj123456";
            String encryptedPrivateKey = encrypt(privateKeyStr, password);
            String encryptedPublicKey = encrypt(publicKeyStr, password);
            
            // 确保目录存在
            Files.createDirectories(Paths.get("zoj-backend-common/src/main/resources/jwt"));
            
            // 保存加密的私钥
            String privateKeyPath = "zoj-backend-common/src/main/resources/jwt/encrypted_private.key";
            try (FileOutputStream fos = new FileOutputStream(privateKeyPath)) {
                fos.write(encryptedPrivateKey.getBytes());
            }
            
            // 保存加密的公钥
            String publicKeyPath = "zoj-backend-common/src/main/resources/jwt/encrypted_public.key";
            try (FileOutputStream fos = new FileOutputStream(publicKeyPath)) {
                fos.write(encryptedPublicKey.getBytes());
            }
            
            System.out.println("加密JWT密钥文件生成成功！");
            System.out.println("私钥文件: " + privateKeyPath);
            System.out.println("公钥文件: " + publicKeyPath);
            System.out.println("加密密码: " + password);
            System.out.println();
            System.out.println("密钥已加密保存，需要密码才能解密使用");
            
        } catch (Exception e) {
            System.err.println("生成加密密钥文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加密字符串
     */
    private static String encrypt(String plainText, String password) throws Exception {
        // 从密码生成AES密钥
        SecretKeySpec secretKey = new SecretKeySpec(password.getBytes(), ALGORITHM);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
