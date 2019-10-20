package app;

import app.pojo.EsPage;
import app.util.EsDateUtil;
import app.util.PerformanceMonitor;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将爬取的数据写入到ES中
 *
 * @author faith.huan 2019-10-19 13:08
 */
@Slf4j
public class WriteDataToEs {

    /**
     * 索引名称
     */
    private static final String INDEX_NAME = "es_doc";

    /**
     * 文档类型,使用_doc,固定写法
     */
    private static final String DOC_TYPE = "_doc";

    public static void main(String[] args) {

        try {
            String dir = "D:/es-doc/权威指南";

            // 获取爬取文件集合
            @SuppressWarnings("unchecked")
            Collection<File> collection = FileUtils.listFiles(new File(dir), TrueFileFilter.TRUE, TrueFileFilter.TRUE);

            // 待写入ES对象列表
            List<IndexRequest> requests = new ArrayList<>();

            collection.forEach(file -> {
                try {
                    String content = IOUtils.toString(new FileInputStream(file));
                    EsPage esPage = JSON.parseObject(content, EsPage.class);
                    log.debug("file:{},esPage:{}", file.getPath(), esPage.getTitle());
                    // 只有当标题和内容都不为空时才添加到待写入列表中
                    if (!StringUtils.isAnyBlank(esPage.getContent(), esPage.getTitle())) {
                        // 设置文件名和写入时间
                        esPage.setFileName(file.getName());
                        esPage.setToEsDate(EsDateUtil.getEsDateStringNow());
                        IndexRequest indexRequest = new IndexRequest(INDEX_NAME, DOC_TYPE, esPage.getFileName())
                                .source(JSON.toJSONString(esPage), XContentType.JSON);
                        requests.add(indexRequest);
                    }
                } catch (IOException e) {
                    log.error("将文件解析为EsPage对象失败", e);
                }
            });

            // 使用bulk写入ES
            writeBulkBatch(requests);

            // 使用for循环写入
            //writeForBatch(requests);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 使用bulk批量写入
     *
     * @param requests 待写入IndexRequest列表
     */
    private static void writeBulkBatch(List<IndexRequest> requests) {
        PerformanceMonitor.begin("writeBulkBatch", false);
        RestHighLevelClient client = null;
        try {
            client = getClient();
            BulkRequest bulkRequest = new BulkRequest();
            requests.forEach(bulkRequest::add);
            BulkResponse responses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("写入条数:{},存在异常:{},异常信息:{}", responses.getItems().length, responses.hasFailures(), responses.buildFailureMessage());
        } catch (Exception e) {
            log.error("writeBulkBatch发生异常", e);
        } finally {
            PerformanceMonitor.end();
            closeClient(client);
        }
    }

    /**
     * 使用for循环实现批量写入
     *
     * @param requests 待写入IndexRequest列表
     */
    private static void writeForBatch(List<IndexRequest> requests) {
        PerformanceMonitor.begin("writeForBatch", false);
        RestHighLevelClient client = null;
        try {
            client = getClient();
            for (IndexRequest request : requests) {
                IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
                log.info("indexResponse:{}", indexResponse);
            }
        } catch (Exception e) {
            log.error("writeBulkBatch发生异常", e);
        } finally {
            PerformanceMonitor.end();
            closeClient(client);
        }
    }


    private static void closeClient(RestHighLevelClient client) {
        try {
            if (client != null) {
                client.close();
                log.info("关闭client成功");
            } else {
                log.info("client为null,无需关闭");
            }

        } catch (Exception e) {
            log.error("关闭client发生异常", e);
        }
    }

    /**
     * 获取client
     */
    private static RestHighLevelClient getClient() {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        // 设置用户名和密码,使用为访问es_doc专门创建的search账号
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials("search", "123456"));

        // 设置es的host,端口及认证方式
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("192.168.1.14", 9200))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider));

        return new RestHighLevelClient(builder);
    }


}
