package com.atguigu.gmall.passpost;

import com.atguigu.gmall.passpost.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
	}


	@Test
	public void test01(){
		String key = "atguigu";
		String ip = "192.168.182.132";
		Map map = new HashMap();
		map.put("userId",1001);
		map.put("nickName","marry");
		String token = JwtUtil.encode(key,map,ip);
		System.err.println("token : "+token);

		Map <String, Object> decode = JwtUtil.decode(token, key, "192.168.182.133");
		System.out.println("decode : " + decode);


	}



}
