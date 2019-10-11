package com.test1.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class AIOTest {
	public static void main(String[] args) throws IOException {
		AIOTest test  = new AIOTest();
		test.writeTest("d:/1.txt");
		test.readTest("d:/1.txt");
	}
	
	public void readTest(String filename) throws IOException{
		Path path = Paths.get(filename);
		AsynchronousFileChannel fileChannel = 
			    AsynchronousFileChannel.open(path, StandardOpenOption.READ);

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int position = 0;

		Future<Integer> operation = fileChannel.read(buffer, position);
		//是否操作完成
		while(!operation.isDone());

		buffer.flip();
		byte[] data = new byte[buffer.limit()];
		buffer.get(data);
		System.out.println(new String(data));
		buffer.clear();
	}
	public void writeTest(String filename) throws IOException{
		Path path = Paths.get(filename);
		if(!Files.exists(path)){
		    Files.createFile(path);
		}
		AsynchronousFileChannel fileChannel = 
		    AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int position = 0;
		//是否操作完成
		buffer.put("112345678".getBytes());
		buffer.flip();
		
		Future<Integer> operation = fileChannel.write(buffer, position);
		buffer.clear();
		
		while(!operation.isDone());

		System.out.println("Write done");
	}
}
