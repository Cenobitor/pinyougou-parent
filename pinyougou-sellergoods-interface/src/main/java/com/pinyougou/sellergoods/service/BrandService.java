package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.sellergoods.service
 * @company www.itheima.com
 */
public interface BrandService {

    public List<TbBrand> findAll();

    public PageResult findPage(Integer pageNum ,Integer pageSize);

    /**
     * 插入数据
     * @param brand
     */
    public void add(TbBrand brand);

    public TbBrand findOne(Long id);

    /**
     * brand是更新后的数据 一定要带有Id
     * @param brand
     */
    public void update(TbBrand brand);

    public void delete(Long[] ids);

    /**
     *
     * @param brand 页面传递过来的额查询的条件
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageResult findPage(TbBrand brand,Integer pageNum ,Integer pageSize);

}
