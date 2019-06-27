package external;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicktMasterAPI {
	
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
    private static final String DEFAULT_KEYWORD = ""; // no restriction，用户可以不传，文档上写的没有restriction，任何一个都可以
    private static final String API_KEY = "wGd0O8o8tkzpr8zx5mFJxel5RF5LSVMY";
    //在http request的时候，空格也是个关键字，所以我们要避免有空格
    
    public List<Item> search(double lat, double lon, String keyword) {
    		if(keyword == null) {
    			keyword = DEFAULT_KEYWORD;
    		}
    		
    		try {
    			keyword = URLEncoder.encode(keyword, "UTF-8");
    			//为什么要encode，用户可能传入空格，所以我们需要把其转化为utf-8，转化为%20
    		} catch(UnsupportedEncodingException e) {
    			e.printStackTrace();
    		}
    		
    		//String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s", lat, lon, keyword, 50);
    		String query = String.format("apikey=%s&latlong=%s,%s&keyword=%s&radius=%s", API_KEY, lat, lon, keyword, 50);
    		//这里默认返回是20个，我们把size设为1，来做一个测试
    		//这里的顺序是无所谓的，只要保证这里的parameter是正确的，是能够被读得懂的就可以
    		//这里的&是来分割不同的parameter的，逗号是因为内部有两个参数
    		
    		String url = URL + "?" + query;
    		System.out.println("url = " + url);
    		//测试的时候也可以打印到当前的窗口上来
    		
    		try {
    			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    			connection.setRequestMethod("GET");
    			
    			int responseCode = connection.getResponseCode();
    			//内部会自动连接url，并且会将method发送过去。会call我们的tickemaster API
    			//一般来说这里会自动有一个timeout的，不会一直卡在这里
    			System.out.println("Response code:" + responseCode);
    			
    			if(responseCode != 200) {
    				return new ArrayList<>();
    			}
    			
    			//如果是200，证明是正确的
    			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    			//用BufferedReader的原因是因为如果放在内存的话，会比较慢，避免了一个个字符去读，一次读8k
    			//getInputStream()只会读取body里面的，读取head是用另外的方法
    			
    			String line;
    			StringBuilder response = new StringBuilder();
    			
    			while((line = reader.readLine()) != null) {
    				response.append(line);
    			}
    			
    			reader.close();
    			//要close，不然资源一直被hold
    			
    			JSONObject object = new JSONObject(response.toString());
    			//因为我们已经知道对方给我们返回的结果就是json所以可以这样写
    			
    			//检查json里面存不存在_embedded这么个key
    			if(!object.isNull("_embedded")) {
    				JSONObject embedded = object.getJSONObject("_embedded");
    				return getItemList(embedded.getJSONArray("events"));
    			}
    		} catch (Exception e) {
				// TODO: handle exception
    			e.printStackTrace();
		}
    		return new ArrayList<>();
    }
    
    private void queryAPI(double lat, double lon) {
        List<Item> events = search(lat, lon, null);
        
        for(Item event : events) {
        		System.out.println(event.toJSONObject());
        }
    }
    
    private List<Item> getItemList(JSONArray events) throws JSONException {
    		List<Item> itemList = new ArrayList<>();
    		for(int i = 0; i < events.length(); i++) {
    			JSONObject event = events.getJSONObject(i);
    			
    			ItemBuilder builder = new ItemBuilder();
    			if(!event.isNull("id")) {
    				builder.setItemId(event.getString("id"));
    			}
    			if (!event.isNull("name")) {
                 builder.setName(event.getString("name"));
             }
             if (!event.isNull("url")) {
                 builder.setUrl(event.getString("url"));
             }
             if (!event.isNull("distance")) {
                 builder.setDistance(event.getDouble("distance"));
             }
             
             builder.setAddress(getAddress(event))
             .setCategories(getCategories(event))
             .setImageUrl(getImageUrl(event));
             
             itemList.add(builder.build());
    		}
    		return itemList;
    }
    
    //下面三个helper function是为了得到几个嵌套比较深的element
    private String getAddress(JSONObject event) throws JSONException {
    		if(!event.isNull("_embedded")) {
    			JSONObject embedded = event.getJSONObject("_embedded");
    			if(!embedded.isNull("venues")) {
    				JSONArray venues = embedded.getJSONArray("venues");
    				for(int i = 0; i < venues.length(); i++) {
    					JSONObject venue = venues.getJSONObject(i);
    					StringBuilder builder = new StringBuilder();
    					if(!venue.isNull("address")) {
    						JSONObject address = venue.getJSONObject("address");
    						if(!address.isNull("line1")) {
    							builder.append(address.getString("line1"));
    						}
    						if(!address.isNull("line2")) {
    							builder.append(",");
    							builder.append(address.getString("line2"));
    						}
    						if(!address.isNull("line3")) {
    							builder.append(",");
    							builder.append(address.getString("line3"));
    						}
    					}
    					
    					if(!venue.isNull("city")) {
    						JSONObject city = venue.getJSONObject("city");
    						builder.append(",");
    						builder.append(city.getString("name"));
    					}
    					
    					String result = builder.toString();
    					if(!result.isEmpty()) {
    						return result;
    					}
    				}
    			}
    		}
    		
    		return "";
    }
    
    private String getImageUrl(JSONObject event) throws JSONException {
    		if(!event.isNull("images")) {
    			JSONArray array = event.getJSONArray("images");
    			for(int i = 0; i < array.length(); i++) {
    				JSONObject image = array.getJSONObject(i);
    				if(!image.isNull("url")) {
    					return image.getString("url");
    				}
    			}
    		}
    		return "";
    }
    
    //用set的原因是我们担心会有重复的segment返回过来，一般来说我们用list也可以
    private Set<String> getCategories(JSONObject event) throws JSONException {
    		Set<String> categories = new HashSet<>();
    		if(!event.isNull("classifications")) {
    			JSONArray classifications = event.getJSONArray("classifications");
    			for(int i = 0; i < classifications.length(); i++) {
    				JSONObject classification = classifications.getJSONObject(i);
    				if(!classification.isNull("segment")) {
    					JSONObject segment = classification.getJSONObject("segment");
    					if(!segment.isNull("name")) {
    						categories.add(segment.getString("name"));
    					}
    				}
    			}
    		}
    		return categories;
    }


	public static void main(String[] args) {
		TicktMasterAPI api = new TicktMasterAPI();
		api.queryAPI(29.682684, -95.295410);

	}


}
