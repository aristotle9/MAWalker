package walker;

import java.util.ArrayList;
import java.util.List;

import walker.ErrorData.ErrorType;
import walker.Info.TimeoutEntry;
import action.ActionRegistry;
import action.Explore;
import action.GetFloorInfo;
import action.GotoFloor;
import action.Login;
import action.LvUp;
import action.PFBGood;
import action.PrivateFairyBattle;
import action.RecvPFBGood;
import action.SellCard;
import action.ActionRegistry.Action;

final public class Profile2 {
	
	private ErrorData errorData;
	private Process process;
	
	public Profile2(Process process) {
		
		this.process = process;
		this.errorData = process.errorData;
	}
	
	public void auto() throws Exception {
		try {
			if (errorData.currentErrorType != ErrorType.none) {
				rescue();
			} else {
				long start = System.currentTimeMillis();
				execute(process.think.doIt(getPossibleAction()));
				long delta = System.currentTimeMillis() - start;
				if (delta < 15000) Thread.sleep(15000 - delta);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}
	
	private void rescue() {
		Go.log(errorData.currentErrorType.toString());
		switch (errorData.currentDataType) {
		case bytes:
			Go.log(new String(errorData.bytes));
			break;
		case text:
			Go.log(new String(errorData.text));
			break;
		default:
			break;
		}
		errorData.clear();
	}
	
	private List<ActionRegistry.Action> getPossibleAction() {
		ArrayList<ActionRegistry.Action> result = new ArrayList<ActionRegistry.Action>();
		if (process.info.events.size() != 0) {
			switch(process.info.events.peek()) {
			case notLoggedIn:
			case cookieOutOfDate:
				result.add(ActionRegistry.Action.LOGIN);
				break;
			case fairyTransform:
				Go.log("Rare Fairy Appear");
			case privateFairyAppear:
			case fairyCanBattle:
				result.add(ActionRegistry.Action.PRIVATE_FAIRY_BATTLE);
				break;
			case innerMapJump:
				Go.log("Map Status Changed!");
			case needFloorInfo:	
				result.add(ActionRegistry.Action.GET_FLOOR_INFO);
				break;
			case cardFull:
				result.add(ActionRegistry.Action.SELL_CARD);
				break;
			case needAPBCInfo:
				result.add(ActionRegistry.Action.GOTO_FLOOR);
				break;
			case fairyReward:
				result.add(ActionRegistry.Action.GET_FAIRY_REWARD);
				break;
			case levelUp:
				result.add(Action.LV_UP);
				break;
			case PFBGood:
				result.add(Action.PFB_GOOD);
				break;
			case recvPFBGood:
				result.add(Action.RECV_PFB_GOOD);
				break;
			case gotoFloor:
				result.add(Action.GOTO_FLOOR);
			default:
				Go.log("Profile2 Ignore: " + process.info.events.peek());
				break;
			}
			process.info.events.pop();
			return result;
		}
		ArrayList<TimeoutEntry> te = process.info.CheckTimeout();
		for (TimeoutEntry e : te) {
			switch (e) {
			case apbc:
				process.info.events.push(Info.EventType.needAPBCInfo);
				break;
			case login:
				process.info.events.push(Info.EventType.cookieOutOfDate);
				break;
			case map:
				process.info.events.push(Info.EventType.needFloorInfo);
				break;
			case fairy:
			case reward:
			default:
				break;
			}				
		}
		result.add(ActionRegistry.Action.EXPLORE);
		return result;
	}
	
	private void execute(ActionRegistry.Action action) throws Exception {
		switch (action) {
		case LOGIN:
			try {
				if (process.action(Login.class).run()) {
					Go.log(String.format("User: %s, AP: %d/%d, BC: %d/%d, Card: %d/%d, ticket: %d",
							process.info.username, process.info.ap, process.info.apMax, process.info.bc, process.info.bcMax,
							process.info.cardList.size(), process.info.cardMax, process.info.ticket));	
					process.info.events.push(Info.EventType.needFloorInfo);
				} else {
					process.info.events.push(Info.EventType.notLoggedIn);
				}
			} catch (Exception ex) {
				process.info.events.push(Info.EventType.notLoggedIn);
				if (errorData.currentErrorType == ErrorType.none) {
					throw ex;
				}
			}
			break;
		case GET_FLOOR_INFO:
			try {
				if (process.action(GetFloorInfo.class).run()) {
					Go.log(String.format("Area(%d) Front: %s>%s@c=%d", 
							process.info.area.size(), 
							process.info.area.get(Integer.parseInt(process.info.front.areaId)).areaName, 
							process.info.front.floorId, 
							process.info.front.cost));
				}
				
			} catch (Exception ex) {
				if (ex.getMessage().equals("302")) {
					process.info.events.push(Info.EventType.innerMapJump);
					errorData.clear();
				} else {
					if (errorData.currentErrorType == ErrorType.none) throw ex;
				}
			}
			break;
		case GOTO_FLOOR:
			try {
				if (process.action(GotoFloor.class).run()) {
					Go.log(String.format("Goto: AP: %d/%d, BC: %d/%d, Front:%s>%s",
							process.info.ap, process.info.apMax, process.info.bc, process.info.bcMax,
							process.info.area.get(Integer.parseInt(process.info.front.areaId)).areaName, 
							process.info.front.floorId));	
				} else {
					
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			
			break;
		case PRIVATE_FAIRY_BATTLE:
			try {
				if (process.action(PrivateFairyBattle.class).run()) {
					String result = "";
					if (!process.info.events.empty()) {
						switch (process.info.events.peek()) {
						case fairyBattleEnd:
							result = "Too Late";
							process.info.events.pop();
							break;
						case fairyBattleLose:
							result = "Lose";
							process.info.events.pop();
							break;
						case fairyBattleWin:
							result = "Win";
							process.info.events.pop();
							break;
						default:
							break;
						}
					}
					String str = String.format("PFB name=%s, Lv: %s, bc: %d/%d, ap: %d/%d, ticket: %d, %s",
							process.info.fairy.FairyName, process.info.fairy.FairyLevel, process.info.bc, process.info.bcMax, process.info.ap, process.info.apMax, 
							process.info.ticket, result);
					if (process.info.gather != -1) str += String.format(", gather=%d", process.info.gather);
					Go.log(str);
				} else {
					
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			break;
		case EXPLORE:
			try {
				if (process.action(Explore.class).run()) {
					Go.log(String.format("Explore: AP: %d, Gold+%s, Exp+%s, Progress:%s, Result: %s.", process.info.ap,
							process.info.ExploreGold, process.info.ExploreExp, process.info.ExploreProgress, process.info.ExploreResult));
				} else {
					
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			break;
		case LV_UP:
			try {
				if (process.action(LvUp.class).run()) {
					Go.log(String.format("Level UP! AP:%d BC:%d", process.info.apMax, process.info.bcMax));
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			break;
		case SELL_CARD:
			try {
				if (process.action(SellCard.class).run()) {
					Go.log(errorData.text);
					errorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			break;
		case PFB_GOOD:
			try {
				if (process.action(PFBGood.class).run()) {
					Go.log(errorData.text);
					errorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
				
			}
			break;
		case RECV_PFB_GOOD:
			try {
				if (process.action(RecvPFBGood.class).run()) {
					Go.log(errorData.text);
					errorData.clear();
				} else {
					Go.log("Something wrong");
				}
			} catch (Exception ex) {
				if (errorData.currentErrorType == ErrorType.none) throw ex;
			}
			break;
		default:
			break;
		}
	}
	
}
