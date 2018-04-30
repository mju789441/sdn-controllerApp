package com.nculab.kuoweilun.sdncontrollerapp.controller;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.nculab.kuoweilun.sdncontrollerapp.host.Host;
import com.nculab.kuoweilun.sdncontrollerapp.host.HostAdapter;
import com.nculab.kuoweilun.sdncontrollerapp.switcher.Switch;
import com.nculab.kuoweilun.sdncontrollerapp.switcher.SwitchAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Kuo Wei Lun on 2017/11/6.
 */

public class ControllerSocket {

    //Componenet
    private boolean rsa_switch = false;
    public String URL = "140.115.204.156";
    private int port = 9487;
    public String status = "未連線";
    private Context context = null;

    private ControllerAdapter controllerAdapter = null;
    //switch
    private SwitchAdapter switchAdapter = null;
    public ArrayList<Switch> switchArrayList = null;
    public ArrayList<String> switchIDArrayList = null;
    //host
    private HostAdapter hostAdapter = null;
    public ArrayList<Host> hostArrayList = null;
    public ArrayList<String> hostPortArrayList = null;
    //topology
    private boolean forTopology = false;
    public boolean threadComplete = true;
    //Socket
    private Socket socket = null;
    private BufferedReader reader = null;
    private PrintStream writer = null;
    //Rsa
    private RSA rsa = null;
    //component handler
    private Handler handler = new Handler();
    //Thread
    public Thread thread_connect = null;
    public Thread thread_getSwitch = null;
    public Thread thread_getHost = null;

    public ControllerSocket(String URL, Context context) {
        this.URL = URL;
        this.context = context;
        setThread();
    }

    public ControllerSocket(String URL, Context context, ControllerAdapter adapter) {
        this.URL = URL;
        this.context = context;
        controllerAdapter = adapter;
        setThread();
    }

    public ControllerSocket(String URL, Context context, ArrayList<Switch> list, SwitchAdapter adapter) {
        this.URL = URL;
        this.context = context;
        switchArrayList = list;
        switchAdapter = adapter;
        setThread();
        setSwitchThread();
    }

    public ControllerSocket(String URL, Context context, ArrayList<Host> list, HostAdapter adapter) {
        this.URL = URL;
        this.context = context;
        hostArrayList = list;
        hostAdapter = adapter;
        setThread();
        setHostThread();
    }

    public ControllerSocket(String URL, Context context, ArrayList<Switch> switchArrayList, ArrayList<Host> hostArrayList) {
        this.URL = URL;
        this.context = context;
        this.switchArrayList = switchArrayList;
        this.hostArrayList = hostArrayList;
        forTopology = true;
        setThread();
        setSwitchThread();
        setHostThread();
    }

    private void setThread() {
        thread_connect = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //已連線時拋出例外
                    if (isConnected()) {
                        throw new InterruptedException();
                    }
                    //連線
                    setStatus("連線中");
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(URL, port));
                    writer = new PrintStream(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    if (rsa_switch) {
                        rsa = new RSA();
                        //送出MyPublicKey
                        final String str = rsa.getMyPublicKey();
                        sendMsg(str);
                        //接收對方的PublicKey
                        final String key = getMsg();
                        if (key == null) {
                            throw new Exception();
                        }
                        rsa.setPublicKey(key);
                    }
                    setStatus("已連線");
                } catch (IOException e) {
                    e.printStackTrace();
                    disconnection();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (final Exception e) {
                    e.printStackTrace();
                    disconnection();
                }
            }
        });
    }

    private void setSwitchThread() {
        switchIDArrayList = new ArrayList<String>();
        thread_getSwitch = new Thread(new Runnable() {
            @Override
            public void run() {
//                while (true) {
//                    try {
//                        //等待連線
//                        while (true) {
//                            if (isConnected()) {
//                                break;
//                            }
//                            if (failConnected()) {
//                                Toast.makeText(context, "斷線", Toast.LENGTH_SHORT).show();
//                                throw new Exception();
//                            }
//                        }
//                        //傳送請求
//                        sendEncryptedMsg("GET switch -ID -bytes");
//                        //接收回復
//                        final String msg = getDncryptedMsg();
//                        if (msg == null) {
//                            throw new Exception();
//                        }
//                        String[] temp = msg.split("\n");
//                        if (temp.length > 2 && temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
//                            boolean switchChanged = false;
//                            if (switchArrayList.size() > temp.length - 2) {
//                                switchChanged = true;
//                                for (int i = temp.length - 2; i < switchArrayList.size(); i++) {
//                                    switchArrayList.remove(i);
//                                    switchIDArrayList.remove(i);
//                                }
//                            }
//                            for (int i = 1; i < temp.length - 1; i++) {
//                                final String[] temp2 = temp[i].split(" ");
//                                if (switchArrayList.size() < temp.length - 2) {
//                                    switchChanged = true;
//                                    switchArrayList.add(new Switch(temp2[0], temp2[1]));
//                                    switchIDArrayList.add(temp2[0]);
//                                } else if (!switchArrayList.get(i - 1).equals(new Switch(temp2[0], temp2[1]))) {
//                                    switchChanged = true;
//                                    switchArrayList.set(i - 1, new Switch(temp2[0], temp2[1]));
//                                    switchIDArrayList.set(i - 1, temp2[0]);
//                                }
//                            }
//                            if (switchAdapter != null && switchChanged) {
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        switchAdapter.notifyDataSetChanged();
//                                    }
//                                });
//                            }
//                        } else {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(context, "取得資料錯誤", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                            throw new Exception();
//                        }
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        break;
//                    } catch (final Exception e) {
//                        e.printStackTrace();
//                        disconnection();
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        break;
//                    }
//                }
            }
        });
    }

    private void setHostThread() {
        hostPortArrayList = new ArrayList<String>();
        thread_getHost = new Thread(new Runnable() {
            @Override
            public void run() {
//                while (true) {
//                    try {
//                        //等待連線
//                        while (true) {
//                            if (isConnected()) {
//                                break;
//                            }
//                            if (failConnected()) {
//                                Toast.makeText(context, "斷線", Toast.LENGTH_SHORT).show();
//                                throw new Exception();
//                            }
//                        }
//                        //傳送請求
//                        sendEncryptedMsg("GET /v1.0/topology/hosts/");
//                        //接收回復
//                        final String msg = getDncryptedMsg();
//                        if (msg == null) {
//                            throw new Exception();
//                        }
//                        String[] temp = msg.split("\n");
//                        if (temp.length > 2 && temp[0].equals("host") && temp[temp.length - 1].equals("/host")) {
//                            boolean hostChanged = false;
//                            if (hostArrayList.size() > temp.length - 2) {
//                                hostChanged = true;
//                                for (int i = temp.length - 2; i < hostArrayList.size(); i++) {
//                                    hostArrayList.remove(i);
//                                    hostPortArrayList.remove(i);
//                                }
//                            }
//                            for (int i = 1; i < temp.length - 1; i++) {
//                                final String[] temp2 = temp[i].split(" ");
//                                if (hostArrayList.size() < temp.length - 2) {
//                                    hostChanged = true;
//                                    hostArrayList.add(new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
//                                    hostPortArrayList.add(temp2[1]);
//                                } else if (!hostArrayList.get(i - 1).equals(new Host(temp2[0], temp2[1], temp2[2], temp2[3]))) {
//                                    hostChanged = true;
//                                    hostArrayList.set(i - 1, new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
//                                    hostPortArrayList.set(i - 1, temp2[1]);
//                                }
//                            }
//                            if (hostAdapter != null && hostChanged) {
//                                handler.post(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        hostAdapter.notifyDataSetChanged();
//                                    }
//                                });
//                            }
//                        } else {
//                            handler.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toast.makeText(context, "取得資料錯誤", Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                            throw new Exception();
//                        }
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        break;
//                    } catch (final Exception e) {
//                        e.printStackTrace();
//                        disconnection();
//                        if (forTopology) {
//                            threadComplete = true;
//                            break;
//                        }
//                        break;
//                    }
//                }
            }
        });
    }

    public void ban(final Host host) {
//        if (host.IP != "None") {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    thread_getHost.interrupt();
//                    sendEncryptedMsg("POST /ban/" + host.IP);
//                    try {
//                        getMsg();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    thread_getHost.start();
//                }
//            }).start();
//        }
    }

    public void unban(final Host host) {
//        if (host.IP != "None") {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    thread_getHost.interrupt();
//                    sendEncryptedMsg("POST /unban/" + host.IP);
//                    try {
//                        getMsg();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    thread_getHost.start();
//                }
//            }).start();
//        }
    }

    public void getSwitchArrayList() {
        try {
//            //等待連線
//            while (true) {
//                if (isConnected()) {
//                    break;
//                }
//                if (failConnected()) {
//                    Toast.makeText(context, "斷線", Toast.LENGTH_SHORT).show();
//                    throw new Exception();
//                }
//            }
//            //傳送請求
//            sendEncryptedMsg("GET switch -ID -bytes");
//            //接收回復
//            final String msg = getDncryptedMsg();
//            if (msg == null) {
//                throw new Exception();
//            }
//            String[] temp = msg.split("\n");
//            if (temp.length > 2 && temp[0].equals("switch_speed") && temp[temp.length - 1].equals("/switch_speed")) {
//                boolean switchChanged = false;
//                if (switchArrayList.size() > temp.length - 2) {
//                    switchChanged = true;
//                    for (int i = temp.length - 2; i < switchArrayList.size(); i++) {
//                        switchArrayList.remove(i);
//                        switchIDArrayList.remove(i);
//                    }
//                }
//                for (int i = 1; i < temp.length - 1; i++) {
//                    final String[] temp2 = temp[i].split(" ");
//                    if (switchArrayList.size() < temp.length - 2) {
//                        switchChanged = true;
//                        switchArrayList.add(new Switch(temp2[0], temp2[1]));
//                        switchIDArrayList.add(temp2[0]);
//                    } else if (!switchArrayList.get(i - 1).equals(new Switch(temp2[0], temp2[1]))) {
//                        switchChanged = true;
//                        switchArrayList.set(i - 1, new Switch(temp2[0], temp2[1]));
//                        switchIDArrayList.set(i - 1, temp2[0]);
//                    }
//                }
//                if (switchAdapter != null && switchChanged) {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            switchAdapter.notifyDataSetChanged();
//                        }
//                    });
//                }
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "取得資料錯誤", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                throw new Exception();
//            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
        }
    }

    public void getHostArrayList() {
        try {
//            //等待連線
//            while (true) {
//                if (isConnected()) {
//                    break;
//                }
//                if (failConnected()) {
//                    Toast.makeText(context, "斷線", Toast.LENGTH_SHORT).show();
//                    throw new Exception();
//                }
//            }
//            //傳送請求
//            sendEncryptedMsg("GET /v1.0/topology/hosts/");
//            //接收回復
//            final String msg = getDncryptedMsg();
//            if (msg == null) {
//                throw new Exception();
//            }
//            String[] temp = msg.split("\n");
//            if (temp.length > 2 && temp[0].equals("host") && temp[temp.length - 1].equals("/host")) {
//                boolean hostChanged = false;
//                if (hostArrayList.size() > temp.length - 2) {
//                    hostChanged = true;
//                    for (int i = temp.length - 2; i < hostArrayList.size(); i++) {
//                        hostArrayList.remove(i);
//                        hostPortArrayList.remove(i);
//                    }
//                }
//                for (int i = 1; i < temp.length - 1; i++) {
//                    final String[] temp2 = temp[i].split(" ");
//                    if (hostArrayList.size() < temp.length - 2) {
//                        hostChanged = true;
//                        hostArrayList.add(new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
//                        hostPortArrayList.add(temp2[1]);
//                    } else if (!hostArrayList.get(i - 1).equals(new Host(temp2[0], temp2[1], temp2[2], temp2[3]))) {
//                        hostChanged = true;
//                        hostArrayList.set(i - 1, new Host(temp2[0], temp2[1], temp2[2], temp2[3]));
//                        hostPortArrayList.set(i - 1, temp2[1]);
//                    }
//                }
//                if (hostAdapter != null && hostChanged) {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            hostAdapter.notifyDataSetChanged();
//                        }
//                    });
//                }
//            } else {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, "取得資料錯誤", Toast.LENGTH_SHORT).show();
//                    }
//                });
//                throw new Exception();
//            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
        }
    }

    public void setStatus(final String status) {
        this.status = status;
        if (controllerAdapter != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    controllerAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public boolean isConnected() {
        if (status == "已連線") {
            return true;
        }
        return false;
    }

    public boolean failConnected() {
        if (status == "斷線") {
            return true;
        }
        return false;
    }

    public void disconnection() {
        setStatus("斷線");
        close();
    }

    public void reset() {
        setStatus("未連線");
        close();
    }

    public void close() {
        if (socket != null) {
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getDncryptedMsg() {
        try {
            String msg = getMsg();
            if (msg == null) {
                return null;
            }
            if (!rsa_switch) {
                String temp;
                //由於一次只能讀到"\n"所以要讀入多行。字串包含"/"代表最後一行
                for (; !(temp = getMsg()).contains("/"); ) {
                    msg += '\n' + temp;
                }
                msg += '\n' + temp;
                return msg;
            }
            return rsa.decrypt(msg.getBytes());
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "未能接收訊息", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }
    }

    public void sendEncryptedMsg(final String instruction) {
        try {
            if (!rsa_switch) {
                sendMsg(instruction);
            } else {
                sendMsg(rsa.encrypt(instruction.getBytes()));
            }
        } catch (final Exception e) {
            e.printStackTrace();
            disconnection();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "未能傳送訊息", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public String getMsg() throws IOException {
        return reader.readLine();
    }

    public void sendMsg(String msg) throws IOException {
        writer.println(msg);
    }

}
