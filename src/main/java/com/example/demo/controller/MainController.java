package com.example.demo.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.bean.Record;
import com.example.demo.dao.MainMapper;
import com.example.demo.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class MainController {

    @Autowired
    private MainMapper mainMapper;

    /**
     * 根据请求地址{localhost:8080/mainSCC}访问项目的首页，不刷新界面数据，这是Get请求。
     *
     * @return
     */
    @GetMapping("mainSCC")
    public String main() {
        return "mainSCC";
    }

    /**
     * 根据请求地址{localhost:8080/mainSCC}访问项目的首页，并刷新界面数据，这是Post请求。
     *
     * @return
     */
    @GetMapping("mainSCCData")
    @ResponseBody
    public Map<String, Object> Refresh(@RequestParam("userId") String userId,
                                       @RequestParam("username") String username,
                                       @RequestParam("page") int page,
                                       @RequestParam("limit") int limit) {
        List<Record> res = mainMapper.findRecordByUserId(userId);

        System.out.println(userId + " " + username + " " + page + " " + limit);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "数据刷新成功");
        response.put("count", res.size());
        response.put("data", res);
        return response;
    }


    /**
     * 根据请求地址{localhost:8080/uploadSCC}上传文件。
     *
     * @return
     */
    @PostMapping("uploadSCC")
    @ResponseBody
    public Map<String, Object> uploadSCC(@RequestParam("file") MultipartFile sourceCode, @RequestParam("userId") String userId) {
//        Properties properties = new Properties();
        try {
//            properties.load(new FileInputStream(Util.getResources() + File.separator + "config"));  // 加载配置文件config，找出存储位置。
            String SCCDirPath = Util.getResources() + File.separator + "SCCDir" + File.separator + userId;  // .../SCCDir/userId
            Util.createNewDir(SCCDirPath);  // 先建立用户的专属文件夹。
            String savePath = SCCDirPath + File.separator + System.currentTimeMillis() + "-" + sourceCode.getOriginalFilename();  // 新文件等待保存的位置
            File dest = new File(savePath);
            sourceCode.transferTo(dest);  // 将新文件保存下来。
            Record record = new Record(dest);  // 准备创建对应的记录，并插入到数据库中。
            mainMapper.uploadSCC(record);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "数据刷新成功");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/removeSCC}删除任务记录。
     *
     * @return
     */
    @PostMapping("removeSCC")
    @ResponseBody
    public Map<String, Object> removeSCC(@RequestParam("recordIds[]") String[] recordIds) {
        for(int i = 0; i < recordIds.length; ++i) {
            Record record = mainMapper.findSCCByRecordId(recordIds[i]);
            File sourceCodeFile = new File(record.getFilePath());
            sourceCodeFile.delete();
            File logFile = new File(record.getFilePath().replaceAll(".sol", ".log"));
            logFile.delete();
            System.out.println("即将删除" + sourceCodeFile);
        }
        mainMapper.removeSCC(recordIds);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("success", true);
        response.put("msg", "删除成功！");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/detectSCC}启动目标的检测任务。
     *
     * @return
     */
    @PostMapping("detectSCC")
    @ResponseBody
    public Map<String, Object> detectSCC(@RequestParam("recordId") String recordId) {
        mainMapper.detectSCC(recordId, 0);
        Record record = mainMapper.findSCCByRecordId(recordId);
        // 启动七种检测任务。
        String[] attackTypes = new String[]{
                "access_control", "arithmetic", "denial_of_service", "front_running", "reentrancy",
                "time_manipulation", "unchecked_low_level_calls"
        };
        String[] dataDirTypes = new String[]{
                "access_control_contract_data_augmentation", "arithmetic_contract_data_augmentation", "dos_contract_data_augmentation", "front_running_contract_data_augmentation",
                "reentrancy_contract_data_augmentation", "time_manipulation_contract_data_augmentation", "uncheck_contract_data_augmentation"
        };
        String cmd = "";
        for (int i = 0; i < 7; ++i) {
            cmd = "/root/anaconda3/envs/lunikhod/bin/python3 /root/MVD-HG/Test.py --source_dir " + new File(record.getFilePath()).getParent() + " --dest_dir /root/MVD-HG/" + dataDirTypes[i] + "/wait_predict --filename " + new File(record.getFilePath()).getName() + " --attack_type " + attackTypes[i];
            Util.executeCommandAsync(cmd);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("success", true);
        response.put("msg", "启动成功！");
        return response;
    }

    /**
     * 根据请求地址{localhost:8080/refreshSCC}刷新任务记录。
     *
     * @return
     */
    @PostMapping("refreshSCC")
    @ResponseBody
    public Map<String, Object> refreshSCC(@RequestParam("recordId") String recordId) {
        Record record = mainMapper.findSCCByRecordId(recordId);
        File log = new File(record.getFilePath().replaceAll(".sol", ".log"));  // 先找出日志文件
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
            record.setLog(record.getFilePath().replaceAll(".sol", ".log"));
            mainMapper.overSCC(record);
        } else {
            mainMapper.detectSCC(recordId, jsonObject.keySet().size());
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
    @RequestMapping("/downloadSCC")
    public void downloadSCC(@RequestParam("recordId") String recordId, HttpServletResponse response) {
        Record record = mainMapper.findSCCByRecordId(recordId);
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
    @GetMapping("/showGraphSCC")
    public ModelAndView showGraphSCC(@RequestParam("recordId") String recordId,
                                     @RequestParam("start") Integer start,
                                     @RequestParam("end") Integer end) {
        ModelAndView modelAndView = new ModelAndView();
        Record sclRecord = mainMapper.findSCCByRecordId(recordId);
        String filePath = sclRecord.getFilePath();  // 原始文件的保存地址。


        // 取出所有的节点名字
        JSONObject nodeJson = Util.getJson(filePath.replace(".sol", "_node.json"));
        ArrayList<String> nodes = new ArrayList<>();
        for (int i = 0; i < nodeJson.getJSONArray("node_feature_list").size(); i++) {
            nodes.add(nodeJson.getJSONArray("node_feature_list").getJSONObject(i).getString("nodeType"));
        }

        List<Map<String, Object>> data = new ArrayList<>();

        JSONArray astJson = Util.getJsonArray(filePath.replace(".sol", "_ast_edge.json"));
        for (int i = 0; i < astJson.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            int astSource = (int) astJson.getJSONObject(i).get("source_node_node_id"), astTarget = (int) astJson.getJSONObject(i).get("target_node_node_id");
            if (!(start == -1 && end == -1) && (
                    (astSource - 1 < start || astSource - 1 > end) || (astTarget - 1 < start || astTarget - 1 > end)
            )) {
                continue;
            }
            map.put("source", astSource + "." + nodes.get(astSource - 1));
            map.put("target", astTarget + "." + nodes.get(astTarget - 1));
            map.put("type", "AST");
            map.put("color", "red");
            data.add(map);
        }

        JSONArray cfgJson = Util.getJsonArray(filePath.replace(".sol", "_cfg_edge.json"));
        for (int i = 0; i < cfgJson.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            int cfgSource = (int) cfgJson.getJSONObject(i).get("source_node_node_id"), cfgTarget = (int) cfgJson.getJSONObject(i).get("target_node_node_id");
            if (!(start == -1 && end == -1) && (
                    (cfgSource - 1 < start || cfgSource - 1 > end) || (cfgTarget - 1 < start || cfgTarget - 1 > end)
            )) {
                continue;
            }
            map.put("source", cfgSource + "." + nodes.get(cfgSource - 1));
            map.put("target", cfgTarget + "." + nodes.get(cfgTarget - 1));
            map.put("type", "CFG");
            map.put("color", "green");
            data.add(map);
        }

        JSONArray dfgJson = Util.getJsonArray(filePath.replace(".sol", "_dfg_edge.json"));
        for (int i = 0; i < dfgJson.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            int dfgSource = (int) dfgJson.getJSONObject(i).get("source_node_node_id"), dfgTarget = (int) dfgJson.getJSONObject(i).get("target_node_node_id");
            if (!(start == -1 && end == -1) && (
                    (dfgSource - 1 < start || dfgSource - 1 > end) || (dfgTarget - 1 < start || dfgTarget - 1 > end)
            )) {
                continue;
            }
            map.put("source", dfgSource + "." + nodes.get(dfgSource - 1));
            map.put("target", dfgTarget + "." + nodes.get(dfgTarget - 1));
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
        modelAndView.setViewName("graphSCC");
        return modelAndView;
    }
}