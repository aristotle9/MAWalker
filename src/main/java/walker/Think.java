package walker;

import info.Card;

import java.util.List;

import action.ActionRegistry;
import action.ActionRegistry.Action;

public class Think {
	
	private static final String AP_HALF = "101";
	private static final String BC_HALF = "111";
	private static final String AP_FULL = "1";
	private static final String BC_FULL = "2";
	
	private static final int EXPLORE_NORMAL = 60;
	private static final int EXPLORE_URGENT = 80;
	private static final int GFL_PRI = 50;
	private static final int GFL_HI_PRI = 70;
	private static final int GF_PRI = 25;
	private static final int USE_PRI = 99;
	
	private Process process;
	private Info info;
	
	public Think(Process process) {
		this.process = process;
		this.info = process.info;
	}
	
	public ActionRegistry.Action doIt (List<ActionRegistry.Action> possible) {
		Action best = Action.NOTHING;
		int score = Integer.MIN_VALUE + 20;
		for (int i = 0; i < possible.size(); i++) {
			switch (possible.get(i)) {
			case LOGIN:
				return ActionRegistry.Action.LOGIN;
			case ADD_AREA:
				return Action.ADD_AREA;
			case GET_FLOOR_INFO:
				return Action.GET_FLOOR_INFO;
			case GET_FAIRY_LIST:
				if (info.FairyBattleFirst) {
					if (score < GFL_HI_PRI) {
						best = Action.GET_FAIRY_LIST;
						score = GFL_HI_PRI;
					}
				} else {
					if (score < GFL_PRI) {
						best = Action.GET_FAIRY_LIST;
						score = GFL_PRI;
					}
				}
				break;
			case GOTO_FLOOR:
				if (score < GF_PRI) {
					best = Action.GOTO_FLOOR;
					score = GF_PRI;
				}
				break;
			case PRIVATE_FAIRY_BATTLE:
				if (info.Profile == 2) {
					process.info.fairy.No = "2";
					return Action.PRIVATE_FAIRY_BATTLE;
				}
				if (canBattle()) return Action.PRIVATE_FAIRY_BATTLE;
				break;
			case EXPLORE:
				int p = explorePoint();
				if (p > score) {
					best = Action.EXPLORE;
					score = p;
				}
				break;
			case GUILD_BATTLE:
				process.info.gfairy.No = info.PublicFairyBattle.No;
				return Action.GUILD_BATTLE;
			case GUILD_TOP:
				return Action.GUILD_TOP;
			case GET_FAIRY_REWARD:
				return Action.GET_FAIRY_REWARD;
			case PFB_GOOD:
				return Action.PFB_GOOD;
			case RECV_PFB_GOOD:
				return Action.RECV_PFB_GOOD;
			case NOTHING:
				break;
			case SELL_CARD:
				if (cardsToSell()) return Action.SELL_CARD;
				break;
			case LV_UP:
				decideUpPoint();
				return Action.LV_UP;
			case USE:
				int ptr = decideUse();
				if (ptr > score) {
					best = Action.USE;
					score = ptr;
				}
				break;
			default:
				break;
			}
		}
		return best;
	}
	
	private int decideUse() {
		
		if (info.autoUseAp) {
			if (process.info.ap < info.autoApLow) {
				switch (info.autoApType) {
				case ALL:
					if (process.info.halfApToday > 0 && process.info.halfAp > 0) {
						process.info.toUse = AP_HALF;
						return USE_PRI;
					} else {
						if (process.info.fullAp > info.autoApFullLow) {
							process.info.toUse = AP_FULL;
							return USE_PRI;
						}
					}
					break;
				case FULL_ONLY:
					if (process.info.fullAp > info.autoApFullLow) {
						process.info.toUse = AP_FULL;
						return USE_PRI;
					}
					break;
				case HALF_ONLY:
					if (process.info.halfApToday > 0 && process.info.halfAp > 0) {
						process.info.toUse = AP_HALF;
						return USE_PRI;
					}
					break;
				default:
					break;
				
				}
			}
		}
		if (info.autoUseBc) {
			if (process.info.bc < info.autoBcLow) {
				switch (info.autoBcType) {
				case ALL:
					if (process.info.halfBcToday > 0 && process.info.halfBc > 0) {
						process.info.toUse = BC_HALF;
						return USE_PRI;
					} else {
						if (process.info.fullBc > info.autoBcFullLow) {
							process.info.toUse = BC_FULL;
							return USE_PRI;
						}
					}
					break;
				case FULL_ONLY:
					if (process.info.fullBc > info.autoBcFullLow) {
						process.info.toUse = BC_FULL;
						return USE_PRI;
					}
					break;
				case HALF_ONLY:
					if (process.info.halfBcToday > 0 && process.info.halfBc > 0) {
						process.info.toUse = BC_HALF;
						return USE_PRI;
					}
					break;
				default:
					break;
				
				}
			}
		}
		return Integer.MIN_VALUE;
	}

	private boolean canBattle() {
		switch (process.info.fairy.Type) {
		case 0:
			process.info.fairy.No = info.PublicFairyBattle.No;
			break;
		case 4:
			if (!info.AllowBCInsuffient && process.info.bc < info.FriendFairyBattleNormal.BC) return false;
			if (process.info.bc <= 2) return false;
			process.info.fairy.No = info.FriendFairyBattleNormal.No;
			break;
		case 5:
			if (info.RareFairyUseNormalDeck) {
				if (!info.AllowBCInsuffient && process.info.bc <= info.FriendFairyBattleRare.BC)
				{
					if(process.info.bc <= info.FriendFairyBattleNormal.BC) return false;
					process.info.fairy.No = info.FriendFairyBattleNormal.No;
				}
				else if(process.info.bc <= info.FriendFairyBattleRare.BC)
				{
					if(process.info.bc > 2)
					{
						process.info.fairy.No = info.FriendFairyBattleRare.No;
					}
					else return false;
				}
				else
				{
					process.info.fairy.No = info.FriendFairyBattleRare.No;
				}
				
			}
			else
			{
				if(!info.AllowBCInsuffient)
				{
					if(process.info.bc <= info.FriendFairyBattleRare.BC) return false;
					process.info.fairy.No = info.FriendFairyBattleRare.No;
				}
				else if(process.info.bc > 2)
				{
					process.info.fairy.No = info.FriendFairyBattleRare.No;
				}
				else return false;

			}
			break;
		case 6:
			
			if (!info.AllowBCInsuffient && process.info.bc < info.PrivateFairyBattleNormal.BC) return false;
			if (process.info.bc <= 2) return false;
			process.info.fairy.No = info.PrivateFairyBattleNormal.No;
			break;
		case 7:
			if (info.RareFairyUseNormalDeck) {
				if (!info.AllowBCInsuffient && process.info.bc <= info.PrivateFairyBattleRare.BC)
				{
					if(process.info.bc <= info.PrivateFairyBattleNormal.BC) return false;
					process.info.fairy.No = info.PrivateFairyBattleNormal.No;
				}
				else if(process.info.bc <= info.PrivateFairyBattleRare.BC)
				{
					if(process.info.bc > 2)
					{
						process.info.fairy.No = info.PrivateFairyBattleRare.No;
					}
					else return false;
				}
				else
				{
					process.info.fairy.No = info.PrivateFairyBattleRare.No;
				}
				
			}
			else
			{
				if(!info.AllowBCInsuffient)
				{
					if(process.info.bc <= info.PrivateFairyBattleRare.BC) return false;
					process.info.fairy.No = info.PrivateFairyBattleRare.No;
				}
				else if(process.info.bc > 2)
				{
					process.info.fairy.No = info.PrivateFairyBattleRare.No;
				}
				else return false;

			}
			break;
		default:
			return false;
		}
		return true;
	}
	
	private void decideUpPoint() {
		if (info.Profile == 1) {
			//主号全加BC
			process.info.apUp = 0;
			process.info.bcUp = process.info.pointToAdd;
		} else if (info.Profile == 2) {
			//小号全加AP
			process.info.apUp = process.info.pointToAdd;
			process.info.bcUp = 0;
		}
	}
	
	private int explorePoint() {
		try {
			if (info.Profile == 2) {
				if (process.info.ap < 1) return Integer.MIN_VALUE;
				process.info.front = process.info.floor.get(1);
				return EXPLORE_URGENT;
			}
			if (process.info.bc == 0) return Integer.MIN_VALUE;
			// 首先确定楼层
			if (process.info.AllClear) {
				int ap = process.info.ap / process.info.bc * info.PrivateFairyBattleNormal.BC;
				if (ap > 1) {
					process.info.front = process.info.floor.get(ap);
				} else {
					process.info.front = process.info.floor.get(1);
				}
			}
			if (info.OneAPOnly) process.info.front = process.info.floor.get(1);
			// 判断是否可以行动
			if (process.info.front == null) process.info.front = process.info.floor.get(1);
			if (!info.AllowBCInsuffient && process.info.bc < info.PrivateFairyBattleNormal.BC) return Integer.MIN_VALUE;
			if (process.info.ap < process.info.front.cost) return Integer.MIN_VALUE;
			if (process.info.ap == process.info.apMax) return EXPLORE_URGENT;
		} catch (Exception ex) {
			ex.printStackTrace();
			return Integer.MIN_VALUE;
		}
		return EXPLORE_NORMAL;
	}
	private boolean cardsToSell() {
		if (info.Profile == 2) {
			int count = 0;
			String toSell = "";
			for (Card c : process.info.cardList) {
				if (!info.KeepCard.contains(c.serialId)) {
					if (toSell.isEmpty()) {
						toSell = c.serialId;
					} else {
						toSell += "," + c.serialId;
					}
					count++;
				}
				if (count >= 30) break;
			}
			process.info.toSell = toSell;
			return false; // 测试状态
			//return !toSell.isEmpty();
		} else if (info.Profile == 1) {
			int count = 0;
			String toSell = "";
			for (Card c : process.info.cardList) {
				if (!c.exist) continue;
				if (c.holo && c.hp >= 3500) continue; //闪卡不卖，但是低等级的闪卡照样要卖
				if (c.hp > 6000) continue; //防止不小心把贵重卡片卖了 
				if (info.CanBeSold.contains(c.cardId)) {
					if (toSell.isEmpty()) {
						toSell = c.serialId;
					} else {
						toSell += "," + c.serialId;
					}
					count++;
					c.exist = false;
				}
				if (count >= 30) break;
			}
			
			process.info.toSell = toSell;
			return !toSell.isEmpty();
		}
		return false;
		
	}
	
}
