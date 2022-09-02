import Domain.{ProcessLine, ProcessLineResult}
import akka.actor.{Actor, ActorLogging}

class Worker extends Actor with ActorLogging {
  override def receive: Receive = {
    case ProcessLine(line) =>
    //  log.info(s"Processing: $line")
      sender() ! ProcessLineResult(line.split(" ").length)
  }
}
