package action;

import info.FairyBattleInfo;
import info.Floor;

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

public class Explore extends AbstractAction {
	public static final Action Name = Action.EXPLORE;
	
	private static final String URL_EXPLORE = "http://web.million-arthurs.com/connect/app/exploration/guild_explore?cyt=1";
	private byte[] response;
	
	public boolean run() throws Exception {
		ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();
		post.add(new BasicNameValuePair("area_id", process.info.front.areaId));
		post.add(new BasicNameValuePair("auto_build", "1"));
		post.add(new BasicNameValuePair("floor_id", process.info.front.floorId));
		try {
			response = process.network.ConnectToServer(URL_EXPLORE, post, false);
		} catch (Exception ex) {
			if (ex.getMessage().startsWith("302")) {
				process.info.events.push(Info.EventType.innerMapJump);
				return false;
			}
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
			errorData.currentErrorType = ErrorType.ExploreDataError;
			errorData.bytes = response;
			throw ex;
		}
		
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		
		try {
			String code = xpath.evaluate("/response/header/error/code", doc);
			if (!code.equals("0")) {
				if (code.equals("8000")) {
					process.info.events.push(Info.EventType.cardFull);
				}
				errorData.currentErrorType = ErrorType.ExploreResponse;
				errorData.currentDataType = DataType.text;
				errorData.text = xpath.evaluate("/response/header/error/message", doc);
				return false;
			}
			
			process.info.username = xpath.evaluate("//your_data/name", doc);
			process.info.lv = Integer.parseInt(xpath.evaluate("//town_level", doc));
			process.info.ap = Integer.parseInt(xpath.evaluate("//ap/current", doc));
			process.info.apMax = Integer.parseInt(xpath.evaluate("//ap/max", doc));
			process.info.bc = Integer.parseInt(xpath.evaluate("//bc/current", doc));
			process.info.bcMax = Integer.parseInt(xpath.evaluate("//bc/max", doc));
			process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
			
			process.info.SetTimeoutByAction(Name);
			
			// TODO: 添加升级事件
			process.info.exp = Integer.parseInt(xpath.evaluate("//explore/next_exp", doc));
			
			process.info.ExploreProgress = xpath.evaluate("//explore/progress", doc);
			process.info.ExploreGold = xpath.evaluate("//explore/gold", doc);
			process.info.ExploreExp = xpath.evaluate("//explore/get_exp", doc);
			
			int evt = Integer.parseInt(xpath.evaluate("//explore/event_type", doc));
			switch (evt) {
			case 22:
				// fairy battle
				process.info.fairy = new FairyBattleInfo();
				process.info.fairy.Type = FairyBattleInfo.PRIVATE | FairyBattleInfo.SELF;
				process.info.fairy.FairyName = xpath.evaluate("//ex_fairy/fairy/name", doc);
				process.info.fairy.FairyLevel = xpath.evaluate("//ex_fairy/fairy/lv", doc);
				process.info.fairy.SerialId = xpath.evaluate("//ex_fairy/fairy/serial_id", doc);
				process.info.fairy.UserId = xpath.evaluate("//ex_fairy/fairy/discoverer_id", doc);
				
				process.info.events.push(Info.EventType.privateFairyAppear);
				process.info.events.push(Info.EventType.gotoFloor);
				process.info.events.push(Info.EventType.recvPFBGood);
				process.info.ExploreResult = "Fairy Appear";
				break;
			case 5:
				// floor or area clear
				if ((Boolean)xpath.evaluate("count(//next_floor)>0", doc, XPathConstants.BOOLEAN)) {
					// floor clear
					Floor f = new Floor();
					f.areaId = xpath.evaluate("//next_floor/area_id", doc);
					f.floorId = xpath.evaluate("//next_floor/floor_info/id", doc);
					f.cost = Integer.parseInt(xpath.evaluate("//next_floor/floor_info/cost", doc));
					process.info.front = f;
					process.info.floor.put(f.cost, f);
					process.info.ExploreResult = "Floor Clear";
				} else {
					process.info.events.push(Info.EventType.areaComplete);
					process.info.ExploreResult = "Area Clear";
				}
				break;
			case 12:
				// AP
				process.info.ExploreResult = String.format("AP recover(%d)", 
						Integer.parseInt(xpath.evaluate("//explore/recover", doc)));
				break;
			case 13:
				// BC
				process.info.ExploreResult = String.format("BC recover(%d)", 
						Integer.parseInt(xpath.evaluate("//explore/recover", doc)));
				break;
			case 19:
				int delta = Integer.parseInt(xpath.evaluate("//special_item/after_count", doc)) - 
							Integer.parseInt(xpath.evaluate("//special_item/before_count", doc));
				process.info.ExploreResult = String.format("Gather(%d)", delta);
				break;
			case 2:
				process.info.ExploreResult = "Meet People";
				break;
			case 3:
				process.info.ExploreResult = "Get Card";
				break;
			default:
				process.info.ExploreResult = String.format("Code: %d", evt);
				break;
			}
			
		} catch (Exception ex) {
			if (errorData.currentErrorType != ErrorType.none) throw ex;
			errorData.currentDataType = DataType.bytes;
			errorData.currentErrorType = ErrorType.ExploreDataParseError;
			errorData.bytes = response;
			throw ex;
		}
		return true;
	}
	
}
