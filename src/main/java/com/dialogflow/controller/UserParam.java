package com.dialogflow.controller;

/**
 * 
 * @author pritisharma
 * Created for Google DFCX SDK.
 *
 */
public class UserParam {
	public String Body;
	public String NumSegments;
	public String SmsSid;
	public String To;
	public String From;
	public String AccountSid;
	public String SmsStatus;
	public String NumMedia;
	public String Latitude;
	public String Longitude;
	public String Label;
	public String ChannelAddress;
	public String getChannelAddress() {
		return ChannelAddress;
	}
	public void setChannelAddress(String channelAddress) {
		ChannelAddress = channelAddress;
	}
	public String getLatitude() {
		return Latitude;
	}
	public void setLatitude(String latitude) {
		Latitude = latitude;
	}
	public String getLongitude() {
		return Longitude;
	}
	public void setLongitude(String longitude) {
		Longitude = longitude;
	}
	public String getLabel() {
		return Label;
	}
	public void setLabel(String label) {
		Label = label;
	}
	public String getAddress() {
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
	public String Address;
	
	public String getBody() {
		return Body;
	}
	public void setBody(String body) {
		Body = body;
	}
	public String getNumSegments() {
		return NumSegments;
	}
	public void setNumSegments(String numSegments) {
		NumSegments = numSegments;
	}
	public String getSmsSid() {
		return SmsSid;
	}
	public void setSmsSid(String smsSid) {
		SmsSid = smsSid;
	}
	public String getTo() {
		return To;
	}
	public void setTo(String to) {
		To = to;
	}
	public String getFrom() {
		return From;
	}
	public void setFrom(String from) {
		From = from;
	}
	public String getAccountSid() {
		return AccountSid;
	}
	public void setAccountSid(String accountSid) {
		AccountSid = accountSid;
	}
	public String getSmsStatus() {
		return SmsStatus;
	}
	public void setSmsStatus(String smsStatus) {
		SmsStatus = smsStatus;
	}
	public String getNumMedia() {
		return NumMedia;
	}
	public void setNumMedia(String numMedia) {
		NumMedia = numMedia;
	}
	
	
}
