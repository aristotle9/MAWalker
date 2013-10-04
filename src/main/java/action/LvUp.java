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

public class LvUp extends AbstractAction {
	public static final Action Name = Action.LV_UP;
	
	private static final String URL_POINT_SETTING = "http://web.million-arthurs.com/connect/app/town/pointsetting?cyt=1";

	
	private byte[] response;
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("ap", String.valueOf(process.info.apUp)));
		post.add(new BasicNameValuePair("bc", String.valueOf(process.info.bcUp)));
		try {
			response = process.network.ConnectToServer(URL_POINT_SETTING, post, false);
		} catch (Exception ex) {
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
			errorData.currentErrorType = ErrorType.LvUpDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			String code = xpath.evaluate("/response/header/error/code", doc);
			if (!code.equals("0")) {
				errorData.currentErrorType = ErrorType.LvUpResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			ParseUserDataInfo.parse(doc, process);
			
			process.info.SetTimeoutByAction(Name);
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.LvUpDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		return true;
	}
	
}
