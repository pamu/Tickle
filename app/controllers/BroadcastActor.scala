package controllers

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.iteratee.Enumerator

case class Join(androidId: String)
case class Leave(androidId: String)
case class Broadcast(message: String)

class BroadcastActor extends Actor {
	var androids = Set[String]()
	val (enumerator, channel) = Concurrent.broadcast[String]
	
	def receive = {
	  
	  case Join(androidId) => {
	    
	    println("join request from "+androidId)
	    
		  if(!androids.contains(androidId)) {
		    val iteratee = Iteratee.foreach[String]{ message => 
		      self ! Broadcast("%s %s " format(androidId, message))
		      println(message+" broadcasted")
		      }.mapDone{ case _ =>
		        self ! Leave(androidId)
		      }
		    androids += androidId
		    channel.push("User %s has joined the room, now %s users"format(androidId, androids.size))
		    sender ! (iteratee, enumerator)
		  }else {
			  val enumerator = Enumerator("AndroidID %s is already in use" format androidId)
			  val iteratee = Iteratee.ignore
			  (iteratee, enumerator)
		  }
		  
		  
	  }
	  
	  case Leave(androidId) => {
	    androids -= androidId
	    channel.push("User %s has left the room, %s users left" format(androidId, androids.size))
	  }
	  
	  case Broadcast(message) => channel.push(message) 
	  
	}
	
}