package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicktMasterAPI;

public class MySQLConnection implements DBConnection {

    private Connection conn;
    
   public MySQLConnection() {
       try {
           Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
           conn = DriverManager.getConnection(MySQLDBUtil.URL);
          
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
   
   //使用完成过后关闭连接
   @Override
   public void close() {
       if (conn != null) {
           try {
               conn.close();
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   }


	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		try {
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			for(String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> itemIds = new HashSet<>();
		String sql = "SELECT * FROM history WHERE user_id = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
                String itemId = rs.getString("item_id");
                itemIds.add(itemId);
            }

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return itemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if(conn == null) {
			return new HashSet<>();
		}
		
		Set<Item> items = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
            String sql = "SELECT * FROM items WHERE item_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            for (String itemId : itemIds) {
                stmt.setString(1, itemId);
                
                ResultSet rs = stmt.executeQuery();
                
                ItemBuilder builder = new ItemBuilder();
                
                while (rs.next()) {
                    builder.setItemId(rs.getString("item_id"));
                    builder.setName(rs.getString("name"));
                    builder.setAddress(rs.getString("address"));
                    builder.setImageUrl(rs.getString("image_url"));
                    builder.setUrl(rs.getString("url"));
                    builder.setCategories(getCategories(itemId));
                    builder.setDistance(rs.getDouble("distance"));
                    builder.setRating(rs.getDouble("rating"));
                    
                    items.add(builder.build());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return items;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
            return null;
        }
        Set<String> categories = new HashSet<>();
        try {
            String sql = "SELECT category from categories WHERE item_id = ? ";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, itemId);
            // why not using execute: 只能返回true或者false，成功或者失败，对于select来说不适合
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String category = rs.getString("category");
                categories.add(category);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		// TODO Auto-generated method stub
		
		TicktMasterAPI ticketMasterAPI = new TicktMasterAPI();
        List<Item> items = ticketMasterAPI.search(lat, lon, term);

        for(Item item : items) {
        		saveItem(item);
        }

        return items;
	}

	@Override
	public void saveItem(Item item) {
		// TODO Auto-generated method stub
		//IGNORE: insert multiple row at the same time, without ignore, one row's error will drop all the efforts of other insertion
        try {
        	String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
	        ps.setDouble(3, item.getRating());
	        ps.setString(4, item.getAddress());
	        ps.setString(5, item.getImageUrl());
	        ps.setString(6, item.getUrl());
	        ps.setDouble(7, item.getDistance());
	        ps.execute();
	        
	        sql = "INSERT IGNORE INTO categories VALUES(?, ?)";
	        ps = conn.prepareStatement(sql);
	        ps.setString(1, item.getItemId());
	        for(String category : item.getCategories()) {
	            ps.setString(2, category);
	            ps.execute();
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
            return "";
        }        
        String name = "";
        try {
            String sql = "SELECT first_name, last_name FROM users WHERE user_id = ? ";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                String lastname = rs.getString("last_name");
                String firstname = rs.getString("first_name");
                name = firstname + lastname;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
            return false;
        }
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			return statement.execute();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

        return false;
	}

	@Override
	public boolean registerUser(String userId, String password, String firstname, String lastname) {
		if (conn == null) {
            System.err.println("DB connection failed");
            return false;
        }

        try {
            String sql = "INSERT IGNORE INTO users VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userId);
            ps.setString(2, password);
            ps.setString(3, firstname);
            ps.setString(4, lastname);
            
            return ps.executeUpdate() == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
	}

}
