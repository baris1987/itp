package com.th.nuernberg.itp.webservice;

import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.th.nuernberg.itp.webservice.interfaces.*;

@Produces(MediaType.APPLICATION_JSON)
public abstract class BaseResource implements IWebService {
	
	  @GET
	  @Path("ping") 	
	  public String ping() {
		  Random rn = new Random();
		  return "{ping: "+(rn.nextInt()%10000)+"}";
	  }
}
