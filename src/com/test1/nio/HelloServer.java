package com.test1.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;

/**
 * 服务端
 * 
 *
 */
public class HelloServer {  
	 
    private String name = "";  
    private Selector selector;
    //开出一块1024字节的缓冲区
    private ByteBuffer buffer = ByteBuffer.allocate(1024);  
    private CharsetDecoder decoder = Charset.forName("GB2312").newDecoder();  
    private CharsetEncoder encoder = Charset.forName("GB2312").newEncoder();  
  
    public HelloServer(int port) throws IOException {  
        selector = this.getSelector(port);  
        
    }  
 
     
    private Selector getSelector(int port) throws IOException {
    	 //获得一个ServerSocketChannel通道  
        ServerSocketChannel server = ServerSocketChannel.open();
        // 设置通道为非阻塞
        server.configureBlocking(false); 
        // 绑定端口 
        server.socket().bind(new InetSocketAddress(port));  
        
        // 创建多路复用器
        Selector sel = Selector.open();  
        //将该通道绑定到Selector，并为该通道注册SelectionKey.OP_ACCEPT事件(监听可连接事件)  
        //当该事件到达时，selector.select()会返回，如果该事件没到达selector.select()会一直阻塞。  
        server.register(sel, SelectionKey.OP_ACCEPT);  
        return sel;  
    }  
    
    public void listen() {
    	System.out.println("服务端启动成功！"); 
        try {  
            while(true) {
            	//当注册的事件到达时，方法返回；否则,该方法会一直阻塞 
                selector.select();
                // 获得selectedKey集合的迭代器
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();  
                while (iter.hasNext()) {  
                    SelectionKey key =  iter.next();
                    // 必须手工移除,以防重复处理
                    iter.remove();
                    //处理
                    process(key);  
                }  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  

    private void process(SelectionKey key) throws IOException {  
        if (key.isAcceptable()) { // 接收请求  
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            // 获得和客户端连接的通道 
            SocketChannel channel = server.accept();  
            //设置非阻塞模式  
            channel.configureBlocking(false);
            //在和客户端连接成功之后，为了可以接收到客户端的信息，需要给通道设置读就绪状态。
            channel.register(selector, SelectionKey.OP_READ);  
        } else if (key.isReadable()) { // 读信息
        	 // 服务器可读取消息，得到事件发生的SocketChannel通道 
            SocketChannel channel = (SocketChannel) key.channel();
            //从Channel写到Buffer
            int count = channel.read(buffer);  
            if (count > 0) {
            	//将Buffer从写模式切换到读模式
            	buffer.flip();
                //解码
                CharBuffer charBuffer = decoder.decode(buffer);  
                name = charBuffer.toString();  
                System.out.println("收到客户端信息："+name);
                //给通道设置写就绪
                SelectionKey sKey = channel.register(selector,  
                        SelectionKey.OP_WRITE);  
                sKey.attach(name);//附加name数据  
            } else {  
                channel.close();  
            }  
            //清空缓冲区
            buffer.clear();  
        } else if (key.isWritable()) { // 写事件  
            SocketChannel channel = (SocketChannel) key.channel();
            //获取附加的name
            String name = (String) key.attachment();
            System.out.println("向客户端发送消息："+"Hello! " + name);
            // 编码
            ByteBuffer block = encoder.encode(CharBuffer.wrap("Hello! " + name));  
              
            //从Buffer读取数据到Channel
            channel.write(block);  
            //关闭客户端通道
            channel.close();  
  
        }  
    }  
  
    public static void main(String[] args) throws IOException {  
    	//单线程 管理多个SocketChannel通道
        HelloServer server = new HelloServer(8888);  
          
        server.listen();  
       
    }  
} 
