package com.atguigu.gmall.manage;


import org.csource.common.MyException;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class GmallManageWebApplicationTests {

	@Test
	void contextLoads() {
	}

//	@Test
//	public void textFileUpload() throws IOException{
//		String file = this.getClass().getResource("/tracker.conf").getFile();
//		ClientGlobal.init(file);
//		TrackerClient trackerClient=new TrackerClient();
//		TrackerServer trackerServer=trackerClient.getConnection();
//		StorageClient storageClient=new StorageClient(trackerServer,null);
//		String orginalFilename="d://001.jpg";
//		String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
//		for (int i = 0; i < upload_file.length; i++) {
//			String s = upload_file[i];
//			System.out.println("s = " + s);
//		}
//	}

}
