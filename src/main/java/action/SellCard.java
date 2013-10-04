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

public class SellCard extends AbstractAction {
	public static final Action Name = Action.SELL_CARD;
	
	private static final String URL_SELL_CARD = "http://web.million-arthurs.com/connect/app/trunk/sell?cyt=1";
	private byte[] response;
	
	public boolean run() throws Exception {
		if (process.info.toSell.isEmpty()) return false;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("serial_id", process.info.toSell));
		try {
			response = process.network.ConnectToServer(URL_SELL_CARD, post, false);
		} catch (Exception ex) {
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
			errorData.currentErrorType = ErrorType.SellCardDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("1010")) {
				errorData.currentErrorType = ErrorType.SellCardResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			} else {
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				process.info.toSell = "";
				return true;
			}
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.SellCardDataError;
			errorData.bytes = response;
			throw ex;
		}
		

	}
	
}
