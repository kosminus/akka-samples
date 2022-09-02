package chat

import akka.actor.{Actor, ActorLogging}

class SimpleActor extends Actor with ActorLogging {
  override def receive: Receive = {
   case m => println(s"received message $m ")

  }
}
