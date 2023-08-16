package com.example.demo.service;

import java.lang.*;
import com.alibaba.fastjson.JSON;
import com.example.demo.pojo.Answer;
import com.example.demo.pojo.Content;
import com.example.demo.pojo.Question;
import com.example.demo.utils.JsonParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.query.functionscore.ScriptScoreQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.demo.utils.HtmlParseUtil;
@Service
public class ContentService {

    //将客户端注入
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    //1、解析数据放到 es 中
    public boolean parseContent(String keyword) throws IOException {
        List<HtmlParseUtil.Content> contents = new HtmlParseUtil().parseJD(keyword);
        //把查询的数据放入 es 中
        BulkRequest request = new BulkRequest();
        request.timeout("2m");

        for (int i = 0; i < contents.size(); i++) {
            request.add(
                    new IndexRequest("group-data")
                            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));

        }
        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    //2、获取这些数据实现基本的搜索功能
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        //keyword="机器学习";
       // keyword=keyword.getBytes("UTF-8").toString();
        if (pageNo <= 1) {
            pageNo = 1;
        }
        if (pageSize <= 1) {
            pageSize = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("group-data");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo).size(pageSize);

        //精准匹配
       // TermQueryBuilder termQuery = QueryBuilders.termQuery("title", keyword);
        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("title", keyword);


        //sourceBuilder.query(termQuery);
        sourceBuilder.query(matchQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行搜索
        SearchRequest source = searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }
        return list;
    }

    public List<Map<String, Object>> searchQA(String keyword, int pageNo, int pageSize) throws IOException {
        //keyword="机器学习";
        // keyword=keyword.getBytes("UTF-8").toString();
        if (pageNo <= 1) {
            pageNo = 1;
        }
        if (pageSize <= 1) {
            pageSize = 1;
        }

        //条件搜索
        SearchRequest searchRequest = new SearchRequest("group-data");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo).size(pageSize);
         // 构建多字段查询
//        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword)
//                .field("question", 8) // 设置问题字段的权重为 8
//                .field("context", 2); // 设置内容字段的权重为 2
//        sourceBuilder.query(multiMatchQuery);

//

        // 精准匹配 --- 不调整排序算法
//         TermQueryBuilder termQuery = QueryBuilders.termQuery("answer", keyword);
//        sourceBuilder.query(termQuery);
//
//        MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("question", keyword);
//        sourceBuilder.query(matchQuery);

//////        System.out.println(keyword_buff);




        //调整排序算法 ---boost
        String[] keyword_buff = keyword.trim().split(" ");
        if(keyword_buff.length<=1){
            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("question", keyword);
            sourceBuilder.query(matchQuery);
        }
        else{
            MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("question", keyword_buff[0]);
            matchQuery1.boost(2);

            String keyword_left=keyword_buff[1];
            for(int i=2;i<keyword_buff.length;i++){
                keyword_left=" "+keyword_buff[i];
            }
            MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("context", keyword_left);
            String scoreScript = "double relevance = 0.0;\n" +
                    "if (doc['question.keyword'].size() > 0) {\n" +
                    "   relevance += 0.2; // 基础权重\n" +
                    "   if (doc['question.keyword'].value == params.keyword) {\n" +
                    "       relevance += 0.4; // 完全匹配权重\n" +
                    "   } else if (doc['question.keyword'].value.contains(params.keyword)) {\n" +
                    "       relevance += 0.3; // 部分匹配权重\n" +
                    "   }\n" +
                    "}\n" +
                    "return relevance;";
            Map paraMap=new HashMap();
            Script script=new Script(Script.DEFAULT_SCRIPT_TYPE,"painless",scoreScript,paraMap);
            ScriptScoreQueryBuilder scriptScoreQueryBuilder=QueryBuilders.scriptScoreQuery(matchQuery2,script);
            BoolQueryBuilder boolQueryBuilder=QueryBuilders.boolQuery();
            boolQueryBuilder.should(matchQuery1);
            boolQueryBuilder.should(scriptScoreQueryBuilder);
            sourceBuilder.query(boolQueryBuilder);
        }

//       // 调整排序算法 ---boost positive and negative
//        String[] keyword_buff = keyword.trim().split(" ");
//        if(keyword_buff.length<=1){
//            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("context", keyword);
//            sourceBuilder.query(matchQuery);
//        }
//        else{
//            MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("context", keyword_buff[0]);
//            matchQuery1.boost(2);
//
//            String keyword_left=keyword_buff[1];
//            for(int i=2;i<keyword_buff.length;i++){
//                keyword_left=" "+keyword_buff[i];
//            }
//            MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("context", keyword_left);
//            BoostingQueryBuilder boosting=QueryBuilders.boostingQuery(matchQuery1,matchQuery2);
//            boosting.negativeBoost(0.2f);
//            sourceBuilder.query(boosting);
//        }

        //调整排序算法 ---使用script score
//        String[] keyword_buff = keyword.trim().split(" ");
//        System.out.println(keyword_buff);
//        if(keyword_buff.length<=1){
//            MatchQueryBuilder matchQuery = QueryBuilders.matchQuery("question", keyword);
//            sourceBuilder.query(matchQuery);
//        }
//        else{
//            MatchQueryBuilder matchQuery1 = QueryBuilders.matchQuery("question", keyword_buff[0]);
//            matchQuery1.boost(1);
//
//            String keyword_left=keyword_buff[1];
//            for(int i=2;i<keyword_buff.length;i++){
//                keyword_left=" "+keyword_buff[i];
//            }
//            MatchQueryBuilder matchQuery2 = QueryBuilders.matchQuery("context", keyword_left);
//            matchQuery2.boost(5);
//            String scoreScript ="int weight=10;\n"+
//                                "def random= randomScore(params.uuidHash);\n"+
//                                "return weight*random";
//            Map paraMap=new HashMap();
//            int randint=(int)(Math.random()*100);
//            System.out.println(randint);
//            paraMap.put("uuidHash",randint);
//            Script script=new Script(Script.DEFAULT_SCRIPT_TYPE,"painless",scoreScript,paraMap);
//            ScriptScoreQueryBuilder scriptScoreQueryBuilder=QueryBuilders.scriptScoreQuery(matchQuery2,script);
//            BoolQueryBuilder boolQueryBuilder=QueryBuilders.boolQuery();
//            boolQueryBuilder.should(matchQuery1);
//            boolQueryBuilder.should(scriptScoreQueryBuilder);
//            sourceBuilder.query(boolQueryBuilder);
//        }
//
        MultiMatchQueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery(keyword)
                .field("question", 8) // 设置问题字段的权重为 8
                .field("context", 2); // 设置内容字段的权重为 2
        sourceBuilder.query(multiMatchQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行搜索
        SearchRequest source = searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }

        // 当数据库无匹配结果时，调用HtmlParseUtil进行网页搜索和解析

        if (true) {
            HtmlParseUtil htmlParseUtil = new HtmlParseUtil();
            List<HtmlParseUtil.Content> parsedResults = htmlParseUtil.parseJD(keyword);
            // 将解析的结果添加到返回的列表中
            for (HtmlParseUtil.Content parsedResult : parsedResults) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("question", parsedResult.getQuestion());
                resultMap.put("answer", parsedResult.getAnswer());
                resultMap.put("All_answers",parsedResult.getAll_answers());
                resultMap.put("context",parsedResult.getContext());
                resultMap.put("start",parsedResult.getStart());
                resultMap.put("id" ,parsedResult.getId());
                resultMap.put("end",parsedResult.getEnd());
                list.add(resultMap);
            }
            List<HtmlParseUtil.Content> contents = new HtmlParseUtil().parseJD(keyword);
            //把查询的数据放入 es 中
            BulkRequest request = new BulkRequest();
            request.timeout("2m");

            for (int i = 0; i < contents.size(); i++) {
                request.add(
                        new IndexRequest("group-data")
                                .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));

            }
            BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        }

        return list;
    }


    public List<Map<String, Object>> searchAnswer(String id) throws IOException {
        //条件搜索insurance_question
        SearchRequest searchRequest = new SearchRequest("group-data");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //精准匹配
        TermQueryBuilder termQuery = QueryBuilders.termQuery("id", id);
        //TermQueryBuilder matchQuery = QueryBuilders.termQuery("qid", qid);

        sourceBuilder.query(termQuery);
        //sourceBuilder.query(matchQuery);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //执行搜索
        SearchRequest source = searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }

        return list;
    }

    public boolean writeQAContent() throws IOException {

        //write quesitons into ES
        String file_path="E:/elk/pool/all_groups_data.json";
        List<Question> questionList = new JsonParseUtil().parseJson(file_path);



        //把查询的数据放入 es 中
        BulkRequest request = new BulkRequest();
        request.timeout("2m");

        for (int i = 0; i < questionList.size(); i++) {
            request.add(
                    new IndexRequest("all_groups_data")
                            .source(JSON.toJSONString(questionList.get(i)), XContentType.JSON));

        }
        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);

        //write answers into ES
        file_path="E:/elk/pool/answersnew.json";
        List<Answer> answerList = new JsonParseUtil().parseAnJson(file_path);

        //把查询的数据放入 es 中
        request = new BulkRequest();
        request.timeout("2m");

        for (int i = 0; i < answerList.size(); i++) {
            request.add(
                    new IndexRequest("insurance_answer")
                            .source(JSON.toJSONString(answerList.get(i)), XContentType.JSON));

        }
        bulk = client.bulk(request, RequestOptions.DEFAULT);

        return !bulk.hasFailures();
    }

}
