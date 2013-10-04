package action;

import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import action.ActionRegistry.Action;

public class GetFairyReward extends AbstractAction{
	public static final Action Name = Action.GET_FAIRY_REWARD;
	
	private static final String URL_GET_FAIRY_REWARD = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_rewards?cyt=1";
	private byte[] response;
	
	public boolean run() throws Exception {
		Document doc;
		try {
			response = process.network.ConnectToServer(URL_GET_FAIRY_REWARD, new ArrayList<NameValuePair>(), false);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.ConnectionError;
			errorData.text = ex.getMessage();
			throw ex;
		}
		
		try {
			doc = process.ParseXMLBytes(response);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.GetFairyRewardDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals("1010")) {
				errorData.currentErrorType = ErrorType.GetFairyRewardResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			} else {
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return true;
			}
			
		} catch (Exception ex) {
			if (errorData.currentErrorType == ErrorType.none) {
				throw ex;
			}
		}
		
		return false;
	}
	
}
