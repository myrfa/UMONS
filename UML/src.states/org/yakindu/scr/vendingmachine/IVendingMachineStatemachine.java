package org.yakindu.scr.vendingmachine;
import org.yakindu.scr.IStatemachine;
import org.yakindu.scr.ITimerCallback;

public interface IVendingMachineStatemachine extends ITimerCallback, IStatemachine {

	public interface SCInterface {
		public void raiseInsertPiece();
		public void raiseAddItem();
		public void raiseRefound();
		public void raiseMaintenance();
		public void raiseAdd();
		public void raiseDelete();
		public void raiseLogin();
		public void raiseCreate();
		public void raiseLoad();
		public void raiseAlter();
		public void raiseSave();
		public void raiseLogout();
		public long getPiece();
		public void setPiece(long value);
		public long getItemPrice();
		public void setItemPrice(long value);
		public long getLoginType();
		public void setLoginType(long value);
		public long getTotalPaid();
		public void setTotalPaid(long value);

	}

	public SCInterface getSCInterface();

}