package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.search.service.impl
 * @company www.itheima.com
 */
@Service(timeout = 5000)//设置超时时间 5 秒钟  默认有一个超时的时间 是 1 秒钟
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;
    @Override
    public Map search(Map searchMap) {
        Map resultMap = new HashMap();
        //高亮查询
        Map map = searchHightList(searchMap);
        resultMap.putAll(map);
        //根据关键字来进行分组查询得到分类的列表
        List<String> categoryList = searchCategoryList(searchMap);
        resultMap.put("categoryList",categoryList);

        //获取品牌列表 和规格的列表（判断 如果是被点击了分类 要根据被点击的分类来实现搜索，默认展示第一个）
        if(!"".equals(searchMap.get("category"))){
            //说明有人点击了某一个商品的分类 需要根据这个商品分类来实现查询
            Map map1 = searchBrandListAndSpecListByCategory((String)searchMap.get("category"));
            resultMap.putAll(map1);
        }else {//展示默认的
            if (categoryList != null && categoryList.size() > 0) {
                Map map1 = searchBrandListAndSpecListByCategory(categoryList.get(0));
                resultMap.putAll(map1);
            }
        }

        return resultMap;
    }

    private Map searchHightList(Map searchMap){
        Map map = new HashMap();

        //1.先获取从页面传递过来的参数的值   通过KEY获取
        String keywords = (String)searchMap.get("keywords");//获取主查询的条件

        //2.创建查询的对象    设置查询的条件  主查询条件
       /* Query query = new SimpleQuery("*:*");

        Criteria criteria = new Criteria("item_keywords");
        criteria.is(keywords);//item_keywords:手机
        query.addCriteria(criteria);*/
       //2.设置主查询的条件
        HighlightQuery query =  new SimpleHighlightQuery();
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(keywords);
        query.addCriteria(criteria);
        //3.设置高亮查询的条件   设置高亮显示的域  设置前缀  设置后缀
        HighlightOptions hightoptions = new HighlightOptions();
        hightoptions.addField("item_title");//设置高亮显示的域
        hightoptions.setSimplePrefix("<em style=\"color:red\">");
        hightoptions.setSimplePostfix("</em>");
        query.setHighlightOptions(hightoptions);



        //设置过滤条件  商品分类的过滤
        if (searchMap.get("category") != null && !"".equals(searchMap.get("category"))) {
            Criteria fitercriteria = new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterquery = new SimpleFilterQuery(fitercriteria);
            query.addFilterQuery(filterquery);//
        }

        //设置品牌的过滤
        if (searchMap.get("brand") != null && !"".equals(searchMap.get("brand"))) {
            Criteria fitercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterquery = new SimpleFilterQuery(fitercriteria);
            query.addFilterQuery(filterquery);//
        }

        //设置规格的过滤条件

        if (searchMap.get("spec") != null) {
            Map<String,String> spec = (Map<String, String>) searchMap.get("spec");

            for (String key : spec.keySet()) {
                String value = spec.get(key);
                Criteria fitercriteria = new Criteria("item_spec_"+key).is(value);//item_spec_网络：3G
                FilterQuery filterquery = new SimpleFilterQuery(fitercriteria);
                query.addFilterQuery(filterquery);//
            }
        }


        //4.执行查询 获取高亮数据
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            TbItem entity = tbItemHighlightEntry.getEntity();//实体对象 现在是没有高亮的数据的

            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
            //高亮对象
//            for (HighlightEntry.Highlight highlight : highlights) {
//                System.out.println("高亮显示的域:"+highlight.getField());
//                List<String> snipplets = highlight.getSnipplets();//["",""]
//                System.out.println(snipplets);
//            }
            //如有高亮，就取高亮
            if(highlights!=null && highlights.size()>0 && highlights.get(0)!=null &&  highlights.get(0).getSnipplets()!=null && highlights.get(0).getSnipplets().size()>0) {
                entity.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }


        List<TbItem> tbItems = highlightPage.getContent();//获取未高亮的文档的集合
        System.out.println("结果"+tbItems.size());
        //4.获取结果集  返回
        map.put("rows",tbItems);
        return map;

    }

    /**
     * 商品的分类的列表
     * @param searchMap 根据关键字查询
     * @return
     */
    public List<String> searchCategoryList(Map searchMap){
        List<String> categroyList = new ArrayList<>();

        String keywords = (String)searchMap.get("keywords");
        Query query = new SimpleQuery("*:*");

        Criteria criteria = new Criteria("item_keywords");
        criteria.is(keywords);//item_keywords:手机
        query.addCriteria(criteria);

       //设置分组查询条件
        GroupOptions groupoptions = new GroupOptions();
        groupoptions.addGroupByField("item_category");//group by categry
        query.setGroupOptions(groupoptions);
        //分组查询
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            //获取分组的值
            System.out.println("分组的值"+tbItemGroupEntry.getGroupValue());
            categroyList.add(tbItemGroupEntry.getGroupValue());
        }
        return categroyList;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据商品的分类的名称查询缓存中的品牌和规格的列表
     * @param categoryName
     * @return
     */
    public Map searchBrandListAndSpecListByCategory(String categoryName){
        Map map  = new HashMap();
        Long  typeTempldateId = (Long) redisTemplate.boundHashOps("itemCat").get(categoryName);
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeTempldateId);
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeTempldateId);
        map.put("brandList",brandList);
        map.put("specList",specList);
        return map;

    }
}
