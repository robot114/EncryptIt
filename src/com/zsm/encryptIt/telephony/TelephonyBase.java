package com.zsm.encryptIt.telephony;

public interface TelephonyBase {

	public abstract String getOutgoingCall();

	public abstract void setOutgoingCall(String number);

	public abstract String getOutgoingSms();

	public abstract void setOutgoingSms(String number);

}
