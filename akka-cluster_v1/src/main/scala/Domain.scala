import akka.actor.ActorRef

object Domain {
  case class ProcessFile(filename:String)
  case class ProcessLine(line: String)
  case class ProcessLineResult(count:Int)
}
