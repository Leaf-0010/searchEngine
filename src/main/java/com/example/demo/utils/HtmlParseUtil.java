package com.example.demo.utils;

import com.example.demo.pojo.Content;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@Component


public class HtmlParseUtil {

//    public static void main(String[] args) throws IOException {
//        new HtmlParseUtil().parseJD("JavaScript 中如何进行音视频处理").forEach(System.out::println);
//    }
    static int m = 0;
    public List<Content> parseJD(String keywords) throws IOException {

        keywords = java.net.URLEncoder.encode(keywords, "UTF-8");

        List<Content> contentList = new ArrayList<>();

        boolean databaseResultsFound = false; // 标记是否在数据库中找到匹配结果

        // 在数据库中搜索匹配结果
        List<Content> databaseResults = searchFromDatabase(keywords);

        if (!databaseResults.isEmpty()) {
            contentList.addAll(databaseResults);
            databaseResultsFound = true;
        }
        if(!databaseResultsFound) {
                keywords = keywords.replace(" ", "");

                String url = "https://cn.bing.com/search?q=" + keywords + "&qs=n" + "&form=QBRE" + "&sp=" + "&lq=0" + "&pq=" + keywords + "&sc=" + "&sk=" + "&cvid=" + "&ghsh=" + "&ghacc=" + "&ghpl=";

                System.out.println(url);
//            java.awt.Desktop.getDesktop().browse(URI.create(url));
                Connection connection = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                        .timeout(5000);
                Document document = connection.get();
                Elements elements = document.getElementsByClass("b_algo");
                System.out.println(elements.size());
                for (Element element : elements) {
//                Elements a = element.getElementsByTag("a");

                    String title = element.getElementsByClass("b_title").eq(0).text();
//                String url0 = element.getElementsByClass("b_title").eq(0).attr("href");
                    String url0 = element.getElementsByTag("a").eq(0).attr("href");
//                String url0 = a.get(0).attr("href");
                    Connection connection1 = Jsoup.connect(url0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                            .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                            .timeout(5000);
                    Document document1;
                    try {
                        document1 = connection1.get();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    String text0 = document1.text();
                    StringBuilder builder = new StringBuilder(text0);
                    int index = 0;
                    while (builder.length() > index) {
                        char tmp = builder.charAt(index);
                        if (Character.isSpaceChar(tmp) || Character.isWhitespace(tmp)) {
                            builder.setCharAt(index, ' ');
                        }
                        index++;
                    }
                    text0 = builder.toString().replaceAll(" +", " ").trim();
               Elements elements1 = element.getElementsByTag("a");
                String text5  = elements1.html();
                text5 = text5.replace("<strong>","");
                text5 = text5.replace("</strong>","");
                System.out.println("................"+text5+"................");
                Elements elements2 = element.getElementsByClass("algoSlug_icon");
                String textanswer = elements2.html();
                textanswer = textanswer.replace("<strong>","");
                textanswer = textanswer.replace("<strong>","");
                    String[] str = text0.split(" ");
                    int start = 0;
                    int end = 0;
                    String now_text = new String();
                    for (int j = 0; j < str.length; j++) {
                        if (str[j].length() >= 50 && str[j].length() <= 500) {
                            for (int x = j; x < str.length - 2; x++) {
                                now_text = now_text + str[x];
                                start = j;
                                end = end + str[x].length();
                            }
                            break;
                        }
//                    System.out.println(str[j]);

                    }
                    System.out.println(now_text);
                    Content content = new Content();
                    content.setContext(now_text);
                    content.setQuestion(text5);
                    content.setAnswer(url0);
                    content.setStart(start);
                    content.setAll_answers(now_text);
                    content.setEnd(end);
                    content.setId(6000+m);
                    m++;
                    System.out.println(title + url0);
                    contentList.add(content);
                }
        }
        return contentList;
    }


    // 模拟从数据库中搜索匹配结果的方法，您需要根据实际情况进行修改
    private List<Content> searchFromDatabase(String keywords) {
        List<Content> databaseResults = new ArrayList<>();

        // 在数据库中搜索匹配结果，并将结果添加到databaseResults列表中

        return databaseResults;
    }
    public static class Content {
        private String title;
        private String answer;
        private String context;
        private String question;
        private int start;
        private int id;
        private String all_answers;
        private int end;
        public String getTitle() {
            return title;
        }
        public void setEnd(int end){
            this.end = end;
        }
        public void setTitle(String title) {
            this.title = title;
        }

        public void setContext(String context) {
            this.context = context ;
        }
        public void setQuestion(String question) {
            this.question = question;
        }

        public void setAnswer(String text0) {
            this.answer = text0;
        }

        public void setAll_answers(String nowText) {
            this.all_answers = nowText;
        }

        public void setStart(int start) {
            this.start = start;
        }
        public void setId(int id){
            this.id = id;
        }
        public int getEnd(){
            return end;
        }
        public String getContext() {
            return context;
        }
        public String getQuestion(){
            return question;
        }
        public String getAnswer(){
            return answer;
        }
        public String getAll_answers(){
            return all_answers;
        }
        public int getStart(){
            return start;
        }
        public int getId(){
            return id;
        }
    }

}

/*
public class HtmlParseUtil {

    //测试数据
    public static void main(String[] args) throws IOException, InterruptedException {
        //获取请求
        String url = "https://search.jd.com/Search?keyword=python";
        // 解析网页 （Jsou返回的Document就是浏览器的Docuement对象）
        Document document = Jsoup.parse(new URL(url), 30000);
        //获取id，所有在js里面使用的方法在这里都可以使用
        Element element = document.getElementById("J_goodsList");
        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");
        //用来计数
        int c = 0;
        //获取元素中的内容  ，这里的el就是每一个li标签
        for (Element el : elements) {
            c++;
            //这里有一点要注意，直接attr使用src是爬不出来的，因为京东使用了img懒加载
            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            //获取商品的价格，并且只获取第一个text文本内容
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            String shopName = el.getElementsByClass("p-shop").eq(0).text();

            System.out.println("========================================");
            System.out.println(img);
            System.out.println(price);
            System.out.println(title);
            System.out.println(shopName);
        }
        System.out.println(c);
    }
}*/
