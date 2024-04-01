package com.example.demo.bean;

import com.example.demo.utils.Util;
import org.apache.ibatis.type.Alias;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/*
CREATE TABLE SCLRecord (
    recordId VARCHAR(255) PRIMARY KEY,
    filePath VARCHAR(255),
    uploadTime VARCHAR(255),
    totalBytes VARCHAR(255),
    vulnerability VARCHAR(255),
    state VARCHAR(255),
    log VARCHAR(255),
    hash VARCHAR(255),
    userId VARCHAR(255)
);
*/

@Alias("SCLRecord")
public class SCLRecord {
    // 这条记录的id
    private String recordId;

    //文件名字
    private String filePath;

    // 上传时间
    private String uploadTime;

    // 文件字节数
    private String totalBytes;

    // 文件是否包含漏洞
    private String vulnerability;

    // 文件的检测状态
    private String state;

    // 文件对应的检测日志的存储路径
    private String log;

    // 文件的哈希值
    private String hash;

    // 文件的主人的id
    private String userId;

    public SCLRecord() {

    }

    // 根据文件的保存路径，自己解析出所需的内容。
    public SCLRecord(File file) {
        this.filePath = file.getPath();
        Date date = new Date(Long.valueOf(file.getName().split("-")[0]));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        this.uploadTime = sdf.format(date);
        this.totalBytes = String.valueOf(file.length());
        this.vulnerability = "";
        this.state = "Pending";
        this.hash = Util.calSha(file.getPath());
        this.recordId = file.getName().split("-")[0] + hash.substring(0, 7);
        this.log = "Pending";
        this.userId = file.getParentFile().getName();
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(String totalBytes) {
        this.totalBytes = totalBytes;
    }

    public String getVulnerability() {
        return vulnerability;
    }

    public void setVulnerability(String vulnerability) {
        this.vulnerability = vulnerability;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SCLRecord record = (SCLRecord) o;
        return Objects.equals(recordId, record.recordId) &&
                Objects.equals(filePath, record.filePath) &&
                Objects.equals(uploadTime, record.uploadTime) &&
                Objects.equals(totalBytes, record.totalBytes) &&
                Objects.equals(vulnerability, record.vulnerability) &&
                Objects.equals(state, record.state) &&
                Objects.equals(log, record.log) &&
                Objects.equals(hash, record.hash) &&
                Objects.equals(userId, record.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId, filePath, uploadTime, totalBytes, vulnerability, state, log, hash, userId);
    }

    @Override
    public String toString() {
        return "SCLRecord{" +
                "recordId='" + recordId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", uploadTime='" + uploadTime + '\'' +
                ", totalBytes='" + totalBytes + '\'' +
                ", vulnerability='" + vulnerability + '\'' +
                ", state='" + state + '\'' +
                ", log='" + log + '\'' +
                ", hash='" + hash + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }
}