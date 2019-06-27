package akuna;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.*;

public class Order {
	
	static class event {
		Long owned;
		Long needed;
		int orderStatus; // 0 -> 1 acknowledge -> 2 reject
		int cancelStatus; // 0 -> 1 acknowledge -> 2 reject -> 3 ask
		String Symbol;
		
		
		public event(Long owned, Long needed, int orderStatus, int cancelStatus, String Symbol) {
			this.owned = owned;
			this.needed = needed;
			this.orderStatus = orderStatus;
			this.cancelStatus = cancelStatus;
			this.Symbol = Symbol;
		}
	}
	
	public static class OrderMarkingEngine {
		Map<Long, event> events;
		String curSymbol;
		
		final private String[] types = {"NEW", "ORDER_ACK", "ORDER_REJ", "CANCEL", "CANCEL_ACK", "CANCEL_REJ", "FILL"}; 
		final private String[] sides = {"BUY", "SELL"};
		
		//if possible, a singleton will be better here
		public OrderMarkingEngine() {
			events = new HashMap<>();
			curSymbol = "";
		}
		
		public int handleEvent(String jsonEvent) throws JSONException {
			JSONObject obj = new JSONObject(jsonEvent);
			
			String type = (String) obj.get("type");
			Long ID = (Long) obj.get("order_id");
			
			if(type.equals(types[0])) {
				this.curSymbol = (String) obj.get("symbol");
				Ordering(ID, obj);
			} else if(type.equals(types[1])) {
				//order acknowledge
				events.get(ID).orderStatus = 1;
			} else if(type.equals(types[2])) {
				//order reject
				event temp = events.get(ID);
				temp.owned = (long) 0;
				temp.needed = (long) 0;
				temp.orderStatus = 2;
				temp.cancelStatus = 0;
				
				events.put(ID, temp);
			} else if(type.equals(types[3])) {
				//cancel 
				events.get(ID).cancelStatus = 3;
			} else if(type.equals(types[4])) {
				//cancel acknowledge
				event temp = events.get(ID);
				if(temp.needed < 0) {
					temp.owned += Math.abs(temp.owned);
					temp.needed = (long)0;
				} else {
					temp.needed = (long)0;
				}
			} else if(type.equals(types[4])) {
				//cancel reject
				events.get(ID).cancelStatus = 2;
			} else if(type.equals(types[5])) {
				//Fill
				fill(ID, obj);
			}
			
			String symbol = events.get(ID).Symbol;
			int result = 0;
			for(Map.Entry<Long, event> eventNode : events.entrySet()) {
				if(eventNode.getValue().Symbol.equals(symbol)) {
					result += eventNode.getValue().owned;
				}
			}
			
			return result;
		}
		
		private void Ordering(Long ID, JSONObject obj) throws JSONException {
			Long quantity = (Long) obj.get("quantity");
			String side = (String) obj.get("side");
			
			if(side.equals(sides[0])) {
				events.put(ID, new event((long)0, quantity, 0, 0, this.curSymbol));
			} else if(side.equals(sides[1])) {
				//mark as short, do it immediately
				events.put(ID, new event(-quantity, -quantity, 0, 0, this.curSymbol));
			}
		}
		
		private void fill(Long ID, JSONObject obj) throws JSONException {
			Long filled = (Long) obj.get("filled_quantity");
			Long remaining = (Long) obj.get("remaining_quantity");
			if(filled + remaining != Math.abs(this.events.get(ID).needed)) {
				System.out.println("Error");
			} else {
				event temp = events.get(ID);
				if(temp.needed < (long) 0) {
					temp.needed += filled;
				} else {
					temp.needed -= filled;
					temp.owned += filled;
				}
			}
		}
	}
}
