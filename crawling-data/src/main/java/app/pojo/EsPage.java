package app.pojo;

import lombok.Data;

/**
 * @author faith.huan 2019-07-21 11:01
 */
@Data
public class EsPage {

    private String fileName;

    private String title;

    private String content;

    private String url;

    private String crawlDate;

    private String toEsDate;

}
