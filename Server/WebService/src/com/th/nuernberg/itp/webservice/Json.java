package com.th.nuernberg.itp.webservice;

import com.google.gson.Gson;
import com.th.nuernberg.itp.webservice.interfaces.IJson;

public class Json implements IJson {

	public String build(Object o) {
		Gson gson = new Gson();
		return gson.toJson(o);
	}

}
