package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TYPE_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryShopList() {
        // 1. 从redis中查询商铺类型列表
        List<String> shopTypes = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPE_KEY, 0, 9);
        // 2. 命中，返回商铺类型信息
        List<ShopType> shopTypesByRedis = new ArrayList<>();
        if (shopTypes.size() != 0) {
            for (String shopType : shopTypes) {
                ShopType type = JSONUtil.toBean(shopType, ShopType.class);
                shopTypesByRedis.add(type);
            }
            return Result.ok(shopTypesByRedis);
        }
        // 3. 未命中，从数据库中查询商铺类型,并根据sort排序
        List<ShopType> shopTypesByMysql = query().orderByAsc("sort").list();
        // 4. 将商铺类型存入到redis中
        for (ShopType shopType : shopTypesByMysql) {
            String s = JSONUtil.toJsonStr(shopType);
            stringRedisTemplate.opsForList().rightPushAll(CACHE_SHOP_TYPE_KEY,s);
        }
        // 5. 返回商铺类型信息
        return Result.ok(shopTypesByMysql);
    }
}
