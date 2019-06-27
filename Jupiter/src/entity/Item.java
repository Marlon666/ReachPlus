package entity;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
	private String itemId;
    private String name;
    private double rating;
    private String address;
    private Set<String> categories;
    private String imageUrl;
    private String url;
    private double distance;
    
    //不用普通的构造函数的原因，是因为万一以后要加，所有的依赖都要进行修改，所以在这里变量比较多的时候，我们需要用builder pattern
    //这样的话，以后如果我们想要加一个新的element，只需要在builder里加一个setter就好了，改动量就会比较少
    private Item(ItemBuilder builder) {
    		this.itemId = builder.itemId;
        this.name = builder.name;
        this.rating = builder.rating;
        this.address = builder.address;
        this.categories = builder.categories;
        this.imageUrl = builder.imageUrl;
        this.url = builder.url;
        this.distance = builder.distance;
    }
    
	public String getItemId() {
		return itemId;
	}
	public String getName() {
		return name;
	}
	public double getRating() {
		return rating;
	}
	public String getAddress() {
		return address;
	}
	public Set<String> getCategories() {
		return categories;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public String getUrl() {
		return url;
	}
	public double getDistance() {
		return distance;
	}
	
	//需要写一个方法，把我们的这些field通过JSON的形式返回
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		try {
			object.put("item_id", itemId);
			//这些名字要跟前端规定好
			object.put("name", name);
			object.put("rating", rating);
			object.put("address", address);
			object.put("categories", new JSONArray(categories));
			object.put("image_url", imageUrl);
			object.put("url", url);
			object.put("distance", distance);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public static class ItemBuilder {
		private String itemId;
	    private String name;
	    private double rating;
	    private String address;
	    private Set<String> categories;
	    private String imageUrl;
	    private String url;
	    private double distance;
		public void setItemId(String itemId) {
			this.itemId = itemId;
		}
		public ItemBuilder setName(String name) {
			this.name = name;
			return this;
		}
		public ItemBuilder setRating(double rating) {
			this.rating = rating;
			return this;
		}
		public ItemBuilder setAddress(String address) {
			this.address = address;
			return this;
		}
		public ItemBuilder setCategories(Set<String> categories) {
			this.categories = categories;
			return this;
		}
		public ItemBuilder setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
			return this;
		}
		public ItemBuilder setUrl(String url) {
			this.url = url;
			return this;
		}
		public ItemBuilder setDistance(double distance) {
			this.distance = distance;
			return this;
		}
	    
		public Item build() {
			return new Item(this);
		}
	    
	}

}
