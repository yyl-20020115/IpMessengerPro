package com.noc.ipmessengerpro.adapter;

import java.util.ArrayList;
import java.util.List;

import com.noc.ipmessengerpro.R;
import com.noc.ipmessengerpro.activity.MyFeiGeChatActivity;
import com.noc.ipmessengerpro.data.User;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 主界面用户二级列表的adapter
 * 
 * @author ccf
 * 
 * V1.0 2012/2/17
 *
 */
public class UserExpandableListAdapter extends BaseExpandableListAdapter {
	private final Context context;	//父activity
	protected Resources res;
	private final LayoutInflater mChildInflater;	//用于加载分组的布局xml
	private final LayoutInflater mGroupInflater;	//用于加载对应分组用户的布局xml
	List<String> groups = new ArrayList<String>();
	List<List<User>> children = new ArrayList<List<User>>();
	
	public UserExpandableListAdapter(Context c,List<String> groups,List<List<User>> children){
		mChildInflater = LayoutInflater.from(c);
		mGroupInflater = LayoutInflater.from(c);
		this.groups = groups;
		this.children = children;
		context = c;
		res = c.getResources();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) { //根据组索引和item索引，取得listitem
		return children.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) { //返回item索引
		return childPosition;
	}

	//分组视图
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View myView = mChildInflater.inflate(R.layout.children, null);
		if(groups == null || groups.size() == 0 || children == null || children.size() == 0){
			return myView;
		}
		final User user = children.get(groupPosition).get(childPosition);
		
		TextView childTv = myView.findViewById(R.id.child_name);
		TextView childIp = myView.findViewById(R.id.child_ip);
		final TextView childInfoNo = myView.findViewById(R.id.child_infos);
		ImageView childImg = myView.findViewById(R.id.user_img);
		childTv.setText(user.getUserName());	//用户名显示
		childIp.setText(user.getIp());	//IP显示
		childImg.setImageDrawable(res.getDrawable(R.drawable.ic_launcher));
		if(user.getMsgCount() == 0){	//若没有未接收的消息，则不显示
			childInfoNo.setVisibility(View.GONE);
		}else{
			childInfoNo.setText("" + user.getMsgCount());
		}
		
		
		myView.setOnClickListener(new View.OnClickListener(){	//点击子项
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(context, MyFeiGeChatActivity.class);
				intent.putExtra("receiverName", user.getUserName());
				intent.putExtra("receiverIp", user.getIp());
				intent.putExtra("receiverGroup", user.getGroupName());
				
				childInfoNo.setVisibility(View.GONE);
				user.setMsgCount(0);
				
				context.startActivity(intent);
			}
		});
		return myView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {//根据组索引返回分组的子item数
		return children.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) { //根据组索引返回组
		return children.get(groupPosition);
	}

	@Override
	public int getGroupCount() { //返回分组数
		return groups.size();
	}

	@Override
	public long getGroupId(int groupPosition) { //返回分组索引
		return groupPosition;
	}

	//组视图
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View myView = mGroupInflater.inflate(R.layout.groups, null);
		if(groups == null || groups.size() == 0 || children == null || children.size() == 0){
			return myView;
		}
		
		//一级菜单收放状态对应图标设置
		ImageView groupImg = myView.findViewById(R.id.group_img);
		if(isExpanded)
			groupImg.setImageDrawable(res.getDrawable(R.drawable.group_exp));
		else
			groupImg.setImageDrawable(res.getDrawable(R.drawable.group_notexp));
		
		//设置文本内容
		TextView groupTv = myView.findViewById(R.id.group);
		groupTv.setText(groups.get(groupPosition));
		TextView groupOnLine = myView.findViewById(R.id.group_onlinenum);
		groupOnLine.setText("[" + getChildrenCount(groupPosition) + "]");
		
		return myView;
	}

	@Override
	public boolean hasStableIds() { //行是否具有唯一id
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) { //行是否可选
		return false;
	}
}
