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

public class Use extends AbstractAction {
public static final Action Name = Action.USE;
	
	private static final String URL_USE = "http://web.million-arthurs.com/connect/app/item/use?cyt=1";
	private byte[] response;
	
	public boolean run() throws Exception {
		if (process.info.toUse.isEmpty()) return false;
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("item_id", process.info.toUse));
		try {
			response = process.network.ConnectToServer(URL_USE, post, false);
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
			errorData.currentErrorType = ErrorType.UseDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("1000")) {
				errorData.currentErrorType = ErrorType.UseResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			} else {
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				process.info.toUse = "";
				ParseUserDataInfo.parse(doc, process);
				return true;
			}
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.UseDataError;
			errorData.bytes = response;
			throw ex;
		}
		

	}
	
}
