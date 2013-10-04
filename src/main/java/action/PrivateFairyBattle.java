package action;

import info.FairyBattleInfo;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;

import walker.ErrorData.DataType;
import walker.ErrorData.ErrorType;
import walker.Info;
import action.ActionRegistry.Action;

public class PrivateFairyBattle extends AbstractAction {
	public static final Action Name = Action.PRIVATE_FAIRY_BATTLE;
	
	private static final String URL_PRIVATE_BATTLE = "http://web.million-arthurs.com/connect/app/private_fairy/private_fairy_battle?cyt=1";
	
	private byte[] response;
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("no", process.info.fairy.No));
		post.add(new BasicNameValuePair("serial_id", process.info.fairy.SerialId));
		post.add(new BasicNameValuePair("user_id", process.info.fairy.UserId));
		try {
			response = process.network.ConnectToServer(URL_PRIVATE_BATTLE, post, false);
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
			errorData.currentErrorType = ErrorType.PrivateFairyBattleDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			if (!xpath.evaluate("/response/header/error/code", doc).equals("0")) {
				errorData.currentErrorType = ErrorType.PrivateFairyBattleResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			if (process.info.LatestFairyList.size() > 1000) process.info.LatestFairyList.poll();
			process.info.LatestFairyList.offer(process.info.fairy);
			
			if ((Boolean)xpath.evaluate("count(//private_fairy_top) > 0", doc, XPathConstants.BOOLEAN)) {
				process.info.events.push(Info.EventType.fairyBattleEnd);
				return true;
			}
			ParseUserDataInfo.parse(doc, process);
			ParseCardList.parse(doc, process);
			if (xpath.evaluate("//battle_result/winner", doc).equals("1")) {
				if(process.info.fairy.UserId.equals(process.info.userId))
					process.info.OwnFairyBattleKilled = true;
				process.info.events.push(Info.EventType.fairyBattleWin);
			} else {
				if(process.info.fairy.UserId.equals(process.info.userId))
					process.info.OwnFairyBattleKilled = false;
				process.info.events.push(Info.EventType.fairyBattleLose);
			}
			
			//info.fairy.FairyName = xpath.evaluate("//battle_vs_info/player[last()]/name", doc);
			process.info.SetTimeoutByAction(Name);
			
			String spec = xpath.evaluate("//private_fairy_reward_list/special_item/after_count", doc);
			if (spec.length() != 0) {
				process.info.gather = Integer.parseInt(spec);
			} else {
				process.info.gather = -1;
			}
			
			// 检查觉醒
			if ((Boolean)xpath.evaluate("count(//ex_fairy/rare_fairy)>0", doc, XPathConstants.BOOLEAN)) {
				// Yes
				process.info.fairy.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF | FairyBattleInfo.RARE;
				process.info.fairy.FairyLevel = xpath.evaluate("//ex_fairy/rare_fairy/lv", doc);
				process.info.fairy.SerialId = xpath.evaluate("//ex_fairy/rare_fairy/serial_id", doc);
				process.info.fairy.UserId = xpath.evaluate("//ex_fairy/rare_fairy/discoverer_id", doc);
				process.info.events.push(Info.EventType.fairyTransform);
			}
			
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.PrivateFairyBattleDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		
		return true;
		
	}
}
