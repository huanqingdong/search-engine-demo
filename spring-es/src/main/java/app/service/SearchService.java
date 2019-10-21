package app.service;

import app.vo.EsPageVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 全文检索服务类
 *
 * @author faith.huan 2019-10-21 21:42:02
 */
@Service
@Slf4j
public class SearchService {

    private static final String INDEX_NAME = "es_doc";
    private static final String DOC_TYPE = "_doc";


    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    /**
     * 高亮字段
     */
    private final HighlightBuilder.Field[] highlightFields = new HighlightBuilder.Field[]{
            new HighlightBuilder.Field("content").fragmentSize(500).numOfFragments(1).noMatchSize(500).preTags("<font color='red'>").postTags("</font>"),
            new HighlightBuilder.Field("title").fragmentSize(150).numOfFragments(1).noMatchSize(150).preTags("<font color='red'>").postTags("</font>")
    };
    /**
     * 显示字段筛选,不显示content字段
     */
    private final SourceFilter sourceFilter = new FetchSourceFilterBuilder().withExcludes("content").build();


    public SearchService(ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    /**
     * 全文检索
     *
     * @param keyword  关键词
     * @param type     检索类型  phrase|term
     * @param page     页码
     * @param pageSize 一页条数
     */
    public AggregatedPage<EsPageVO> fullTextSearch(String keyword, String type,
                                                 int page, int pageSize) {
        try {
            QueryBuilder queryBuilder = buildKeywordQuery(keyword, type);

            SearchQuery searchQuery = new NativeSearchQueryBuilder().withIndices(INDEX_NAME).withTypes(DOC_TYPE)
                    .withQuery(queryBuilder)
                    // 设置字段筛选
                    .withSourceFilter(sourceFilter)
                    // 设置高亮字段
                    .withHighlightFields(highlightFields)
                    // 设置分页
                    .withPageable(PageRequest.of(page, pageSize)).build();

            return elasticsearchRestTemplate.queryForPage(searchQuery, EsPageVO.class,new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                    List<EsPageVO> list = new ArrayList<>();
                    SearchHits hits = response.getHits();
                    for (SearchHit searchHit : hits) {
                        EsPageVO doc = new EsPageVO();
                        Map<String, Object> sourceMap = searchHit.getSourceAsMap();
                        doc.setScore(searchHit.getScore());
                        doc.setFileName(MapUtils.getString(sourceMap, "fileName"));
                        doc.setUrl(MapUtils.getString(sourceMap, "url"));
                        doc.setCrawlDate(MapUtils.getString(sourceMap, "crawlDate"));
                        doc.setToEsDate(MapUtils.getString(sourceMap, "toEsDate"));

                        // 高亮字段处理
                        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                        // 标题高亮
                        if (highlightFields.containsKey("title")) {
                            Text[] titles = highlightFields.get("title").getFragments();
                            doc.setTitle(titles[0].string());
                        } else {
                            log.warn("未找到标题高亮内容");
                            doc.setTitle(MapUtils.getString(sourceMap, "title"));
                        }
                        // 正文高亮
                        if (highlightFields.containsKey("content")) {
                            Text[] contents = highlightFields.get("content").getFragments();
                            doc.setContent(contents[0].string());
                        } else {
                            log.warn("未找到正文高亮内容");
                            doc.setContent("无正文内容");
                        }
                        list.add(doc);
                    }

                    return new AggregatedPageImpl<T>((List<T>) list, pageable, hits.getTotalHits(),  hits.getMaxScore());

                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> type) {
                    return null;
                }
            });

        } catch (Exception e) {
            log.error("高级查询发生异常", e);
            return null;
        }
    }

    /**
     * 根据关键词和检索类型构建查询
     *
     * @param keyword 关键词
     * @param type    查询类型
     * @return QueryBuilder
     */
    private QueryBuilder buildKeywordQuery(String keyword, String type) {
        if ("phrase".equals(type)) {
            // 使用短语匹配查询
            log.debug("matchPhraseQuery,keyword:{}", keyword);
            BoolQueryBuilder builder = QueryBuilders.boolQuery();
            builder.should().add(QueryBuilders.matchPhraseQuery("title", keyword));
            builder.should().add(QueryBuilders.matchPhraseQuery("content", keyword));
            return builder;
        } else {
            // 使用分词查询
            log.debug("multiMatchQuery,keyword:{}", keyword);
            return QueryBuilders.multiMatchQuery(keyword, "content", "title");
        }

    }

}
