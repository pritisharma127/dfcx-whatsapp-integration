package com.dialogflow.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.dialogflow.model.OTPConfirmationRequest;

/**
 * @author pritisharma
 * This is the service class where Aarogya Setu public APIs are called for generating OTP, validating OTP and vaccination certificate download.
 *
 */

@Service
public class CowinService {
	
	private static Logger logger = LoggerFactory.getLogger(CowinService.class);
	
	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	RestTemplateBuilder restTemplBuilder;
	
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	public String generateOTP(String mobile) {
		
		logger.info("CowinService.generateOTPWhatsApp(), mobile = " + mobile);
		mobile = "{\"mobile\": \"" + mobile + "\"}";
		String url = "https://cdn-api.co-vin.in/api/v2/auth/public/generateOTP";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> list = new ArrayList<MediaType>();
		
		list.add(MediaType.ALL);
		headers.setAccept(list);
		HttpEntity<String> requestEntity = new HttpEntity<String>(mobile, headers);
		
		logger.info("CowinService.generateOTPWhatsApp(), requestEntity = " + requestEntity.getBody());
		
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		
		logger.info("CowinService.generateOTPWhatsApp(), response = " + response.getBody());
		
		return response.getBody();
		
	}
	
	public String validateOTP(String encodedOTP, String trxnId) {
		
		String url = "https://cdn-api.co-vin.in/api/v2/auth/public/confirmOTP";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.ALL);
		headers.setAccept(list);
		
		OTPConfirmationRequest otpConfirmationRequest = new OTPConfirmationRequest();
		otpConfirmationRequest.setOtp(encodedOTP);
		otpConfirmationRequest.setTxnId(trxnId);
		
		HttpEntity<OTPConfirmationRequest> requestEntity = new HttpEntity<OTPConfirmationRequest>(otpConfirmationRequest, headers);
		
		logger.info("CowinService.validateOTP(), requestEntity = " + requestEntity.getBody());
		
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		
		logger.info("CowinService.validateOTP(), response = " + response.getBody());
		
		return response.getBody();
		
	}
	
	public void downloadCertificate(String benRefId, String token) throws Exception {
		
		String url = "https://cdn-api.co-vin.in/api/v2/registration/certificate/public/download?beneficiary_reference_id=" + benRefId;
		logger.info("CowinService.downloadCertificate(), url == " + url);
		HttpHeaders headers = new HttpHeaders();
		List<MediaType> list = new ArrayList<MediaType>();
		list.add(MediaType.APPLICATION_PDF);
		headers.setAccept(list);
		headers.add("Authorization", "Bearer "+token);
		
		HttpEntity<String> requestEntity = new HttpEntity<String>(headers);
		Thread.sleep(1000);
		restTemplate = restTemplBuilder.build();
		ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, byte[].class);
		String files = response.getHeaders().getContentDisposition().getFilename();
		Files.write(Paths.get(System.getProperty("user.dir") + "/Cert_" + benRefId + ".pdf"), response.getBody());//Working
		
		File src = new File(System.getProperty("user.dir") + "/Cert_" + benRefId + ".pdf");
		logger.info("CowinService.downloadCertificate(), src file: " + src.getAbsolutePath());
		
		File dest = new File(System.getProperty("user.dir") + "/target/classes/static/Cert_" + benRefId + ".pdf");
		logger.info("CowinService.downloadCertificate(), dest file: " + dest.getAbsolutePath());
		
		FileUtils.copyFile(src,dest);
		logger.info("CowinService.downloadCertificate(), is file copied to destination? --> " + Files.exists(Paths.get(System.getProperty("user.dir") + "/target/classes/static/Cert_" + benRefId + ".pdf")));
		
		// Delete file from src
		FileUtils.forceDelete(src);
		logger.info("CowinService.downloadCertificate(), is source file deleted? --> " + Files.exists(Paths.get(System.getProperty("user.dir") + "/Cert_" + benRefId + ".pdf")));
		
		logger.info("files--" + files);
		
	}

}
