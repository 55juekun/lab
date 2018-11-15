package com.firstspringboot.spring.socket;

import javax.net.SocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @ProjectName: springboot
 * @Package: com.firstspringboot.spring.socket
 * @ClassName: MySockt
 * @Description: 自定义socket的调用相关方法
 * @Author: 55珏坤
 * @CreateDate: 2018/11/12 16:52
 * @Version: 1.0
 */
public class MySocket {
    static ServerSocket serverSocket= null;
    static Hashtable<Integer,Socket> list=new Hashtable<>();

    public static void begin(){
        try {
            serverSocket = new ServerSocket(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("服务端已启动，等待客户端连接..");
        int x=0;
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                socket.setKeepAlive(true);
                x++;
                System.out.println(list.size());

                InputStream in=socket.getInputStream();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
                String info="";
                OutputStream out=socket.getOutputStream();
                final PrintWriter writer=new PrintWriter(out);
                System.out.println("已接收到客户端连接");
                String temp = new String();
                temp=bufferedReader.readLine();
                int id=Integer.parseInt(temp);
                list.put(id,socket);
            }catch (Exception i){
                i.printStackTrace();
            }
        }
    }
    public static void sendMsgToClient(int id,String word){
        Socket s=list.get(id);
        System.out.println("容量大小是"+list.size());
        if (s.isClosed()) {
            System.out.println("没有");
            return;
        }
        try {
            OutputStream out=s.getOutputStream();
            PrintWriter writer=new PrintWriter(out);
            writer.write(word);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
