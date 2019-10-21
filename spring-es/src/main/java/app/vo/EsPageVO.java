package app.vo;

import lombok.Data;

/**
 * @author faith.huan 2019-07-21 11:01
 */
@Data
public class EsPageVO {

    /**
     * 搜索评分
     */
    private Float score;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 原文地址
     */
    private String  url;

    /**
     * 爬取时间
     */
    private String crawlDate;

    /**
     * 写入ES时间
     */
    private String toEsDate;

}
