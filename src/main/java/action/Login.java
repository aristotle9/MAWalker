package action;


import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import walker.Info;

public class Login extends AbstractAction {
	public static final ActionRegistry.Action Name = ActionRegistry.Action.LOGIN;
	// URLs
	private static final String URL_CHECK_INSPECTION = "http://web.million-arthurs.com/connect/app/check_inspection?cyt=1";
	private static final String URL_LOGIN = "http://web.million-arthurs.com/connect/app/login?cyt=1";
	// error type
	public static final String ERR_CHECK_INSPECTION = "Login/check_inspection";
	public static final String ERR_LOGIN = "Login/login";
	
	private byte[] result;
	
	public boolean run() throws Exception {
		try {
			return run(true);
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	public boolean run(boolean jump) throws Exception {
		Document doc;
		if (!jump) {
			try {
				result = process.network.ConnectToServer(URL_CHECK_INSPECTION, new ArrayList<NameValuePair>(), true);
			} catch (Exception ex) {
				errorData.currentDataType = DataType.text;
				errorData.currentErrorType = ErrorType.ConnectionError;
				errorData.text = ERR_CHECK_INSPECTION;
				throw ex;
			}
		}
		ArrayList<NameValuePair> al = new ArrayList<NameValuePair>();
		al.add(new BasicNameValuePair("login_id",process.info.LoginId));
		al.add(new BasicNameValuePair("password",process.info.LoginPw));
		try {
			result = process.network.ConnectToServer(URL_LOGIN, al, true);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.ConnectionError;
			errorData.text = ex.getMessage();
			ex.printStackTrace();
			throw ex;
		}
		try {
			doc = process.ParseXMLBytes(result);
		} catch (Exception ex) {
			errorData.currentDataType = DataType.text;
			errorData.currentErrorType = ErrorType.LoginDataError;
			errorData.text = ERR_LOGIN;
			throw ex;
		}
		try {
			return parse(doc);
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	private boolean parse(Document doc) throws Exception {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.LoginResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			if (GuildDefeat.judge(doc)) {
				return false;
			}
			
			if (!xpath.evaluate("//fairy_appearance", doc).equals("0")) {
				process.info.events.push(Info.EventType.fairyAppear);
			}
			
			process.info.userId = xpath.evaluate("//login/user_id", doc);
			ParseUserDataInfo.parse(doc, process);
			ParseCardList.parse(doc, process);
			
			process.info.SetTimeoutByAction(Name);
			
			process.info.cardMax = Integer.parseInt(xpath.evaluate("//your_data/max_card_num",doc));
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.LoginDataParseError;
			errorData.bytes = result;
			throw ex;
		}
		return true;
	}
	
}
