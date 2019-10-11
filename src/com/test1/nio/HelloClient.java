package com.test1.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;


/**
 * 客户端
 *
 *
 */
public class HelloClient {  
	  
    private InetSocketAddress ip = new InetSocketAddress("localhost", 8888);
    private CharsetDecoder decoder = Charset.forName("GB2312").newDecoder();  
    private CharsetEncoder encoder = Charset.forName("GB2312").newEncoder();  
    
     class Message implements Runnable {  
    	 private String name;  
    	 private String msg = "";  
  
        public Message(String name) {  
            this.name = name;  
        }  
  
        public void run() {  
            try {  
                long start = System.currentTimeMillis();  
                //打开SocketChannel通道  
                SocketChannel client = SocketChannel.open();  
                //设置为非阻塞模式  
                client.configureBlocking(false);  
                // 创建多路复用器  
                Selector selector = Selector.open();  
                //注册连接就绪状态
                client.register(selector, SelectionKey.OP_CONNECT);  
                //去连接  
                client.connect(ip);  
                //创建读取的缓冲区 
                ByteBuffer buffer = ByteBuffer.allocate(1024);  
               
                _FOR: while(true) {  
                    selector.select(); 
                    // 获得selectedKey集合的迭代器
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();  
  
                    while (iter.hasNext()) {  
                        SelectionKey key =  iter.next();
                        // 必须手工移除,以防重复处理
                        iter.remove();  
                        if (key.isConnectable()) { // 连接事件监听   
                            SocketChannel channel = (SocketChannel) key.channel();
                            // 如果正在连接，则完成连接
                            if (channel.isConnectionPending())  
                                channel.finishConnect();
                            //向服务器发送数据
                            System.out.println("向服务端发送消息："+name);
                            channel.write(encoder.encode(CharBuffer.wrap(name))); 
                            //在和服务端连接成功之后，为了可以接收到服务端的信息，需要给通道设置读就绪状态。 
                            channel.register(selector, SelectionKey.OP_READ);  
                        } else if (key.isReadable()) {  // 可读事件监听  
                            SocketChannel channel = (SocketChannel) key.channel();
                            //从Channel写到Buffer
                            int count = channel.read(buffer);  
                            if (count > 0) {  
                              
                                //将Buffer从写模式切换到读模式
                                buffer.flip();  
                                //返回当前位置与上限之间的元素个数，即返回缓冲区中的剩余元素数
                                /*while (buffer.remaining() > 0) {  
                                    byte b = buffer.get();  
                                    msg += (char) b;  
                                      
                                } */ 
                                CharBuffer charBuffer = decoder.decode(buffer);  
                                msg = charBuffer.toString(); 
                                System.out.println("收到服务端信息："+msg);
                                //清空整个缓存,compact()方法只会清除已经读过的数据
                                buffer.clear();  
                            } else {
                            	System.out.println(name+"没数据可读，关闭客户端");
                                client.close();  
                                break _FOR;  
                            }  
                        }  
                    } 
                    
                }  
                double last = (System.currentTimeMillis() - start) * 1.0 / 1000;  
                System.out.println(name+"使用时间 :" + last + "s.");  
                msg = "";  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }  
  
    public static void main(String[] args) throws IOException {  
      
        HelloClient cli = new HelloClient();
        for (int index = 0; index < 10; index++) {
            System.out.println("开启客户端"+index);
            new Thread(cli.new Message("client[" + index + "]")).start();  
        }  
      
    }  
}  
