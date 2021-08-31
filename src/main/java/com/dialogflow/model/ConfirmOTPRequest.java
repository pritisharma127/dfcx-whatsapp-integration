package com.dialogflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ConfirmOTPRequest {
	
	@JsonProperty
	private String otp;
	
	@JsonProperty
	private String txnId;
	
	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

}