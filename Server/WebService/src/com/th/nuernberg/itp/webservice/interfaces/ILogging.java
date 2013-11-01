package com.th.nuernberg.itp.webservice.interfaces;

public interface ILogging {
	void write(String type, Object... messages);
	void enable(boolean enabled);
}
