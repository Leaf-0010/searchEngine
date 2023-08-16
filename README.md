# 全文检索系统

### 1.运行步骤

##### 数据流程：

打开elasicsearch->运行DemoApplication.java->输入localhost:8080/contentse进入页面->开始搜索

##### meta-search：

对于输入的问题判断是否在elasticsearch之中找到对应的匹配,若找不到则调用HtmlParseUtil.java进行对问题在必应之中进行搜索,找到之后逐个解析对应的网站链接,进入之中后获取相应的网站的文字,接着将对应的信息对应的index存入es之中，便于被搜索。

### 2.排序算法说明

定义了一个权重变量 weight，并使用 randomScore() 函数生成一个随机分数 random。最后，将权重和随机分数相乘作为文档的评分。
脚本评分的规则是将权重乘以一个随机分数。这意味着具有更高权重的文档将在排序中排在前面，而随机分数的引入可以为相同权重的文档引入一定的随机性，以确保排序的多样性。
使用MultiMatchQueryBuilder进行多字段匹配，并通过.field("字段名", 权重)设置字段的权重比例。
脚本评分规则基于字段 question.keyword 评分。
若字段 question.keyword 存在，并且与参数 keyword 完全匹配，给予基础权重 0.6 和完全匹配权重 0.4。
若字段 question.keyword 存在，并且包含参数 keyword，给予基础权重 0.6 和部分匹配权重 0.3。
若字段 question.keyword 不存在或不满足上述条件，则评分为 0.0。

