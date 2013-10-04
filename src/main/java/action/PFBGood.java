package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import walker.Go;
import action.ActionRegistry.Action;

public class PFBGood extends AbstractAction{
	public static final Action Name = Action.PFB_GOOD;

	private static final String URL_PFB_GOOD = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_battle_good?cyt=1";
	private static final String URL_FAIRY_HISTORY = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_history?cyt=1";
	private byte[] response;
	private String serial_Id;

	public boolean run() throws Exception {
		Document doc;
		Boolean set = false;
		//for (info.PFBGood pg : info.PFBGoodList) {
		while (!process.info.PFBGoodList.empty()) { 
			info.PFBGood pg = process.info.PFBGoodList.pop();
			try {
				serial_Id = pg.serialId;
				ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
				al.add(new BasicNameValuePair("serial_id", pg.serialId));
				al.add(new BasicNameValuePair("user_id", pg.userId));
				response = process.network.ConnectToServer(URL_FAIRY_HISTORY,
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
				errorData.currentErrorType = ErrorType.FairyHistoryDataError;
				errorData.bytes = response;
				throw ex;
			}

			try {
				set = parse(doc);
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) {
					throw ex;
				}
			}
		}

		return set;
	}

	private boolean parse(Document doc) throws Exception {

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.FairyHistoryResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			}

			NodeList fairy = (NodeList) xpath.evaluate(
					"//fairy_history/attacker_history/attacker", doc,
					XPathConstants.NODESET);
			String user_id = "";
			for (int i = 0; i < fairy.getLength(); i++) {
				Node f = fairy.item(i).getFirstChild();
				do {
					if (f.getNodeName().equals("user_id")) {
						String str = f.getFirstChild().getNodeValue();
						if (!str.equals(process.info.userId))
							user_id += f.getFirstChild().getNodeValue() + ",";
					}
					f = f.getNextSibling();
				} while (f != null);
			}
			user_id = user_id.substring(0, user_id.length() - 1);
			try {
				if (run2(serial_Id, user_id)) {
					Go.log(errorData.text);
					errorData.clear();
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none)
					throw ex;
			}
			process.info.SetTimeoutByAction(Name);

		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none)
				throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.FairyHistoryDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		return true;
	}

	public boolean run2(String serialId, String userId) throws Exception {
		Document doc;
		try {
			ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
			al.add(new BasicNameValuePair("dialog", "1"));
			al.add(new BasicNameValuePair("serial_id", serialId));
			al.add(new BasicNameValuePair("user_id", userId));
			response = process.network.ConnectToServer(URL_PFB_GOOD, al, false);
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
			errorData.currentErrorType = ErrorType.PFB_GoodDataError;
			errorData.bytes = response;
			throw ex;
		}

		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals(
					"1010")) {
				errorData.currentErrorType = ErrorType.PFB_GoodResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
				return false;
			} else {
				errorData.text = xpath.evaluate(
						"/response/header/error/message", doc);
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
