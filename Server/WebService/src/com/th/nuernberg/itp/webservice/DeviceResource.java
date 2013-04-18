package com.th.nuernberg.itp.webservice;

import com.th.nuernberg.itp.webservice.interfaces.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("device")
public class DeviceResource implements IWebServiceDevice {
	
	  @GET
	  @Path("echo/{text}") 
	  @Produces(MediaType.TEXT_PLAIN)
	  public String echo(@PathParam("text") String text) {
		  return "echo("+text+")";
	  }
	
	  @GET
	  @Path("register") 
	  @Produces(MediaType.TEXT_PLAIN)
	  public String register() {
		  return "register()";
	  }
	  
	  @GET
	  @Path("receive") 
	  @Produces(MediaType.TEXT_PLAIN)
	  public String receive() {
		  return "receive()";
	  }
	  
	  @GET
	  @Path("push") 
	  @Produces(MediaType.TEXT_PLAIN)
	  public String push() {
		  return "push()";
	  }
}