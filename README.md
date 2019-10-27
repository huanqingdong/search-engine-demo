# 搜索引擎示例

## 项目简介

基于ES构建一个简单的搜索引擎

* 项目效果图

![Image text](https://img-blog.csdnimg.cn/20191027213606550.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2h1YW5xaW5nZG9uZw==,size_16,color_FFFFFF,t_70)

## 项目模块

### docker-build 

- 构建elasticsearch docker镜像所有脚本及资源文件

### docker-compose

- 启动elasticsearch的docker-compose.yml文件
- 启动nginx的docker-compose.yml文件
- 启动kibana的docker-compose.yml文件

### crawling-data

- 使用webmagic框架爬取数据
- 将爬取数据写入到ES中

### index-manage

- 索引创建相关文档

### spring-es

- 全文检索接口
- 前端展示页面 

