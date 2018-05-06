package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        //1.先获取从页面传递过来的参数的值   通过KEY获取
        String keywords = (String)searchMap.get("keywords");//获取主查询的条件

        //2.创建查询的对象    设置查询的条件  主查询条件
        Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords");
        criteria.is(keywords);//item_keywords:手机
        query.addCriteria(criteria);

        //价格过滤  高亮  分页  。。。。。。。。规格

        //3.执行查询
        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        List<TbItem> tbItems = page.getContent();//获取查询的结果
        System.out.println("结果"+tbItems.size());
        //4.获取结果集  返回
        resultMap.put("rows",tbItems);


        return resultMap;
    }
}
