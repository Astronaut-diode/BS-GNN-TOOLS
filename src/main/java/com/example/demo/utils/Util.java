package com.example.demo.utils;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jdk.nashorn.internal.parser.JSONParser;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
    /**
     * 返回resources文件夹的路径。
     *
     * @return
     */
    public static String getResources() {
        // 获取当前类的类加载器
        ClassLoader classLoader = Util.class.getClassLoader();
        // 使用类加载器获取static文件夹的位置
        String osName = System.getProperty("os.name").toLowerCase();
        String staticFolderPath;
        if (osName.contains("windows")) {
            staticFolderPath = classLoader.getResource("static").getPath();
        } else {
            staticFolderPath = "/root/demo/target/classes/static";
        }
        return new File(staticFolderPath).getParent();
    }

    /**
     * 确保path的文件夹是否已经存在，否则将其建立完毕
     *
     * @param path
     * @return
     */
    public static void createNewDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println(path + "文件夹创建成功");
        }
    }

    public static String calSha(String filePath) {
        String hash = "";
        try {
            // 获取文件输入流
            FileInputStream fis = new FileInputStream(filePath);

            // 创建MessageDigest对象
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // 创建缓冲区
            byte[] buffer = new byte[8192];
            int bytesRead;

            // 读取文件内容并更新MessageDigest对象
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            // 关闭文件输入流
            fis.close();

            // 计算哈希值
            byte[] hashBytes = digest.digest();

            // 将哈希值转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            hash = sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }

        return hash;
    }

    public static void executeCommandAsync(final String command) {
        // 创建一个新线程执行命令
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                executeCommand(command);
            }
        });
        thread.start(); // 启动线程
    }

    private static void executeCommand(String command) {
        try {
            // 执行命令
            Process process = Runtime.getRuntime().exec(command);

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待命令执行完成
            int exitCode = process.waitFor();
            System.out.println("命令执行完毕，退出码：" + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给定路径对应的JSONObject结果
     * @param path
     * @return
     */
    public static JSONObject getJson(String path) {
        JSONParser parser = null;
        StringBuilder jsonStringBuilder = new StringBuilder();
        File file = new File(path);
        try {
            FileReader fileReader = new FileReader(file);
            int len = 0;
            char[] cbuffer = new char[1024];
            while((len = fileReader.read(cbuffer)) != -1) {
                jsonStringBuilder.append(new String(cbuffer, 0, len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(jsonStringBuilder.toString());
    }

    /**
     * 给定路径对应的JSONArray结果
     * @param path
     * @return
     */
    public static JSONArray getJsonArray(String path) {
        JSONParser parser = null;
        StringBuilder jsonStringBuilder = new StringBuilder();
        File file = new File(path);
        try {
            FileReader fileReader = new FileReader(file);
            int len = 0;
            char[] cbuffer = new char[1024];
            while((len = fileReader.read(cbuffer)) != -1) {
                jsonStringBuilder.append(new String(cbuffer, 0, len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONArray.parseArray(jsonStringBuilder.toString());
    }
}
