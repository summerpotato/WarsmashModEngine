package com.etheller.warsmash.viewer5.handlers.w3x.simulation.players;

public interface CPlayerUnitOrderListener {
	boolean issueTargetOrder(int unitHandleId, int abilityHandleId, int orderId, int targetHandleId, boolean queue);

	boolean issuePointOrder(int unitHandleId, int abilityHandleId, int orderId, float x, float y, boolean queue);

	// Below: used for "DROP ITEM AT POINT" ????
//	boolean issueTargetAndPointOrder(int unitHandleId, int orderId, int targetHandleId, float x, float y);

	boolean issueImmediateOrder(int unitHandleId, int abilityHandleId, int orderId, boolean queue);
}
