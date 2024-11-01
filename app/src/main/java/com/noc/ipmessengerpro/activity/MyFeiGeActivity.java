package com.noc.ipmessengerpro.activity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.conn.util.InetAddressUtils;
import com.noc.ipmessengerpro.R;
import com.noc.ipmessengerpro.adapter.UserExpandableListAdapter;
import com.noc.ipmessengerpro.data.ChatMessage;
import com.noc.ipmessengerpro.data.User;
import com.noc.ipmessengerpro.utils.IpMessageConst;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyFeiGeActivity extends MyFeiGeBaseActivity implements OnClickListener{
	public static String hostIp;
	private ExpandableListView userList;
	
	private UserExpandableListAdapter adapter;
	private List<String> strGroups;
	private List<List<User>> children;
	
	private TextView totalUser;
	private Button refreshButton;
	private TextView ipTextView;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(!isWifiActive()){
        	Toast.makeText(this, R.string.no_wifi, Toast.LENGTH_LONG).show();
        }
        
        
        findViews();

		strGroups = new ArrayList<String>();
		children = new ArrayList<List<User>>();
        
//        netThreadHelper = NetThreadHelper.newInstance();
        netThreadHelper.connectSocket();
        netThreadHelper.noticeOnline();
        
        adapter = new UserExpandableListAdapter(this, strGroups, children);
        userList.setAdapter(adapter);
        
        refreshButton.setOnClickListener(this);
        refreshViews();
    }
    
	@Override
	public void finish() {
		super.finish();
		netThreadHelper.noticeOffline();
		netThreadHelper.disconnectSocket();
		
	}



	private void findViews() {
		totalUser = findViewById(R.id.totalUser);
		userList = findViewById(R.id.userlist);
		refreshButton = findViewById(R.id.refresh);
		ipTextView = findViewById(R.id.mymood);
		hostIp = getLocalIpAddress();
		ipTextView.setText(hostIp);
	}


	@Override
	public void processMessage(Message msg) {
		switch(msg.what){
		case IpMessageConst.IPMSG_BR_ENTRY:
		case IpMessageConst.IPMSG_BR_EXIT:
		case IpMessageConst.IPMSG_ANSENTRY:
		case IpMessageConst.IPMSG_SENDMSG:
			refreshViews();	
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			exit();
		return true;
	}
	
	private void refreshViews(){
		strGroups.clear();
		children.clear();
		
		Map<String,User> currentUsers = new HashMap<String, User>();
		currentUsers.putAll(netThreadHelper.getUsers());
		Queue<ChatMessage> msgQueue = netThreadHelper.getReceiveMsgQueue();
		Map<String, Integer> ip2Msg = new HashMap<String, Integer>();
		Iterator<ChatMessage> it = msgQueue.iterator();
		while(it.hasNext()){
			ChatMessage chatMsg = it.next();
			String ip = chatMsg.getSenderIp();
			Integer tempInt = ip2Msg.get(ip);
			if(tempInt == null){
				ip2Msg.put(ip, 1);
			}else{
				ip2Msg.put(ip, ip2Msg.get(ip)+1);
			}
		}
		
		Iterator<String> iterator = currentUsers.keySet().iterator();
		while (iterator.hasNext()) {
			User user = currentUsers.get(iterator.next());	
			if(ip2Msg.get(user.getIp()) == null){
				user.setMsgCount(0);
			}else{
				user.setMsgCount(ip2Msg.get(user.getIp()));
			}
			
			String groupName = user.getGroupName();
			int index = strGroups.indexOf(groupName);
			if(index == -1){
				strGroups.add(groupName);
//				List<Map<String,String>> childData = new ArrayList<Map<String,String>>();
//				Map<String, String> child = new HashMap<String,String>();
//				child.put("userName", user.getUserName());
//				childData.add(child);
//				children.add(childData);
				
				List<User> childData = new ArrayList<User>();
				childData.add(user);
				children.add(childData);
			}else{
//				Map<String,String> child = new HashMap<String,String>();
//				child.put("userName", user.getUserName());
//				children.get(index).add(child);
				children.get(index).add(user);
			}
			
		}
		
//		for(int i = 0; i < strGroups.size(); i++){
//			Map<String,String> groupMap = new HashMap<String,String>();
//			groupMap.put("group", strGroups.get(i));
//			groups.add(groupMap);
//		}
		
		
		adapter.notifyDataSetChanged();
		
		String countStr = "当前在线" + currentUsers.size() +"个用户";
        totalUser.setText(countStr);
		
	}

	@Override
	public void onClick(View v) {
		if(v.equals(refreshButton)){
			netThreadHelper.refreshUsers();
			refreshViews();
		}
	
	}
	
	public boolean isWifiActive(){
		ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if(mConnectivity != null){
			NetworkInfo[] infos = mConnectivity.getAllNetworkInfo();
			if(infos != null){
				for(NetworkInfo ni: infos){
					if("WIFI".equals(ni.getTypeName()) && ni.isConnected())
						return true;
				}
			}
		}
		
		return false;
	}
	
	public String getLocalIpAddress(){
		try{
			Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
			while(en.hasMoreElements()){
				NetworkInterface nif = en.nextElement();
				Enumeration<InetAddress> enumIpAddr = nif.getInetAddresses();
				while(enumIpAddr.hasMoreElements()){
					InetAddress mInetAddress = enumIpAddr.nextElement();
					if(!mInetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(mInetAddress.getHostAddress())){
						return mInetAddress.getHostAddress();
					}
				}
			}
		}catch(SocketException ex){
			Log.e("MyFeiGeActivity", "获取本地IP地址失败");
		}
		
		return null;
	}
	
	public String getLocalMacAddress(){
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}
}