package com.yongle.letuiweipad.domain.createorder;

import java.io.Serializable;

/**
 * Created by bert_dong on 2016/11/8 0008.
 * 邮箱：18701038771@163.com
 */
public class UsedRoom implements Serializable{

    private String roomName;

    public UsedRoom() {
    }

    public UsedRoom(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
