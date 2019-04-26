package com.iot.device

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import com.iot.device.IotDevice.{IotDeviceMessage, MeasurementRecorded, MeasurementResult, ReadMeasurement, UpdateMeasurement}


class IotDevice(context: ActorContext[IotDeviceMessage],groupId: String, deviceId: String) extends AbstractBehavior[IotDeviceMessage] {

    var deviceState: List[Double] = List.empty

    override def onMessage(msg: IotDeviceMessage): Behavior[IotDeviceMessage] =
        msg match {
            case UpdateMeasurement(requestId, measurement, replyTo) ⇒
                context.log.info(template = "Received measurement - {}",measurement)
                deviceState = measurement :: deviceState
                replyTo ! MeasurementRecorded(requestId)
                this

             case ReadMeasurement(requestId, replyTo) ⇒
                 if(deviceState.nonEmpty) {
                     context.log.info(s"Here's the deviceState - $deviceState")
                     replyTo ! MeasurementResult(requestId, Some(deviceState.head))
                 }
                 else
                     replyTo ! MeasurementResult(requestId,None)
                 this
        }

    override def onSignal: PartialFunction[Signal, Behavior[IotDeviceMessage]] = {
        case PostStop ⇒
            context.log.info(template = "Shutting down device {} from group {}",deviceId, groupId)
            this
    }

}

object IotDevice {
    def apply(groupId: String, deviceId: String): Behavior[IotDeviceMessage] =
        Behaviors.setup(context ⇒ new IotDevice(context, groupId, deviceId))


    sealed trait IotDeviceMessage
    final case class UpdateMeasurement(requestId: Long, measurement:Double, actorRef: ActorRef[MeasurementRecorded]) extends IotDeviceMessage
    final case class ReadMeasurement(requestId: Long, replyTo: ActorRef[MeasurementResult]) extends IotDeviceMessage

    sealed trait IotDeviceResponse
    final case class MeasurementResult(requestId: Long, value: Option[Double]) extends IotDeviceResponse
    final case class MeasurementRecorded(requestId: Long) extends IotDeviceResponse
}

