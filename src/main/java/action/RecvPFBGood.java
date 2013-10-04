package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import action.ActionRegistry.Action;

public class RecvPFBGood extends AbstractAction{
	public static final Action Name = Action.RECV_PFB_GOOD;
	private byte[] response;
	//private static final String URL_FAIRY_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	private static final String URL_PRIVATE_BATTLE_TOP = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_top?cyt=1";
	public boolean run() throws Exception {
		Document doc;
			try {
				ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
				al.add(new BasicNameValuePair("serial_id", process.info.fairy.SerialId));
				al.add(new BasicNameValuePair("user_id", process.info.fairy.UserId));
				response = process.network.ConnectToServer(URL_PRIVATE_BATTLE_TOP,
						al, false);
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
				errorData.currentErrorType = ErrorType.RecvPFBGoodDataError;
				errorData.bytes = response;
				throw ex;
			}

			try {
				return parse(doc);
			} catch (Exception ex) {
				throw ex;
			}
	}
	
	private boolean parse(Document doc) throws Exception {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.RecvPFBGoodResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			String add, msg;
			if ((Boolean)xpath.evaluate("count(/response/body/private_fairy_top/recover_by_like) > 0", doc, XPathConstants.BOOLEAN)) {
				
				msg = xpath.evaluate("/response/body/private_fairy_top/recover_by_like/message", doc);
				add = xpath.evaluate("/response/body/private_fairy_top/recover_by_like/recover_point", doc);

				errorData.currentErrorType = ErrorType.RecvPFBGoodResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = String.format("%s\n收赞回复%s点BC...", msg, add);
			}
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.RecvPFBGoodDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		return true;
	}
}
