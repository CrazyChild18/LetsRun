package com.lyk.mymap.User;

import java.util.Date;

import cn.bmob.v3.BmobObject;

/**
 * 轨迹类
 * 储存每一个轨迹的属性
 * 与Trace表交互
 *
 * Trajectory classes
 * Stores the properties of each trajectory
 * Interact with the Trace table
 */

public class Trace extends BmobObject {
    private String StartDate;
    private String EndDate;
    private String StartTime;
    private String EndTime;
    private String endTimeMillis;
    private String startTimeMillis;
    private String  distance;
    private User user;

    public String getDistance(){
        return distance;
    }

    public Trace setDistance(String distance){
        this.distance = distance;
        return this;
    }

    public String getStartTimeMillis(){
        return startTimeMillis;
    }

    public Trace setStartTimeMillis(String startTimeMillis){
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    public String getEndTimeMillis(){
        return endTimeMillis;
    }

    public Trace setEndTimeMillis(String endTimeMillis){
        this.endTimeMillis = endTimeMillis;
        return this;
    }

    public String getStartDate(){
        return StartDate;
    }

    public Trace setStartDate(String StartDate){
        this.StartDate = StartDate;
        return this;
    }

    public String getEndDate(){
        return EndDate;
    }

    public Trace setEndDate(String endDate){
        this.EndDate = endDate;
        return this;
    }

    public String getStartTime(){
        return StartTime;
    }

    public Trace setStartTime(String startTime){
        this.StartTime = startTime;
        return this;
    }

    public String getEndTime(){
        return EndTime;
    }

    public Trace setEndTime(String endTime){
        this.EndTime = endTime;
        return this;
    }

    public User getUser(){
        return user;
    }

    public Trace setUser(User user){
        this.user = user;
        return this;
    }

}
