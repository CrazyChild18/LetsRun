package com.lyk.mymap.User;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * 好友类
 * 储存好友属性
 * 实现好友表的数据交互
 *
 * Friend class
 * Store friend attributes
 * Realize the data interaction of the friends table
 */

public class Friend extends BmobObject {

    private String name;
    private BmobFile image;
    private User user;
    private String distance;

    public String getDistance(){
        return distance;
    }

    public Friend setDistance(String distance){
        this.distance = distance;
        return this;
    }

    public Friend(){

    }

    public Friend(String name, BmobFile image){
        this.name = name;
        this.image = image;
    }

    public String getName(){
        return name;
    }

    public Friend setName(String name){
        this.name = name;
        return this;
    }

    public BmobFile getImage(){
        return image;
    }

    public Friend setImage(BmobFile image){
        this.image = image;
        return this;
    }

    public User getUser(){
        return user;
    }

    public Friend setUser(User user){
        this.user = user;
        return this;
    }
}
