package org.gedanken.farley

/**
  * 
  * Xmpp.scala
  * 
  * Copyright 2013 Logan O'Sullivan Bruns
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *  http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

import akka.actor._
import com.typesafe.scalalogging.LazyLogging
import java.io._
import java.util.Properties
import org.gedanken.farley.parser.Parser
import org.jivesoftware.smack._
import org.jivesoftware.smack.chat._
import org.jivesoftware.smack.java7._
import org.jivesoftware.smack.packet._
import org.jivesoftware.smack.tcp._
import scala.collection.mutable.HashSet

object Xmpp extends LazyLogging {
  var connection : AbstractXMPPConnection = null
  var chats = new HashSet[Chat]
  val parser = new Parser(
    "models/en-sent.bin",
    "models/en-parser-chunking.bin",
    "/var/farley/tdb"
  );
  val system = ActorSystem("XmppSystem")

  class Context(userchat: Chat) extends Actor with ActorLogging {
    val chat = userchat

    def receive = {
      case org.gedanken.farley.parser.Message(message) =>
	chat.sendMessage(message)
    }
  }

  def createListener(context: ActorRef) : ChatMessageListener = {
    new ChatMessageListener() {
      override def processMessage(chat: Chat, message: Message) {
        val body = message.getBody
        if (body != null)
          chat.sendMessage(parser.process(message.getBody(), context))
        else {
          logger.info("Ignoring control message.")

          chat.sendMessage("Hello, I'm " + id + " and I'm still ready to help you now.")
        }
      }
    }
  }

  val props = new Properties();
  props.load(new FileInputStream(new File(System.getProperty("user.home"),
    ".farley/xmpp.properties")));
  val user = props.getProperty("xmpp.user");
  val password = props.getProperty("xmpp.password");
  val allowedUsers = props.getProperty("xmpp.allowed").split(", ");
  val id = user.split("@")(0);
  val domain = user.split("@")(1);

  def start() {
    val verifier = new Java7HostnameVerifier()

    val config = 
      XMPPTCPConnectionConfiguration.builder()
        .setUsernameAndPassword(user, password)
        .setHost(domain)
        .setPort(5222)
        .setCompressionEnabled(true)
        .setServiceName(domain)
        .setHostnameVerifier(verifier)
        .build()

    connection = new XMPPTCPConnection(config)
    connection.connect()
    connection.login(id, password, "ScalaBot")

    var chatmanager = ChatManager.getInstanceFor(connection);

    for (allowedUser <- allowedUsers) {

      var userChat = chatmanager.createChat(allowedUser)

      val context = system.actorOf(Props(new Context(userChat)), name = s"message-${userChat.getParticipant.replace('/','_')}-${System.currentTimeMillis}")

      userChat.addMessageListener(createListener(context))

      userChat.sendMessage("Hello, I'm " + id + " and I'm ready to help you now.")
      chats.add(userChat);
    }

    chatmanager.addChatListener(new ChatManagerListener() {
      override def chatCreated(chat: Chat, createdLocally: Boolean) {
        if (createdLocally)
          return

        val participant = chat.getParticipant.split("/")(0)
        if (allowedUsers.contains(participant)) {
          val context = system.actorOf(Props(new Context(chat)), name = s"message-${chat.getParticipant.replace('/','_')}-${System.currentTimeMillis}")
	  chat.addMessageListener(createListener(context))
          chats.add(chat);
        } else
          chat.sendMessage(s"Sorry, not allowed to talk to you $participant")
      }
    })

    connection.addConnectionListener(new ConnectionListener() {
      override def authenticated(connection: XMPPConnection, ignored: Boolean) = Unit

      override def connected(connection: XMPPConnection) = Unit

      override def connectionClosed() = Unit

      override def connectionClosedOnError(e: Exception) = { 
        logger.error("Connection closed on error", e)
        logger.info("Attempting restart of xmpp stack")
        start()
      }

      override def reconnectingIn(seconds: Int) = Unit

      override def reconnectionFailed(e: Exception) = logger.error("Connection reconnection failed", e)

      override def reconnectionSuccessful() = Unit
    })
  }

  def stop() {
    for (chat <- chats) {
      chat.sendMessage("Good bye!")
    }

    connection.disconnect()
  }

  def main(args: Array[String]) {
    start()
  }
}
