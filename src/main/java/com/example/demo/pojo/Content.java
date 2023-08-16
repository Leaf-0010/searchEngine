package com.example.demo.pojo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Content {
    private String title;
    private String detail;
    private String answer;
    private String summary;
    private String Url;
    private String img;
    private String price;
    private String context;
    private String question;
    private int start;
    private int end;
    private String all_answers;
    private int id;
    //可以自行添加属性
}