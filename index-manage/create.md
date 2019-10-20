### 第一个版本

创建一个名为`es_doc_v1`(第一个版本)的索引并为索引创建一个别名`es_doc`

索引字段信息

| 字段名    | 描述     | 类型    | 分词器      |
| --------- | -------- | ------- | ----------- |
| title     | 标题     | text    | ik_max_word |
| url       | url地址  | keyword |             |
| content   | 内容     | text    | ik_max_word |
| crawlDate | 爬取时间 | date    |             |


```nginx
# 创建索引(版本1)
PUT es_doc_v1?include_type_name=false
{
  "settings": {
    "number_of_replicas": 1,
    "number_of_shards": 1,
    "index.highlight.max_analyzed_offset":10000000
  },
  "mappings": {
    "properties": {
      "title":{
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "url":{
        "type": "keyword"
      },
      "content":{
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "crawlDate":{
          "type": "date"
      }
    }
  }
}
# 创建别名
POST /_aliases
{
  "actions" : [
    { "add" : { "index" : "es_doc_v1", "alias" : "es_doc" } }
  ]
}
#别名查看
GET _cat/indices/es*?v

```



### 测试数据写入

```nginx
# 写入测试数据
PUT es_doc/_doc/1
{
  "title":"第一个测试文档",
  "url":"https://www.elastic.co",
  "content":"ElasticSearch是一个基于Lucene的搜索服务器",
  "crawlDate":"2019-10-19T10:17:48.531+08:00"
}
# 查询插入数据
GET es_doc/_search
{
  "query": {
    "match": {
      "_id": "1"
    }
  }
}
# 查询返回数据
{
  "took" : 3,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 1,
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "es_doc_v1",
        "_type" : "_doc",
        "_id" : "1",
        "_score" : 1.0,
        "_source" : {
          "title" : "第一个测试文档",
          "url" : "https://www.elastic.co",
          "content" : "ElasticSearch是一个基于Lucene的搜索服务器",
          "crawlDate" : "2019-10-19T10:17:48.531"
        }
      }
    ]
  }
}
```


### 索引版本升级

创建名称为`es_doc_v2`(第二个版本)的索引对`es_doc_v1`索引进行升级，并将别名`es_doc`指向`es_doc_v2`。

第二个版本的索引`es_doc_v2`中添加如下字段，并将副本数改为`0`

| 字段名    | 描述     | 类型    | 分词器      |
| --------- | -------- | ------- | ----------- |
| toEsDate | 写入ES时间 | date    ||
| fileName | 文件名 | keyword ||

```nginx
# 创建新版本索引es_doc_v2
PUT es_doc_v2?include_type_name=false
{
  "settings": {
    "number_of_replicas": 0,
    "number_of_shards": 1,
    "index.highlight.max_analyzed_offset":10000000
  },
  "mappings": {
    "properties": {
      "fileName":{
        "type": "keyword"
      },
      "title":{
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "url":{
        "type": "keyword"
      },
      "content":{
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "crawlDate":{
          "type": "date"
      },
      "toEsDate":{
          "type": "date"
      }
    }
  }
}

# 将1版本索引中的数据迁移到2版本中
POST _reindex
{
  "source": {
    "index": "es_doc_v1"
  },
  "dest": {
    "index": "es_doc_v2"
  }
}

# 检查测试数据是否迁移到es_doc_v2中
GET es_doc_v2/_search
{
  "query": {
    "match": {
      "_id": "1"
    }
  }
}

# 移除别名执行1版本,添加别名指向2版本
POST /_aliases
{
    "actions" : [
        { "remove" : { "index" : "es_doc_v1", "alias" : "es_doc" } },
        { "add" : { "index" : "es_doc_v2", "alias" : "es_doc" } }
    ]
}

# 别名es_doc中是否有测试数据
GET es_doc/_search
{
  "query": {
    "match": {
      "_id": "1"
    }
  }
}

# 删除1版本的索引
DELETE es_doc_v1
```

