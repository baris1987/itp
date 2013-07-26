package com.th.nuernberg.itp.webservice.interfaces;

public interface IPersistence {
	public void setPersister(IDatabase persister);
	public boolean persist();
}
