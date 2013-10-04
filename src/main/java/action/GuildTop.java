package action;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import walker.Info;
import action.ActionRegistry.Action;

public class GuildTop extends AbstractAction {
	public static final Action Name = Action.GUILD_TOP;
	private static final String URL_GUILD_TOP = "http://web.million-arthurs.com/connect/app/guild/guild_top?cyt=1";
	
	private byte[] response;
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		try {
			response = process.network.ConnectToServer(URL_GUILD_TOP, post, false);
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
			errorData.currentErrorType = ErrorType.GuildTopDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.GuildTopResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}

			if (GuildDefeat.judge(doc)) {
				process.info.events.push(Info.EventType.guildTopRetry);
				return false;
			}
			
			if ((Boolean)xpath.evaluate("count(//guild_top_no_fairy)>0", doc, XPathConstants.BOOLEAN)) {
				// 深夜没有外敌战
				process.info.NoFairy = true;
				return false;
			} else {
				process.info.NoFairy = false;
			}
			
			process.info.gfairy.FairyName = xpath.evaluate("//fairy/name", doc);
			process.info.gfairy.SerialId = xpath.evaluate("//fairy/serial_id", doc);
			process.info.gfairy.GuildId = xpath.evaluate("//fairy/discoverer_id", doc);
			process.info.gfairy.FairyLevel = xpath.evaluate("//fairy/lv", doc);
			
			process.info.events.push(Info.EventType.guildBattle);
			
			return true;
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.GuildTopDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		
	}
	
}
