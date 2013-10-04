package action;

import info.Area;
import info.Floor;

import java.util.ArrayList;
//import java.util.Hashtable;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import action.ActionRegistry.Action;

public class GetFloorInfo extends AbstractAction {
	public static final Action Name = Action.GET_FLOOR_INFO;
	
	private static final String URL_AREA = "http://web.million-arthurs.com/connect/app/exploration/area?cyt=1";
	private static final String URL_FLOOR = "http://web.million-arthurs.com/connect/app/exploration/floor?cyt=1";
	
	
	private byte[] response;
	
	public boolean run() throws Exception {
		response = null;
		Document doc;
		try {
			response = process.network.ConnectToServer(URL_AREA, new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.ConnectionError;
			errorData.text = ex.getMessage();
			throw ex;
		}
		
		try {
			doc = process.ParseXMLBytes(response);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.AreaDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.AreaResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			int areaCount = ((NodeList)xpath.evaluate("//area_info_list/area_info", doc, XPathConstants.NODESET)).getLength();
			if (areaCount > 0) {
				//info.area = new Hashtable<Integer,Area>();
				process.info.area.clear();
				process.info.floor.clear();
			}
			for (int i = 1; i <= areaCount; i++){
				Area a = new Area();
				String p = String.format("//area_info_list/area_info[%d]/",i);
				a.areaId = Integer.parseInt(xpath.evaluate(p+"id", doc));
				a.areaName = xpath.evaluate(p+"name", doc);
				a.exploreProgress = Integer.parseInt(xpath.evaluate(p+"prog_area", doc));
				if (a.areaId > 100000) process.info.area.put(a.areaId, a);
			}
			process.info.AllClear = true;
			process.info.front = null;
			for (int i : process.info.area.keySet()) {
				getFloor(process.info.area.get(i));
			} // end of area iterator
			if (process.info.front == null) process.info.front = process.info.floor.get(1);
			process.info.SetTimeoutByAction(Name);
			
		} catch (Exception ex) {
			if (errorData.currentErrorType == ErrorType.none) {
				throw ex;
			}
		}
		
		return true;
	}
	
	public void getFloor(Area a) throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", String.valueOf(a.areaId)));
		try {
			response = process.network.ConnectToServer(URL_FLOOR, post, false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.ConnectionError;
			errorData.text = ex.getLocalizedMessage();
			throw ex;
		}
		Document doc;
		try {
			doc = process.ParseXMLBytes(response);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.AreaDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		int floorCount = ((NodeList)xpath.evaluate("//floor_info_list/floor_info", doc, XPathConstants.NODESET)).getLength();
		String aid = xpath.evaluate("//exploration_floor/area_id", doc);
		
		for (int j = floorCount; j > 0; j--) {
			Floor f = new Floor();
			String p = String.format("//floor_info_list/floor_info[%d]/", j);
			f.areaId = aid;
			f.floorId = xpath.evaluate(p+"id", doc);
			f.cost = Integer.parseInt(xpath.evaluate(p+"cost", doc));
			f.progress = Integer.parseInt(xpath.evaluate(p+"progress", doc));
			f.type = xpath.evaluate(p+"type", doc);
			if (f.cost < 1) continue; //跳过秘境守护者
			if (process.info.floor.containsKey(f.cost)) {
				if(Integer.parseInt(process.info.floor.get(f.cost).areaId) > Integer.parseInt(f.areaId)) {
					continue;
				}
			}
			process.info.floor.put(f.cost, f);
			if (f.progress != 100 && a.exploreProgress != 100 && process.info.AllClear) {
				process.info.front = f;
				process.info.AllClear = false;
			}
		}
	}
	
	
	
}
