package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.BCCRecord;
import com.example.demo.bean.Record;
import com.example.demo.dao.BCCMapper;
import com.example.demo.dao.MainMapper;
import com.example.demo.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Controller
public class BCCController {

    @Autowired
    private BCCMapper bccMapper;

    /**
     * 根据请求地址{localhost:8080/mainBCC}访问项目的首页，不刷新界面数据，这是Get请求。
     *
     * @return
     */
    @GetMapping("mainBCC")
    public String main() {
        return "mainBCC";
    }

    /**
     * 根据请求地址{localhost:8080/mainSCC}访问项目的首页，并刷新界面数据，这是Post请求。
     *
     * @return
     */
    @GetMapping("mainBCCData")
    @ResponseBody
    public Map<String, Object> Refresh(@RequestParam("userId") String userId,
                                       @RequestParam("username") String username,
                                       @RequestParam("page") int page,
                                       @RequestParam("limit") int limit) {
        List<BCCRecord> res = bccMapper.findRecordByUserId(userId);

        System.out.println(userId + " " + username + " " + page + " " + limit);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "数据刷新成功");
        response.put("count", res.size());
        response.put("data", res);
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/uploadBCC}上传文件。
     *
     * @return
     */
    @PostMapping("uploadBCC")
    @ResponseBody
    public Map<String, Object> uploadBCC(@RequestParam("file") MultipartFile sourceCode, @RequestParam("userId") String userId) {
//        Properties properties = new Properties();
        try {
//            properties.load(new FileInputStream(Util.getResources() + File.separator + "config"));  // 加载配置文件config，找出存储位置。
            String BCCDirPath = Util.getResources() + File.separator + "BCCDir" + File.separator + userId;  // .../BCCDir/userId
            Util.createNewDir(BCCDirPath);  // 先建立用户的专属文件夹。
            String savePath = BCCDirPath + File.separator + System.currentTimeMillis() + "-" + sourceCode.getOriginalFilename();  // 新文件等待保存的位置
            File dest = new File(savePath);
            sourceCode.transferTo(dest);  // 将新文件保存下来。
            BCCRecord record = new BCCRecord(dest);  // 准备创建对应的记录，并插入到数据库中。
            bccMapper.uploadBCC(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "数据刷新成功");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/removeBCC}删除任务记录。
     *
     * @return
     */
    @PostMapping("removeBCC")
    @ResponseBody
    public Map<String, Object> removeBCC(@RequestParam("recordIds[]") String[] recordIds) {
        for(int i = 0; i < recordIds.length; ++i) {
            BCCRecord record = bccMapper.findBCCByRecordId(recordIds[i]);
            File sourceCodeFile = new File(record.getFilePath());
            sourceCodeFile.delete();
            File logFile = new File(record.getFilePath().replaceAll(".bin", ".log"));
            logFile.delete();
            System.out.println("即将删除" + sourceCodeFile);
        }
        bccMapper.removeBCC(recordIds);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("success", true);
        response.put("msg", "删除成功！");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/detectBCC}启动目标的检测任务。
     *
     * @return
     */
    @PostMapping("detectBCC")
    @ResponseBody
    public Map<String, Object> detectBCC(@RequestParam("recordId") String recordId) {
        bccMapper.detectBCC(recordId, 0);
        BCCRecord record = bccMapper.findBCCByRecordId(recordId);
        // 启动检测任务。
        String cmd = "";
        cmd = "/root/anaconda3/envs/lunikhod/bin/python3 /root/BC-GNN/Test3.py --source_dir " + new File(record.getFilePath()).getParent() + " --dest_dir /root/BC-GNN/predict/resources --filename " + new File(record.getFilePath()).getName();
        Util.executeCommandAsync(cmd);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("success", true);
        response.put("msg", "启动成功！");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/refreshBCC}刷新任务记录。
     *
     * @return
     */
    @PostMapping("refreshBCC")
    @ResponseBody
    public Map<String, Object> refreshBCC(@RequestParam("recordId") String recordId) {
        BCCRecord record = bccMapper.findBCCByRecordId(recordId);
        File log = new File(record.getFilePath().replaceAll(".bin", ".log"));  // 先找出日志文件
        if(!log.exists()) {  // 结果文件还没出现，但是不影响，这也算刷新成功
            Map<String, Object> response = new HashMap<>();
            response.put("code", 0);
            response.put("success", true);
            response.put("msg", "刷新成功！");
            return response;
        }
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(log);
            int len;
            byte[] buffer = new byte[1024];
            while((len = fileInputStream.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = JSONObject.parseObject(sb.toString());
        if (jsonObject.keySet().size() == 7) {  // 日志已经存在了，并且其中的种类达到了7种，代表检测完毕。
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while(iterator.hasNext()) {
                String key = iterator.next();
                String value = jsonObject.getString(key);
                if(value.equals("1")) {
                    record.setVulnerability(record.getVulnerability() + key + " ");
                }
            }
            if(record.getVulnerability().length() == 0) {
                record.setVulnerability("No");
            }
            record.setState("Over");
            record.setLog(record.getFilePath().replaceAll(".bin", ".log"));
            bccMapper.overBCC(record);
        } else {
            bccMapper.detectBCC(recordId, jsonObject.keySet().size());
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("success", true);
        response.put("msg", "刷新成功！");
        return response;
    }

    /**
     * @param recordId     想要下载的文件的对应记录号码
     * @param response
     * @功能描述 下载文件:
     */
    @RequestMapping("/downloadBCC")
    public void downloadBCC(@RequestParam("recordId") String recordId, HttpServletResponse response) {
        BCCRecord record = bccMapper.findBCCByRecordId(recordId);
        try {
            // path是指想要下载的文件的路径
            File file = new File(record.getLog());
            if(!file.exists()) {  // 结果文件还没出现，但是不影响，这也算刷新成功
                return;
            }
            // 获取文件名
            String filename = file.getName();
            // 获取文件后缀名
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载   inline表示在线打开   "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();
            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 显示目标文件的异构图，并且按照检测结果进行标红。
     *
     * @return
     */
    @GetMapping("/showGraphBCC")
    public ModelAndView showGraphBCC(@RequestParam("recordId") String recordId,
                                     @RequestParam("start") Integer start,
                                     @RequestParam("end") Integer end) {
        ModelAndView modelAndView = new ModelAndView();

        BCCRecord sclRecord = bccMapper.findBCCByRecordId(recordId);
        String filePath = sclRecord.getFilePath();  // 原始文件的保存地址。


        // 取出所有的节点名字
        JSONObject jsonContent = Util.getJson(filePath.replace(".bin", ".json"));
        ArrayList<String> nodes = new ArrayList<>();
        for (int i = 0; i < jsonContent.getJSONArray("opcodes").size(); i++) {
            nodes.add(jsonContent.getJSONArray("opcodes").getString(i));
        }

        List<Map<String, Object>> data = new ArrayList<>();

        for (int i = 0; i < jsonContent.getJSONArray("CFG").size(); i++) {
            Map<String, Object> map = new HashMap<>();
            int cfgSource = jsonContent.getJSONArray("CFG").getJSONArray(i).getInteger(0), cfgTarget = jsonContent.getJSONArray("CFG").getJSONArray(i).getInteger(1);
            if (!(start == -1 && end == -1) && (
                    (cfgSource < start || cfgSource > end) || (cfgTarget < start || cfgTarget > end)
            )) {
                continue;
            }
            map.put("source", cfgSource + "." + nodes.get(cfgSource));
            map.put("target", cfgTarget + "." + nodes.get(cfgTarget));
            map.put("type", "CFG");
            map.put("color", "green");
            data.add(map);
        }

        for (int i = 0; i < jsonContent.getJSONArray("DFG").size(); i++) {
            Map<String, Object> map = new HashMap<>();
            int cfgSource = jsonContent.getJSONArray("DFG").getJSONArray(i).getInteger(0), cfgTarget = jsonContent.getJSONArray("DFG").getJSONArray(i).getInteger(1);
            if (!(start == -1 && end == -1) && (
                    (cfgSource < start || cfgSource > end) || (cfgTarget < start || cfgTarget > end)
            )) {
                continue;
            }
            map.put("source", cfgSource + "." + nodes.get(cfgSource));
            map.put("target", cfgTarget + "." + nodes.get(cfgTarget));
            map.put("type", "DFG");
            map.put("color", "blue");
            data.add(map);
        }

        modelAndView.addObject("title", new File(sclRecord.getFilePath()).getName());
        modelAndView.addObject("recordId", recordId);
        modelAndView.addObject("data", data);
        modelAndView.addObject("total", nodes.size());
        if(start == -1)
            start = 0;
        modelAndView.addObject("start", start);
        if(end == -1)
            end = nodes.size();
        modelAndView.addObject("end", end);
        modelAndView.setViewName("graphBCC");
        return modelAndView;
    }

}