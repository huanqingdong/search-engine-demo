package app.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 爬取逻辑
 *
 * @author faith.huan 2019-10-13 14:37:01
 */
@Slf4j
public class EsDocPageProcessor implements PageProcessor {


    /**
     * 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
     */
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(10000);

    @Override
    public void process(Page page) {

        String currentUrl = page.getUrl().toString();

        if (isIndexPage(currentUrl)) {
            // 如果是index页,则将页面中所有连接加入抓取列表
            List<String> subUrls = page.getHtml().links().all().stream()
                    // 只添加中文页面
                    .filter(EsDocPageProcessor::isCnPage)
                    .collect(Collectors.toList());
            page.addTargetRequests(subUrls);
        } else {
            /*
             * 通过xpath获取标题和内容
             */
            String title = page.getHtml().xpath("//*[@class='title']/text()").toString();
            String content = String.join(" ", page.getHtml().xpath("//p/text()").all());
            if (StringUtils.isAnyBlank(title, content)) {
                // 如果标题或者内容为空,则不保存页面
                page.setSkip(true);
            } else {
                page.putField("title", title);
                page.putField("content", content);
                page.putField("url", currentUrl);
                page.putField("crawlDate", LocalDateTime.now().toString());
            }
        }
    }

    /**
     * 判断url是不是index页
     */
    private boolean isIndexPage(String url) {
        return StringUtils.endsWith(url, "current/index.html");
    }

    /**
     * 判断url是否为中文页,通过包含/cn/来判断
     */
    private static boolean isCnPage(String url){
        return StringUtils.contains(url,"/cn/");
    }

    @Override
    public Site getSite() {
        return site;
    }

}
