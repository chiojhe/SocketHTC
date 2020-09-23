package com.jeff.sockethtc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import android.os.Handler;
import android.os.Message;




public class MainActivity extends AppCompatActivity {


    /**
     * 主 变量
     */

    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    // Socket变量
    private Socket socket;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is;

    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;

    // 接收服务器发送过来的消息
    String response;

    //接收執行續傳過來的資料，用TextView顯示

    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    OutputStream outputStream;

    /**
     * 按钮 变量
     */

    // 连接 断开连接 发送数据到服务器 的按钮变量
    private Button btnConnect, btnDisconnect, btnSend;

    // 显示接收服务器消息 按钮
    private TextView Receive,receive_message;

    // 输入需要发送的消息 输入框
    private EditText mEdit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /**
         * 初始化操作
         */
        Thread Thread1 = null;
        // 初始化所有按钮
        btnConnect = (Button) findViewById(R.id.connect);
        btnDisconnect = (Button) findViewById(R.id.disconnect);
        btnSend = (Button) findViewById(R.id.send);
        mEdit = (EditText) findViewById(R.id.edit);
        receive_message = (TextView) findViewById(R.id.receive_message);
        receive_message.setMovementMethod(ScrollingMovementMethod.getInstance());
        //Receive = (Button) findViewById(R.id.Receive);

        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();


        // 实例化主线程,用于更新接收过来的消息
        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        receive_message.setText(response);
                        break;
                }
            }
        };


        /**
         * 创建客户端 & 服务器的连接
         */
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            // 创建Socket对象 & 指定服务端的IP 及 端口号
                            socket = new Socket("192.168.1.104", 30001);

                            // 判断客户端和服务器是否连接成功
                            System.out.println(socket.isConnected());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            outputStream = socket.getOutputStream();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final byte[] buffer = new byte[8192];//创建接收缓冲区
                        try {
                            while (true) {
                                InputStream inputStream = socket.getInputStream();
                                final int len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                                {
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        receive_message.append(new String(buffer, 0, len) + "\r\n");
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                });

            }
        });




        /*mThreadPool.execute(new Runnable() {

                                @Override
                                public void run() {
                                    final byte[] buffer = new byte[8192];
                                    try {
                                        while (true) {
                                            InputStream inputStream = socket.getInputStream();
                                            final int len = inputStream.read(buffer);//数据读出来，并且返回数据的长度
                                            runOnUiThread(new Runnable()//不允许其他线程直接操作组件，用提供的此方法可以
                                            {
                                                public void run() {
                                                    // TODO Auto-generated method stub
                                                    receive_message.append(new String(buffer, 0, len) + "\r\n");
                                                }
                                            });
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });*/


        /**
         * 发送消息 给 服务器
         */
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 利用线程池直接开启一个线程 & 执行该线程
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // 步骤1：从Socket 获得输出流对象OutputStream
                            // 该对象作用：发送数据
                            outputStream = socket.getOutputStream();

                            // 步骤2：写入需要发送的数据到输出流对象中
                            outputStream.write((mEdit.getText().toString() + "\n").getBytes("utf-8"));
                            // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                            // 步骤3：发送数据到服务端
                            outputStream.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                });

            }
        });


        /**
         * 断开客户端 & 服务器的连接
         */
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    // 斷開 客戶端傳送到伺服器 的連線，即關閉輸出流物件OutputStream
                    outputStream.close();

                    // 斷開 伺服器傳送到客戶端 的連線，即關閉輸入流讀取器物件BufferedReader
                    //br.close();

                    // 最終關閉整個Socket連線
                    socket.close();

                    // 判斷客戶端和伺服器是否已經斷開連線
                    //System.out.println(socket.isConnected());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });


    }

}
