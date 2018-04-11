package com.dao;

import com.model.FlightUser;
import org.apache.ibatis.annotations.Param;

/**
 * Created by HeXi on 2018/4/6.
 */
public interface FlightUserDAO {
    int insertIntoFlightUser(@Param("flightUser") FlightUser flightUser);
}
