package com.iot.device

import akka.actor.typed.ActorRef
import com.iot.device.DeviceGroup.DeviceGroupMessage
import com.iot.device.IotDevice.IotDeviceMessage

class DeviceManager() {

}

object DeviceManager {

    trait DeviceManagerMessage
    final case class RequestTrackDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceRegistered])
        extends DeviceManagerMessage with DeviceGroupMessage

    final case class DeviceRegistered(device: ActorRef[IotDeviceMessage])

    final case class RequestDeviceList(requestId: Long, groupId: String, replyTo: ActorRef[ReplyDeviceList])
        extends DeviceManagerMessage with DeviceGroupMessage

    final case class ReplyDeviceList(requestId: Long, ids: Set[String])

    private final case class DeviceGroupTerminated(groupId: String) extends DeviceManagerMessage
}
