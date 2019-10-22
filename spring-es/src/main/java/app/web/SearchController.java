package app.web;

import app.service.SearchService;
import app.vo.EsPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.web.bind.annotation.*;

/**
 * 全文检索Controller
 *
 * @author faith.huan 2019-10-21 21:29:26
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }


    /**
     * 全文检索方法
     *
     * @param keyword  关键字
     * @param type     检索类型
     * @param page     页码
     * @param pageSize 一页条数
     */
    @CrossOrigin
    @PostMapping("/fullTextSearch")
    public AggregatedPage<EsPageVO> fullTextSearch(@RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                                                   @RequestParam(value = "type", required = false, defaultValue = "") String type,
                                                   @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize) {

        log.info("keyword:{}, type:{},page:{},pageSize:{}", keyword, type, page, pageSize);

        return searchService.fullTextSearch(keyword, type, page, pageSize);
    }


}
