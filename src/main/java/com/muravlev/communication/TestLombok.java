package com.muravlev.communication;

import lombok.Data;

@Data
public class TestLombok {
    private String field;

    public static void main(String[] args) {
        TestLombok obj = new TestLombok();
        obj.setField("Hello, Lombok!");
        System.out.println(obj.getField());
    }
}
