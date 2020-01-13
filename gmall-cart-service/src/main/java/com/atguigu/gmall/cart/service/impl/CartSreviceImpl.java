package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CartSreviceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Reference
    private ManageService manageService;


    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
    /*
        1.  先查看数据库中是否有该商品
            select * from cartInfo where userId = ? and skuId = ?
            true: 数量相加upd
            false: 直接添加
        2.  放入redis！
     */
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();

        // 定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        // 调用查询数据库并加入缓存
//        if(!jedis.exists(cartKey)){
//            loadCartCache(userId);
//        }

        // 真正添加数据库！
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectByExample(example);
        CartInfo cartInfoExist = null;
        if (cartInfoList!=null && cartInfoList.size()>0){
            cartInfoExist=cartInfoList.get(0);
        }

        // 说明该商品已经在数据库中存在！
        if (cartInfoExist!=null){
            // 数量更新
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            // 初始化实时价格
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            // 更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            // 更新redis！ cartInfoExist

        }else {
            // 商品在数据库中不存在！
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();

            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            // 添加到数据库！
            cartInfoMapper.insertSelective(cartInfo1);
            // 更新redis！ cartInfo1
            cartInfoExist = cartInfo1;
        }
        String cartInfoJson = JSON.toJSONString(cartInfoExist);
        // 更新redis 放在最后！
        jedis.hset(cartKey,skuId, cartInfoJson);
        setCartkeyExpireTime(userId, jedis, cartKey);

        // 关闭redis！
        jedis.close();
    }

    @Override
    public List <CartInfo> getCartList(String userId) {
         /*
            1.  获取redis中的购物车数据
            2.  如果redis 没有，从mysql 获取并放入缓存
         */
        List <CartInfo> cartInfolist = new ArrayList <>();
        Jedis jedis = redisUtil.getJedis();

        //定义key:user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        List <String> stringList = jedis.hvals(cartKey);
        if (stringList!=null && stringList.size()>0){
            for (String cartJson : stringList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfolist.add(cartInfo);
            }
            cartInfolist.sort(new Comparator <CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    return o1.getId().compareTo(o2.getId());
                }
            });

            return  cartInfolist;
        }else {
            //走db  放入redis
            cartInfolist = loadCartCache(userId);
            return cartInfolist;
        }
    }

    //根据userId查询登录购物车数据
    @Override
    public List <CartInfo> mergeToCartList(List <CartInfo> cartInfoArrayList, String userId) {
        //获取到 登录时购物车数据
        List <CartInfo> cartInfoListLogin = cartInfoMapper.selectCartListWithCurPrice(userId);
        //判断登录时购物车数据是否为空？
        if (cartInfoListLogin!=null && cartInfoListLogin.size()>0){
            for (CartInfo cartInfoNoLogin : cartInfoArrayList) {
                //声明一个boolean 类型变量
                boolean isMatch = false;
                //如果说数据库中一天数据都没有
                for (CartInfo cartInfoLogin : cartInfoListLogin) {
                    if (cartInfoNoLogin.getSkuId().equals(cartInfoLogin.getSkuId())){
                        //数量相加
                        cartInfoLogin.setSkuNum(cartInfoLogin.getSkuNum()+cartInfoNoLogin.getSkuNum());
                        //更新数据库
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoLogin);
                        isMatch = true;
                    }
                }
                //表示登录的购物车数据与未登录购物车数据没有匹配上
                if (!isMatch){
                    //直接添加到数据库
                    cartInfoNoLogin.setId(null);
                    cartInfoNoLogin.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoNoLogin);
                }
            }
        }else {
            //数据库为空  直接添加到数据库
            for (CartInfo cartInfo : cartInfoArrayList) {
                cartInfo.setId(null);
                cartInfo.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfo);
            }
        }
        //汇总数据
        List <CartInfo> cartInfoList = loadCartCache(userId);

        //从新在数据中查询并返回数据
        //cartInfoList 数据中数量合并之后的集合
        //合并: 选中状态为1
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfo : cartInfoArrayList) {
                //skuId 相同
                if (cartInfoDB.getSkuId().equals(cartInfo.getSkuId())){
                    //合并未登录选中的数据
                    //如果数据库中为1，未登录中也为1 不用修改
                    if ("1".equals(cartInfo.getIsChecked())){
                        if (!"1".equals(cartInfoDB.getIsChecked())){
                            //修改数据字段为1
                            cartInfoDB.setIsChecked("1");
                            //修改商品状态为被选中
                            checkCart(cartInfo.getIsChecked(),cartInfo.getSkuId(),userId);
                        }
                    }
                }
            }
        }

        return cartInfoList;
    }

    //删除未登录购物车
    @Override
    public void deleteCartList(String userTempId) {
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userTempId);
        cartInfoMapper.deleteByExample(example);

        //删除缓存
        //获取jedis
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userTempId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);

        jedis.close();

    }

    //修改状态
    @Override
    public void checkCart(String isChecked, String skuId, String userId) {
        //1.修改缓存
        //2.修改数据库
        // 第一种方案：直接修改缓存
//      可以使用
//        Jedis jedis = redisUtil.getJedis();
//        // 定义key
//        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
//        // 获取缓存中的对应商品
//        String cartJson = jedis.hget(cartKey, skuId);
//        // cartJson 转换为对象
//        CartInfo cartInfoJson = JSON.parseObject(cartJson, CartInfo.class);
//        cartInfoJson.setIsChecked(isChecked);
//
//        // 将修改之后的数据放入缓存
//        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfoJson));

        // 获取缓存中的商品

        // 修改数据 update cartInfo set is_checked = ? where userId = ? and skuId = ?
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId",userId).andEqualTo("skuId",skuId);
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(isChecked);
        System.out.println("修改数据——————");
        cartInfoMapper.updateByExampleSelective(cartInfo,example);
        //第二种：按照缓存管理的原则：避免出现脏数据，先删除缓存，再放入缓存
        //删除redis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //删除数据
        jedis.hdel(cartKey,skuId);
        //放入缓存
        //select * fron cartInfo where = ? and skuId = ?
        List <CartInfo> cartInfoList = cartInfoMapper.selectByExample(example);
        //获取集合数据第一条数据
        if (cartInfoList!=null && cartInfoList.size()>0){
            CartInfo cartInfoQuery = cartInfoList.get(0);
            //数据初始化时价格
            cartInfoQuery.setSkuPrice(cartInfoQuery.getCartPrice());
            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfoQuery));
        }
        jedis.close();
    }

    //查询购物车选中列表
    @Override
    public List <CartInfo> getCartCheckedList(String userId) {
        List <CartInfo> cartInfoList = new ArrayList <>();
        //获取redis
        Jedis jedis = redisUtil.getJedis();
        //定义key
        String CartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        //获取数据
        List <String> cartList = jedis.hvals(CartKey);
        if (cartList!=null && cartList.size()>0){
            //循环遍历
            for (String cartJson : cartList) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                if ("1".equals(cartInfo.getIsChecked())){
                    cartInfoList.add(cartInfo);
                }
            }
        }
        jedis.close();

        return cartInfoList;
    }

    //获取数据库中的数据并放入缓存
    @Override
    public List<CartInfo> loadCartCache(String userId) {
        // 使用实时价格：将skuInfo.price 价格赋值 cartInfo.skuPrice
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null || cartInfoList.size()==0){
            return  null;
        }
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义key：user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        HashMap<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        jedis.hmset(cartKey,map);
        setCartkeyExpireTime(userId, jedis, cartKey);

        jedis.close();

        return cartInfoList;
    }

    //设置过期时间
    private void setCartkeyExpireTime(String userId, Jedis jedis, String cartKey) {
        //根据user得过期时间设置
        //获取用户的过期时间user:userId:info
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
        //用户key 存在 ，登录
        Long expireTime = null;
        if (jedis.exists(userKey)){
            //获取过期时间
            expireTime = jedis.ttl(userKey);
            //给购物车的key设置
            jedis.expire(cartKey,expireTime.intValue());
        }else {
            //给购物车的可以设置
            jedis.expire(cartKey,7*24*3600);
        }
    }


}
