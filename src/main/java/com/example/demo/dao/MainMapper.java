package com.example.demo.dao;

import com.example.demo.bean.Record;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface MainMapper {
    /**
     * 找出一个用户所有的SCC请求，刷新SCC页面上的数据。
     * @param userId
     * @return
     */
    List<Record> findRecordByUserId(@Param("userId") String userId);

    /**
     * 上传一个新的SCC请求。
     * @param record
     */
    void uploadSCC(Record record);

    /**
     * 删除对应的SCC记录
     * @param recordIds
     */
    void removeSCC(@Param("recordIds") String[] recordIds);


    /**
     * 启动对应的SCC记录的检测任务
     * @param recordId
     */
    void detectSCC(@Param("recordId") String recordId, @Param("num") int num);

    /**
     * 根据recordId查询SCC记录的情况。
     * @param recordId
     * @return
     */
    Record findSCCByRecordId(@Param("recordId") String recordId);

    /**
     * 因为目标record的SCC任务已经完成，所以需要修改状态。
     * @param record
     */
    void overSCC(Record record);
}