package com.example.demo.dao;

import com.example.demo.bean.BCCRecord;
import com.example.demo.bean.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface BCCMapper {
    /**
     * 找出一个用户所有的BCC请求，刷新BCC页面上的数据。
     * @param userId
     * @return
     */
    List<BCCRecord> findRecordByUserId(@Param("userId") String userId);

    /**
     * 上传一个新的BCC请求。
     * @param record
     */
    void uploadBCC(BCCRecord record);

    /**
     * 根据recordId查询BCC记录的情况。
     * @param recordId
     * @return
     */
    BCCRecord findBCCByRecordId(@Param("recordId") String recordId);

    /**
     * 删除对应的BCC记录
     * @param recordIds
     */
    void removeBCC(@Param("recordIds") String[] recordIds);

    /**
     * 启动对应的BCC记录的检测任务
     * @param recordId
     */
    void detectBCC(@Param("recordId") String recordId, @Param("num") int num);

    /**
     * 因为目标record的BCC任务已经完成，所以需要修改状态。
     * @param record
     */
    void overBCC(BCCRecord record);
}