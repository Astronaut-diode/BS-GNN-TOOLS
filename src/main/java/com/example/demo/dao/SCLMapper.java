package com.example.demo.dao;

import com.example.demo.bean.Record;
import com.example.demo.bean.SCLRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface SCLMapper {
    /**
     * 找出一个用户所有的SCL请求，刷新SCL页面上的数据。
     * @param userId
     * @return
     */
    List<SCLRecord> findRecordByUserId(@Param("userId") String userId);

    /**
     * 上传一个新的SCL请求。
     * @param record
     */
    void uploadSCL(SCLRecord record);

    /**
     * 删除对应的SCL记录
     * @param recordIds
     */
    void removeSCL(@Param("recordIds") String[] recordIds);

    /**
     * 根据recordId查询SCL记录的情况。
     * @param recordId
     * @return
     */
    SCLRecord findSCLByRecordId(@Param("recordId") String recordId);

    /**
     * 启动对应的SCL记录的检测任务
     * @param recordId
     */
    void detectSCL(@Param("recordId") String recordId, @Param("num") int num);

    /**
     * 因为目标record的SCL任务已经完成，所以需要修改状态。
     * @param record
     */
    void overSCL(SCLRecord record);
}