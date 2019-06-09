package com.chen.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 返回数据
 * 
 * @author chenjinwei
 *
 * @date 2017年10月27日 下午9:59:27
 */
public class R extends HashMap<String, Object> {
	private static final long serialVersionUID = 1L;
	private Map<String,Object> result =new HashMap<>();
	
	public R() {
		put("code", 200);
		put("timestamp", System.currentTimeMillis());
	}


	public static  R error(int errorCode, String errorMsg){
		R r = new R();
		r.put("code",500);
		r.put("errorCode", errorCode);
		r.put("errorMsg", errorMsg);
		r.put("timestamp", System.currentTimeMillis());
		return r;
	}

	public static R ok(String msg) {
		R r = new R();
		r.putMap("msg", msg);
		return r;
	}
	
	public static R ok(Map<String, Object> map) {
		R r = new R();
		r.result.putAll(map);
		r.put("result",r.result);
		return r;
	}
	
	public static R ok() {
		return new R();
	}

	public R putMap(String key, Object value) {
		this.result.put(key,value);
		this.put("result",result);
		return this;
	}

}
