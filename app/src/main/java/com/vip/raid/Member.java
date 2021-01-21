package com.vip.raid;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Member {
    private String name, named1, named2, named3, named4;

    public Member() {

    }

    public Member(String name, String named1, String named2, String named3, String named4) {
        this.name = name;
        this.named1 = named1;
        this.named2 = named2;
        this.named3 = named3;
        this.named4 = named4;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamed1() {
        return named1;
    }

    public void setNamed1(String named1) {
        this.named1 = named1;
    }

    public String getNamed2() {
        return named2;
    }

    public void setNamed2(String named2) {
        this.named2 = named2;
    }

    public String getNamed3() {
        return named3;
    }

    public void setNamed3(String named3) {
        this.named3 = named3;
    }

    public String getNamed4() {
        return named4;
    }

    public void setNamed4(String named4) {
        this.named4 = named4;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("Named1", named1);
        result.put("Named2", named2);
        result.put("Named3", named3);
        result.put("Named4", named4);
        return result;
    }



}
