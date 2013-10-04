package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import action.ActionRegistry.Action;

public class GotoFloor extends AbstractAction{
	public static final Action Name = Action.GOTO_FLOOR;
	
	private static final String URL_GET_FLOOR = "http://web.million-arthurs.com/connect/app/exploration/get_floor?cyt=1";
	
	private byte[] response;
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", process.info.front.areaId));
		post.add(new BasicNameValuePair("check","1"));
		post.add(new BasicNameValuePair("floor_id",process.info.front.floorId));
		try {
			response = process.network.ConnectToServer(URL_GET_FLOOR, post, false);
		} catch (Exception ex) {
			//if (ex.getMessage().equals("302")) 
			// 上面的是为了截获里图跳转
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.ConnectionError;
			errorData.text = ex.getMessage();
			throw ex;
		}
		
		Document doc;
		try {
			doc = process.ParseXMLBytes(response);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.GotoFloorDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.GotoFloorResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			process.info.username = xpath.evaluate("//your_data/name", doc);
			process.info.lv = Integer.parseInt(xpath.evaluate("//town_level", doc));
			process.info.ap = Integer.parseInt(xpath.evaluate("//ap/current", doc));
			process.info.apMax = Integer.parseInt(xpath.evaluate("//ap/max", doc));
			process.info.bc = Integer.parseInt(xpath.evaluate("//bc/current", doc));
			process.info.bcMax = Integer.parseInt(xpath.evaluate("//bc/max", doc));
			process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
			
			process.info.SetTimeoutByAction(Name);
			
			process.info.exp = Integer.parseInt(xpath.evaluate("//get_floor/next_exp", doc));
			String spec = xpath.evaluate("//get_floor/special_item/before_count", doc);
			if (spec.length() != 0) {
				process.info.gather = Integer.parseInt(spec);
			} else {
				process.info.gather = -1;
			}
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.GotoFloorDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		
		return true;
	}
}
