package com.dao;

import com.model.FlightInfo;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by HeXi on 2018/4/5.
 */
public interface FlightInfoDAO {
    // 查询入库时间
    Date selectCreateTime(@Param("depDate") String depDate, @Param("flightNo") String flightNo);
    List<Date> selectCreateTimeList(@Param("depDate") String depDate, @Param("depCity") String depCity, @Param("arrCity") String arrCity);

    int insertFlightInfo(@Param("flightInfo") FlightInfo flightInfo);

    int insertFlightInfoList(@Param("flightInfoList") List<FlightInfo> flightInfos);

    int updateFlightInfo(@Param("flightInfo") FlightInfo flightInfo);

    int updateSeatDown(@Param("id") long id, @Param("delta") int delta);

    int updateSeatAndCreateTime(@Param("flightInfo") FlightInfo flightInfo);

    FlightInfo selectFlightInfoByDepDateAndFlightNo(@Param("depDate") String depDate, @Param("flightNo") String flightNo);

    FlightInfo selectFlightInfoById(@Param("id") long id);

    int deleteFlightInfoById(@Param("id") long id);

    int deleteFlightInfoByDepDateAndFlightNo(@Param("depDate") String depDate, @Param("flightNo") String flightNo);

    List<FlightInfo> selectSearchResult(@Param("depCity") String depCity,
                                        @Param("arrCity") String arrCity,
                                        @Param("depDate") String depDate,
                                        @Param("number") int number);

    List<FlightInfo> selectSearchResultWithoutPassengerCount(@Param("depCity") String depCity,
                                        @Param("arrCity") String arrCity,
                                        @Param("depDate") String depDate);
}
