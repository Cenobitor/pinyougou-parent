package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author 三国的包子
 * @version 1.0
 * @description 描述
 * @title 标题
 * @package com.pinyougou.sellergoods.service.impl
 * @company www.itheima.com
 */
@Service//使用dubbo的注解
public class BrandServiceImpl implements BrandService {
    @Autowired
    private TbBrandMapper brandMapper;
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //设置分页的条件
        PageHelper.startPage(pageNum,pageSize);
        //紧跟着的第一个查询才会被分页
        List<TbBrand> tbBrands = brandMapper.selectByExample(null);
        Page<TbBrand> page = (Page<TbBrand>)tbBrands;
        return new PageResult(page.getTotal(),page.getResult());//ctr+shift +backspace
    }

    @Override
    public void add(TbBrand brand) {
        brandMapper.insert(brand);
    }

    @Override
    public TbBrand findOne(Long id) {
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(id);
        return tbBrand;
    }

    @Override
    public void update(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    @Override
    public void delete(Long[] ids) {

//        TbBrandExample exmaple = new TbBrandExample();
//        exmaple.createCriteria().andIdIn()
//        brandMapper.deleteByExample(exmaple);//delete from tbran where id in (1,21)......

        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findPage(TbBrand brand, Integer pageNum, Integer pageSize) {
        //设置分页的条件
        PageHelper.startPage(pageNum,pageSize);
        //紧跟着的第一个查询才会被分页
        TbBrandExample example = new TbBrandExample();
        TbBrandExample.Criteria criteria = example.createCriteria();

        if(brand!=null){
            //如果不等于空个添加查询的条件
            if(brand.getName()!=null && brand.getName().length()>0) {
                criteria.andNameLike("%" + brand.getName() + "%");
            }
           if(brand.getFirstChar()!=null && brand.getFirstChar().length()>0){
               criteria.andFirstCharEqualTo(brand.getFirstChar());
           }
        }
        List<TbBrand> tbBrands = brandMapper.selectByExample(example);
        Page<TbBrand> page = (Page<TbBrand>)tbBrands;
        return new PageResult(page.getTotal(),page.getResult());//ctr+shift +backspace

    }
}
