package com.etheller.warsmash.viewer5.handlers.w3x.simulation.behaviors;

import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CSimulation;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CUnit;
import com.etheller.warsmash.viewer5.handlers.w3x.simulation.CWidget;

public abstract class CAbstractRangedBehavior implements CRangedBehavior {
	protected final CUnit unit;

	public CAbstractRangedBehavior(final CUnit unit) {
		this.unit = unit;
	}

	private boolean wasWithinPropWindow = false;
	protected CWidget target;
	private boolean wasInRange = false;
	private CBehaviorMove moveBehavior;

	protected final CAbstractRangedBehavior innerReset(final CWidget target) {
		this.wasWithinPropWindow = false;
		this.target = target;
		this.wasInRange = false;
		if (!this.unit.isMovementDisabled()) {
			if ((target instanceof CUnit) && !((CUnit) target).getUnitType().isBuilding()) {
				this.moveBehavior = this.unit.getMoveBehavior().reset((CUnit) target, this);
			}
			else {
				this.moveBehavior = this.unit.getMoveBehavior().reset(target.getX(), target.getY(), this);
			}
		}
		else {
			this.moveBehavior = null;
		}
		return this;
	}

	protected abstract CBehavior update(CSimulation simulation, boolean withinRange);

	protected abstract boolean checkTargetStillValid(CSimulation simulation);

	protected abstract void resetBeforeMoving(CSimulation simulation);

	@Override
	public final CBehavior update(final CSimulation simulation) {
		if (!checkTargetStillValid(simulation)) {
			return this.unit.pollNextOrderBehavior(simulation);
		}
		if (!isWithinRange(simulation)) {
			if (this.moveBehavior == null) {
				return this.unit.pollNextOrderBehavior(simulation);
			}
			this.wasInRange = false;
			resetBeforeMoving(simulation);
			;
			return this.unit.getMoveBehavior();
		}
		this.wasInRange = true;
		if (!this.unit.isMovementDisabled()) {
			final float prevX = this.unit.getX();
			final float prevY = this.unit.getY();
			final float deltaY = this.target.getY() - prevY;
			final float deltaX = this.target.getX() - prevX;
			final double goalAngleRad = Math.atan2(deltaY, deltaX);
			float goalAngle = (float) Math.toDegrees(goalAngleRad);
			if (goalAngle < 0) {
				goalAngle += 360;
			}
			float facing = this.unit.getFacing();
			float delta = goalAngle - facing;
			final float propulsionWindow = simulation.getGameplayConstants().getAttackHalfAngle();
			final float turnRate = simulation.getUnitData().getTurnRate(this.unit.getTypeId());

			if (delta < -180) {
				delta = 360 + delta;
			}
			if (delta > 180) {
				delta = -360 + delta;
			}
			final float absDelta = Math.abs(delta);

			if ((absDelta <= 1.0) && (absDelta != 0)) {
				this.unit.setFacing(goalAngle);
			}
			else {
				float angleToAdd = Math.signum(delta) * (float) Math.toDegrees(turnRate);
				if (absDelta < Math.abs(angleToAdd)) {
					angleToAdd = delta;
				}
				facing += angleToAdd;
				this.unit.setFacing(facing);
			}
			if (absDelta < propulsionWindow) {
				this.wasWithinPropWindow = true;
			}
			else {
				// If this happens, the unit is facing the wrong way, and has to turn before
				// moving.
				this.wasWithinPropWindow = false;
			}
		}
		else {
			this.wasWithinPropWindow = true;
		}

		return update(simulation, this.wasWithinPropWindow);
	}

}
