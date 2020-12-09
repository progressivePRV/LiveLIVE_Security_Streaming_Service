package com.example.livelive;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    String name, _id, age, email, token;
    ArrayList<String> channels = new ArrayList<String>();

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", _id='" + _id + '\'' +
                ", age='" + age + '\'' +
                ", email='" + email + '\'' +
                ", channels=" + channels +
                '}';
    }
}
