package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet("/login")
public class Login extends HttpServlet {
    private static final long serialVersionUID = 1L;


    /**
     * @see HttpServlet#HttpServlet()
     */
    public Login() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	//因为是get，不需要通过body来获取info
    	
    		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			//写false，不会返回新的，有就返回，没有就返回null
			HttpSession session = request.getSession(false);
			
			JSONObject obj = new JSONObject();
			if(session != null) {
				String userId = session.getAttribute("user_id").toString();
				obj.put("status", "OK").put("user_id", userId).put("username", connection.getFullname(userId));
			} else {
				obj.put("status", "Invalid Session");
				response.setStatus(403);
			}
			//这里body里面写东西，更多的时候是为了帮助我们debug，真正的数据都是在head里
	           RpcHelper.writeJsonObject(response, obj);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connection.close();
		}
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    //对于一对response和request，永远指向同一个session，所以我们通过request绑定session，会自动指向response
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection connection = DBConnectionFactory.getConnection();
		try {
			JSONObject input = RpcHelper.readJSONObject(request);
			String userId = input.getString("user_id");
			String password = input.getString("password");
			
			JSONObject obj = new JSONObject();
			if(connection.verifyLogin(userId, password)) {
				//先检查request的head有没有session_id，如果没有，就帮我创建一个，这个session是存在Tomcat的服务器里的
				HttpSession session = request.getSession();
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600);
				obj.put("status", "OK").put("user_id", userId).put("username", connection.getFullname(userId));
			} else {
				obj.put("status", "User Doen't Exist");
				response.setStatus(401);
			}
			//这里body里面写东西，更多的时候是为了帮助我们debug，真正的数据都是在head里
	           RpcHelper.writeJsonObject(response, obj);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			connection.close();
		}
}

}
