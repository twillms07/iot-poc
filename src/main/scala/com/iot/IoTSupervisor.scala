package com.iot

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.iot.IoTSupervisor.{IotCommand, IotSupervisorRequest, IotSupervisorResponse}


class IoTSupervisor(context: ActorContext[IotCommand]) extends AbstractBehavior[IotCommand] {
    override def onMessage(msg: IotCommand): Behavior[IotCommand] =
        msg match {
            case IotSupervisorRequest(replyTo) ⇒
                context.log.info("This got here...")
                replyTo ! IotSupervisorResponse("This works")
                this
    }

    override def onSignal: PartialFunction[Signal, Behavior[IotCommand]] = {
        case PostStop ⇒
            context.log.info("IoT Supervisor Stopped")
            this
    }

}

object IoTSupervisor {

    trait IotCommand
    case class IotSupervisorRequest(replyTo: ActorRef[IotSupervisorResponse]) extends IotCommand
    case class IotSupervisorResponse(value:String)

    def apply(): Behavior[IotCommand] = Behaviors.setup[IotCommand](context ⇒ new IoTSupervisor(context))
}
