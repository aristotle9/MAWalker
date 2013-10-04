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
import action.ActionRegistry.Action;

public class GuildBattle extends AbstractAction{
	public static final Action Name = Action.GUILD_BATTLE;
	
	private static final String URL_GUILD_BATTLE = "http://web.million-arthurs.com/connect/app/fairy/guild_fairy_battle?cyt=1";
	private byte[] response;
	
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("guild_id", process.info.gfairy.GuildId));
		post.add(new BasicNameValuePair("no", process.info.gfairy.No));
		post.add(new BasicNameValuePair("serial_id", process.info.gfairy.SerialId));
		post.add(new BasicNameValuePair("spp_skill_serial", process.info.gfairy.Spp));
		try {
			response = process.network.ConnectToServer(URL_GUILD_BATTLE, post, false);
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
			errorData.currentErrorType = ErrorType.GuildBattleDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.GuildBattleResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			if (GuildDefeat.judge(doc)) {
				process.info.events.push(Info.EventType.guildTopRetry);
				return true;
			}
			
			ParseUserDataInfo.parse(doc, process);

			if (xpath.evaluate("//own_fairy_battle_result/winner", doc).equals("1")) {
				process.info.events.push(Info.EventType.fairyBattleWin);
			} else {
				process.info.events.push(Info.EventType.fairyBattleLose);
			}

			process.info.week = xpath.evaluate("//week_total_contribution", doc);
			
			process.info.SetTimeoutByAction(Name);
			
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.GuildBattleDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		
		return true;
		
	}
	
}
