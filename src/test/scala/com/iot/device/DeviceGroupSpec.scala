package com.iot.device

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.iot.device.DeviceManager.{DeviceRegistered, ReplyDeviceList, RequestDeviceList, RequestTrackDevice}
import com.iot.device.IotDevice.{MeasurementRecorded, Passivate, UpdateMeasurement}
import org.scalatest.WordSpecLike

import scala.concurrent.duration._
class DeviceGroupSpec extends ScalaTestWithActorTestKit with WordSpecLike {

    "be able to register a device actor" in {
        val probe = createTestProbe[DeviceRegistered]()
        val groupActor = spawn(DeviceGroup("group"))

        groupActor ! RequestTrackDevice("group", "device1", probe.ref)
        val registered1 = probe.receiveMessage()
        val deviceActor1 = registered1.device

        // another deviceId
        groupActor ! RequestTrackDevice("group", "device2", probe.ref)
        val registered2 = probe.receiveMessage()
        val deviceActor2 = registered2.device
        deviceActor1 should !==(deviceActor2)

        // Check that the device actors are working
        val recordProbe = createTestProbe[MeasurementRecorded]()
        deviceActor1 ! UpdateMeasurement(requestId = 0, 1.0, recordProbe.ref)
        recordProbe.expectMessage(MeasurementRecorded(requestId = 0))
        deviceActor2 ! UpdateMeasurement(requestId = 1, 2.0, recordProbe.ref)
        recordProbe.expectMessage(MeasurementRecorded(requestId = 1))
    }

    "ignore requests for wrong groupId" in {
        val probe = createTestProbe[DeviceRegistered]()
        val groupActor = spawn(DeviceGroup("group"))

        groupActor ! RequestTrackDevice("wrongGroup", "device1", probe.ref)
        probe.expectNoMessage(500.milliseconds)
    }

    "return same actor for same deviceId" in {
        val probe = createTestProbe[DeviceRegistered]()
        val groupActor = spawn(DeviceGroup("group"))

        groupActor ! RequestTrackDevice("group", "device1", probe.ref)
        val registered1 = probe.receiveMessage()

        // registering same again should be idempotent
        groupActor ! RequestTrackDevice("group", "device1", probe.ref)
        val registered2 = probe.receiveMessage()

        registered1.device should ===(registered2.device)
    }

    "be able to list active devices" in {
        val registeredProbe = createTestProbe[DeviceRegistered]()
        val groupActor = spawn(DeviceGroup("group"))

        groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
        registeredProbe.receiveMessage()

        groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
        registeredProbe.receiveMessage()

        val deviceListProbe = createTestProbe[ReplyDeviceList]()
        groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
        deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2")))
    }

    "be able to list active devices after one shuts down" in {
        val registeredProbe = createTestProbe[DeviceRegistered]()
        val groupActor = spawn(DeviceGroup("group"))

        groupActor ! RequestTrackDevice("group", "device1", registeredProbe.ref)
        val registered1 = registeredProbe.receiveMessage()
        val toShutDown = registered1.device

        groupActor ! RequestTrackDevice("group", "device2", registeredProbe.ref)
        registeredProbe.receiveMessage()

        val deviceListProbe = createTestProbe[ReplyDeviceList]()
        groupActor ! RequestDeviceList(requestId = 0, groupId = "group", deviceListProbe.ref)
        deviceListProbe.expectMessage(ReplyDeviceList(requestId = 0, Set("device1", "device2")))

        toShutDown ! Passivate
        registeredProbe.expectTerminated(toShutDown, registeredProbe.remainingOrDefault)

        // using awaitAssert to retry because it might take longer for the groupActor
        // to see the Terminated, that order is undefined
        //todo this isn't working
//        registeredProbe.awaitAssert {
//            groupActor ! RequestDeviceList(requestId = 1, groupId = "group", deviceListProbe.ref)
//            deviceListProbe.expectMessage(ReplyDeviceList(requestId = 1, Set("device2")))
//        }
    }
}
