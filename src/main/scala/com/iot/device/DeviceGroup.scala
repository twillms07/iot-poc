package com.iot.device

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.iot.device.DeviceGroup.DeviceGroupMessage
import com.iot.device.DeviceManager.DeviceManagerMessage
import com.iot.device.IotDevice.IotDeviceMessage

class DeviceGroup(context: ActorContext[DeviceGroupMessage], groupId: String) extends AbstractBehavior[DeviceGroup.DeviceGroupMessage] {
    import DeviceGroup._

    private var deviceIdToActor = Map.empty[String, ActorRef[IotDeviceMessage]]

    context.log.info("DeviceGroup {} started", groupId)

    override def onMessage(msg: DeviceGroupMessage): Behavior[DeviceGroupMessage] =
        msg match {
            case trackMsg @ RequestTrackDevice(`groupId`, deviceId, replyTo) =>
                deviceIdToActor.get(deviceId) match {
                    case Some(deviceActor) =>
                        replyTo ! DeviceRegistered(deviceActor)
                    case None =>
                        context.log.info("Creating device actor for {}", trackMsg.deviceId)
                        val deviceActor = context.spawn(IotDevice(groupId, deviceId), s"device-$deviceId")
                        deviceIdToActor += deviceId -> deviceActor
                        replyTo ! DeviceRegistered(deviceActor)
                }
                this

            case RequestTrackDevice(gId, _, _) =>
                context.log.warning("Ignoring TrackDevice request for {}. This actor is responsible for {}.", gId, groupId)
                this
        }

    override def onSignal: PartialFunction[Signal, Behavior[DeviceGroupMessage]] = {
        case PostStop =>
            context.log.info("DeviceGroup {} stopped", groupId)
            this
    }
}

object DeviceGroup {

    trait DeviceGroupMessage
    final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
        extends DeviceManagerMessage with DeviceGroupMessage

    final case class DeviceRegistered(device: ActorRef[IotDeviceMessage])

    private final case class DeviceTerminated(device: ActorRef[IotDeviceMessage], groupId: String, deviceId: String)
        extends DeviceGroupMessage

    def apply(groupId: String): Behavior[DeviceGroupMessage] = Behaviors.setup(context ⇒  new DeviceGroup(context, groupId))


}
