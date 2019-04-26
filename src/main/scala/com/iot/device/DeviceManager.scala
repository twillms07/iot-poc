package com.iot.device

import akka.actor.typed.ActorRef

class DeviceManager() {

}

object DeviceManager {

    trait DeviceManagerMessage
    final case class RegisterDevice(groupId: String, deviceId: String, replyTo: ActorRef[DeviceManagerMessage])

    final case class DeviceRegistered()
}
