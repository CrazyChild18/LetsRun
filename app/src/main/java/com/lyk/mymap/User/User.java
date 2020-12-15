package com.lyk.mymap.User;

import java.lang.reflect.Array;
import java.util.Date;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;

/**
 * 用户属性类
 * 储存用户信息
 * 与User表交互
 *
 * User attribute class
 * Storing user information
 * Interact with the User table
 */

public class User extends BmobUser {

    //昵称
    //nickname
    private String nickname;

    //年龄
    //age
    private Integer age;

    //性别
    //gender
    private String gender;

    //头像
    //avater
    private BmobFile avatar;

    //生日
    //birthday
    private String birthday;

    public String getBirthday(){
        return birthday;
    }

    public User setBirthday(String birthday){
        this.birthday = birthday;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public User setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public User setAge(Integer age) {
        this.age = age;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public User setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public User setAvatar(BmobFile avatar) {
        this.avatar = avatar;
        return this;
    }
}
