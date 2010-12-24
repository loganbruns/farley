package org.gedanken.farley

import akka.actor._
import java.io._
import java.util.Properties
import org.gedanken.farley.parser.Parser
import org.jivesoftware.smack._
import org.jivesoftware.smack.packet._
import scala.collection.mutable.HashSet

object Xmpp {
  var connection : XMPPConnection = null
  var chats = new HashSet[Chat]
  val parser = new Parser("../parser/models/en-sent.bin", 
			  "../parser/models/en-parser-chunking.bin");
  val system = ActorSystem("XmppSystem")
  class Context(userchat: Chat) extends Actor with ActorLogging {
    val chat = userchat

    def receive = {
      case org.gedanken.farley.parser.Message(message) =>
	chat.sendMessage(message)
    }
  }

  def start() {
    var props = new Properties();
    props.load(new FileInputStream(new File(System.getProperty("user.home"),
                                            ".farley/xmpp.properties")));
    var user = props.getProperty("xmpp.user");
    var password = props.getProperty("xmpp.password");
    var allowedUsers = props.getProperty("xmpp.allowed").split(", ");
    var id = user.split("@")(0);
    var domain = user.split("@")(1);

    var config = new ConnectionConfiguration(domain, 5222)
    config.setCompressionEnabled(true)
    config.setSASLAuthenticationEnabled(true)

    connection = new XMPPConnection(config)
    connection.connect()
    connection.login(id, password, "ScalaBot")

    var chatmanager = connection.getChatManager();

    for (allowedUser <- allowedUsers) {

      var userChat = chatmanager.createChat(allowedUser, null)

      val context = system.actorOf(Props(new Context(userChat)), name = "message-" + allowedUser)

      userChat.addMessageListener(new MessageListener() {
        override def processMessage(chat: Chat, message: Message) {
	  var body = message.getBody()
          if (body.startsWith("?"))
            return

	  chat.sendMessage(parser.process(message.getBody(), context))
	}
      })

      userChat.sendMessage("Hello, I'm " + id + " and I'm ready to help you now.")
      chats.add(userChat);
    }
  }

  def stop() {
    for (chat <- chats) {
      chat.sendMessage("Good bye!")
    }

    connection.disconnect()
  }

  def main(args: Array[String]) {
    start()

    Thread.sleep(7 * 86400000)

    stop()
  }
}
