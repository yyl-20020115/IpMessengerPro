package com.noc.ipmessengerpro.interfaces;

import com.noc.ipmessengerpro.data.ChatMessage;

/**
 * 接收消息监听的listener接口
 * @author ccf
 *
 */
public interface ReceiveMsgListener {
	boolean receive(ChatMessage msg);

}
