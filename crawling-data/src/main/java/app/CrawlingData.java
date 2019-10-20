package app;

import app.processor.EsDocPageProcessor;
import lombok.extern.slf4j.Slf4j;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

import java.io.File;

/**
 * 爬取《Elasticsearch 权威指南》中文版 数据
 * https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html
 *
 * @author faith.huan 2019-07-21 04:00:54
 */
@Slf4j
public class CrawlingData {

    public static void main(String[] args) {
        // 爬取开始路径
        String beginUrl = "https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html";
        // 爬取结果存放文件夹
        String dir = "D:/es-doc/权威指南";

        try {
            File file = new File(dir);
            if (!file.exists()) {
                boolean res = file.mkdir();
                if (res) {
                    log.info("创建目录成功");
                }
            }
            Spider.create(new EsDocPageProcessor())
                    //从url开始抓
                    .addUrl(beginUrl)
                    //设置Scheduler，使用Redis来管理URL队列
                    //.setScheduler(new RedisScheduler("localhost"))
                    //设置Pipeline，将结果以json方式保存到文件
                    .addPipeline(new JsonFilePipeline(dir))
                    //开启50个线程同时执行
                    .thread(50)
                    //启动爬虫
                    .run();

        } catch (Exception e) {
            log.error("启动爬虫发生异常", e);
        }

    }

}
