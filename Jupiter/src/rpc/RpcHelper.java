package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

//这个相当于一个工具类，能比较方便我们的使用

public class RpcHelper {
	//这里如果内部进行try catch，我们也不知道该怎么去处理，最好就直接throw，让调用者去处理
	
	//Writes a JSONArray to http response.
	public static void writeJsonArray(HttpServletResponse response, JSONArray array) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*"); 
		//设置访问限制，这里设置*是个通配符，以后可以设置比如说只有某一个外部来的网站才能够访问
		//跨域问题，是不是我的前端发过来的，是针对服务器而不是针对method的
		PrintWriter out = response.getWriter();
		out.print(array);
		out.close();
	}
	
	//Writes a JSONObject to http response.
	public static void writeJsonObject(HttpServletResponse response, JSONObject obj) throws IOException {
		response.setContentType("application/json");
		response.setHeader("Access-Control-Allow-Origin", "*"); 
		//设置访问限制，这里设置*是个通配符，以后可以设置比如说只有某一个外部来的网站才能够访问
		//跨域问题，是不是我的前端发过来的
		PrintWriter out = response.getWriter();
		out.print(obj);
		out.close();
		
	}
	
	public static JSONObject readJSONObject(HttpServletRequest request) {
        StringBuilder sBuilder = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
          String line = null;
          while((line = reader.readLine()) != null) {
              sBuilder.append(line);
          }
          return new JSONObject(sBuilder.toString());
         
        } catch (Exception e) {
          e.printStackTrace();
        }
     
       return new JSONObject();
    }

}
