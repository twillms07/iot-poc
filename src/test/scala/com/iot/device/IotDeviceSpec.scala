package com.iot.device

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.iot.device.IotDevice._
import org.scalatest.WordSpecLike

class IotDeviceSpec extends ScalaTestWithActorTestKit with WordSpecLike {

    "Device actor" must {
        "reply with empty reading if there are no measurements " in {
            val iotDevice = spawn(IotDevice("group", "device"),"iot-test-empty-reading")
            val testProbe = createTestProbe[MeasurementResult]()
            iotDevice ! ReadMeasurement(1234,testProbe.ref)
            testProbe.expectMessage(MeasurementResult(1234L,None))
        }

        "receive confirmation that that latest measurement was updated " in {
            val iotDevice = spawn(IotDevice("group", "device"),"iot-test-record-temp")
            val testProbe = createTestProbe[IotDeviceResponse]()
            iotDevice ! UpdateMeasurement(123, 45L,testProbe.ref)
            testProbe.expectMessage(MeasurementRecorded(123))
        }

        "receive the latest measurement " in {
            val iotDevice = spawn(IotDevice("group", "device"), "iot-test-record-3")
            val testProbe = createTestProbe[IotDeviceResponse]()
            iotDevice ! UpdateMeasurement(123, 45L, testProbe.ref)
            iotDevice ! ReadMeasurement(123, testProbe.ref)
            testProbe.expectMessage(MeasurementRecorded(123))
            testProbe.expectMessage(MeasurementResult(123,Some(45.0)))
        }

    }

}
