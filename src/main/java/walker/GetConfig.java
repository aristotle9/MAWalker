package walker;

import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walker.ErrorData.ErrorType;

public class GetConfig {
	public static void parse(Document doc, Info info, ErrorData errorData) throws Exception {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			
			info.LoginId = xpath.evaluate("/config/username", doc);
			info.LoginPw = xpath.evaluate("/config/password", doc);
			info.UserAgent = xpath.evaluate("/config/user_agent", doc);
			
			info.Profile = Integer.parseInt(xpath.evaluate("/config/profile", doc));
			
			switch (info.Profile) {
			case 1:
				NodeList idl = (NodeList)xpath.evaluate("/config/sell_card/id", doc, XPathConstants.NODESET);
				info.CanBeSold = new ArrayList<String>();
				for (int i = 0; i< idl.getLength(); i++) {
					Node idx = idl.item(i);
					try {
						info.CanBeSold.add(idx.getFirstChild().getNodeValue());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
				info.FairyBattleFirst = xpath.evaluate("/config/option/fairy_battle_first", doc).equals("1");
				info.RareFairyUseNormalDeck = xpath.evaluate("/config/option/rare_fairy_use_normal_deck", doc).equals("1");
				info.AllowBCInsuffient = xpath.evaluate("/config/option/allow_bc_insuffient", doc).equals("1");
				info.OneAPOnly = xpath.evaluate("/config/option/one_ap_only", doc).equals("1");
				info.AutoAddp = xpath.evaluate("/config/option/auto_add_point", doc).equals("1");
				info.AllowAttackSameFairy = xpath.evaluate("/config/option/allow_attack_same_fairy", doc).equals("1");
				info.debug = xpath.evaluate("/config/option/debug", doc).equals("1");
				info.nightModeSwitch = xpath.evaluate("/config/option/night_mode", doc).equals("1");
				info.receiveBattlePresent = xpath.evaluate("/config/option/receive_battle_present", doc).equals("1");
				
				info.autoUseAp = xpath.evaluate("/config/use/auto_use_ap", doc).equals("1");
				if (info.autoUseAp) {
					String half = xpath.evaluate("/config/use/strategy/ap/half", doc);
					if (half.equals("0")) {
						info.autoApType = Info.autoUseType.FULL_ONLY;
					} else if (half.equals("1")) {
						info.autoApType = Info.autoUseType.HALF_ONLY;
					} else {
						info.autoApType = Info.autoUseType.ALL;
					}
					info.autoApLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/ap/low",doc));
					info.autoApFullLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/ap/full_low",doc));
				}
				info.autoUseBc = xpath.evaluate("/config/use/auto_use_bc", doc).equals("1");
				if (info.autoUseBc) {
					String half = xpath.evaluate("/config/use/strategy/bc/half", doc);
					if (half.equals("0")) {
						info.autoBcType = Info.autoUseType.FULL_ONLY;
					} else if (half.equals("1")) {
						info.autoBcType = Info.autoUseType.HALF_ONLY;
					} else {
						info.autoBcType = Info.autoUseType.ALL;
					}
					info.autoBcLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/bc/low",doc));
					info.autoBcFullLow = Integer.parseInt(xpath.evaluate("/config/use/strategy/bc/full_low",doc));
				}
				
				
				
				
				

				info.FriendFairyBattleRare.No = xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleRare']/no", doc);
				info.FriendFairyBattleRare.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleRare']/bc", doc));
				
				info.FriendFairyBattleNormal.No = xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleNormal']/no", doc);
				info.FriendFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FriendFairyBattleNormal']/bc", doc));
				
				info.PublicFairyBattle.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/bc", doc));
				info.PublicFairyBattle.No = xpath.evaluate("/config/deck/deck_profile[name='GuildFairyDeck']/no", doc);

				info.PrivateFairyBattleNormal.No = xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/no", doc);
				info.PrivateFairyBattleNormal.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='FairyDeck']/bc", doc));
				
				info.PrivateFairyBattleRare.No = xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/no", doc);
				info.PrivateFairyBattleRare.BC = Integer.parseInt(xpath.evaluate("/config/deck/deck_profile[name='RareFairyDeck']/bc", doc));
				
				
				break;
			case 2:
				
				info.OneAPOnly = true;
				info.AllowBCInsuffient = true;
				info.FairyBattleFirst = false;
				info.RareFairyUseNormalDeck = false;
				
				info.FriendFairyBattleRare.No = "0";
				info.FriendFairyBattleRare.BC = 0;
				
				info.PublicFairyBattle.BC = 0;
				info.PublicFairyBattle.No = "0";

				info.PrivateFairyBattleNormal.No = "1";
				info.PrivateFairyBattleNormal.BC = 97;
				
				info.PrivateFairyBattleRare.No = "2";
				info.PrivateFairyBattleRare.BC = 2;
				
				break;
			}
			
			
		} catch (Exception ex) {
			if (errorData.currentErrorType == ErrorType.none) {
				throw ex;
			}
		}
		
	}
}
