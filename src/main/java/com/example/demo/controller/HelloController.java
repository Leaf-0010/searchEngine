package com.example.demo.controller;


import com.example.demo.EsDoc;

import com.example.demo.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;


//@RestController
@Controller

public class HelloController {

    @Autowired
    private ContentService contentService2;

    @GetMapping({"/","index"})
    public String index(){
        return "index";
    }
    @GetMapping("/jdsearch")
    public String hello2(Model model){
        return "jdsearch";
    }

    @GetMapping("/contentse")
    public String se(Model model){
        return "se";
    }
    @GetMapping("/se")
    public String search(Model model){
        return "search";
    }

    @GetMapping("/searchAn/{id}")
    public String parsese(Model model, @PathVariable("id") String id) throws IOException, IOException {

        System.out.println(id);
        List<Map<String, Object>> list=contentService2.searchAnswer(id);

        model.addAttribute("id", String.valueOf(list.get(0).get("id")));
        model.addAttribute("context",(String)list.get(0).get("context"));
        model.addAttribute("question",(String)list.get(0).get("question"));
        model.addAttribute("answer",(String)list.get(0).get("answer"));
        model.addAttribute("start", String.valueOf(list.get(0).get("start")));
        model.addAttribute("end", String.valueOf(list.get(0).get("end")));
        model.addAttribute("all_answers",(String)list.get(0).get("all_answers"));

        return "answer";
    }

    @GetMapping("/hello")
    public String hello(Model model){
        model.addAttribute("hello","hello welcome");
        return "test";
    }

    @GetMapping("/hello1")
    @ResponseBody
    public String handle01() throws IOException {
        String str;
        str=EsDoc.searchDoc();
        return str+"\nHello, Spring Boot2!";

    }

    @GetMapping("/getStr")
    @ResponseBody
    public String getStr() throws IOException {
        String str;
        return "\nHello, Spring Boot2!";
    }
}