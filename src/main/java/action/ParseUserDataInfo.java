package action;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import walker.Info;

public class ParseUserDataInfo {
	public static void parse(Document doc, walker.Process process) throws NumberFormatException, XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		process.info.username = xpath.evaluate("//your_data/name", doc);
		process.info.lv = Integer.parseInt(xpath.evaluate("//your_data/town_level", doc));
		process.info.ap = Integer.parseInt(xpath.evaluate("//your_data/ap/current", doc));
		process.info.apMax = Integer.parseInt(xpath.evaluate("//your_data/ap/max", doc));
		process.info.bc = Integer.parseInt(xpath.evaluate("//your_data/bc/current", doc));
		process.info.bcMax = Integer.parseInt(xpath.evaluate("//your_data/bc/max", doc));
		process.info.guildId = xpath.evaluate("//your_data/party_id", doc);
		if ((Boolean)xpath.evaluate("count(//your_data/free_ap_bc_point)>0", doc, XPathConstants.BOOLEAN)) {
			process.info.pointToAdd = Integer.parseInt(xpath.evaluate("//your_data/free_ap_bc_point", doc));
			if (process.info.pointToAdd > 0) process.info.events.push(Info.EventType.levelUp);
		}
		if ((Boolean)xpath.evaluate("count(//your_data/itemlist[item_id=202])>0", doc, XPathConstants.BOOLEAN)) {
			process.info.ticket = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=202]/num", doc));
			if (process.info.ticket > 0) process.info.events.push(Info.EventType.ticketFull);
		}
		if ((Boolean)xpath.evaluate("count(//your_data/itemlist[item_id=1])>0", doc, XPathConstants.BOOLEAN)) {
			process.info.fullAp = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=1]/num", doc));
		}
		if ((Boolean)xpath.evaluate("count(//your_data/itemlist[item_id=2])>0", doc, XPathConstants.BOOLEAN)) {
			process.info.fullBc = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=2]/num", doc));
		}
		if ((Boolean)xpath.evaluate("count(//your_data/itemlist[item_id=101])>0", doc, XPathConstants.BOOLEAN)) {
			process.info.halfAp = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=101]/num", doc));
			process.info.halfApToday = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=101]/times", doc));
		}
		if ((Boolean)xpath.evaluate("count(//your_data/itemlist[item_id=111])>0", doc, XPathConstants.BOOLEAN)) {
			process.info.halfBc = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=111]/num", doc));
			process.info.halfBcToday = Integer.parseInt(xpath.evaluate("//your_data/itemlist[item_id=111]/times", doc));
		}
		
		
		
			
	}
}
