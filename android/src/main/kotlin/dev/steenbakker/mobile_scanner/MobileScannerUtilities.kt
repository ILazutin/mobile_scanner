package dev.steenbakker.mobile_scanner

import android.graphics.ImageFormat
import android.graphics.Point
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.ByteArrayOutputStream

fun Image.toByteArray(): ByteArray {
    val yuvImage = YuvImage(yuv420toNV21(), ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    return out.toByteArray()
}

fun Image.yuv420toNV21(): ByteArray {
    val crop = cropRect
    val width = crop.width()
    val height = crop.height()
    val data = ByteArray(width * height * ImageFormat.getBitsPerPixel(format) / 8)
    val rowData = ByteArray(planes[0].rowStride)
    var channelOffset = 0
    var outputStride = 1
    for (i in planes.indices) {
        when (i) {
            0 -> {
                channelOffset = 0
                outputStride = 1
            }

            1 -> {
                channelOffset = width * height + 1
                outputStride = 2
            }

            2 -> {
                channelOffset = width * height
                outputStride = 2
            }
        }
        val buffer = planes[i].buffer
        val rowStride = planes[i].rowStride
        val pixelStride = planes[i].pixelStride
        val shift = if (i == 0) 0 else 1
        val w = width shr shift
        val h = height shr shift
        buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
        for (row in 0 until h) {
            var length: Int
            if (pixelStride == 1 && outputStride == 1) {
                length = w
                buffer[data, channelOffset, length]
                channelOffset += length
            } else {
                length = (w - 1) * pixelStride + 1
                buffer[rowData, 0, length]
                for (col in 0 until w) {
                    data[channelOffset] = rowData[col * pixelStride]
                    channelOffset += outputStride
                }
            }
            if (row < h - 1) {
                buffer.position(buffer.position() + rowStride - length)
            }
        }
    }
    return data
}

val Barcode.data: Map<String, Any?>
    get() = mapOf(
        "corners" to cornerPoints?.map { corner -> corner.data }, "format" to format,
        "rawBytes" to rawBytes, "rawValue" to rawValue?.trim(), "type" to valueType,
        "calendarEvent" to calendarEvent?.data, "contactInfo" to contactInfo?.data,
        "driverLicense" to driverLicense?.data, "email" to email?.data,
        "geoPoint" to geoPoint?.data, "phone" to phone?.data, "sms" to sms?.data,
        "url" to url?.data, "wifi" to wifi?.data, "displayValue" to displayValue
    )

private val Point.data: Map<String, Double>
    get() = mapOf("x" to x.toDouble(), "y" to y.toDouble())

private val Barcode.CalendarEvent.data: Map<String, Any?>
    get() = mapOf(
        "description" to description, "end" to end?.rawValue, "location" to location,
        "organizer" to organizer, "start" to start?.rawValue, "status" to status,
        "summary" to summary
    )

private val Barcode.ContactInfo.data: Map<String, Any?>
    get() = mapOf(
        "addresses" to addresses.map { address -> address.data },
        "emails" to emails.map { email -> email.data }, "name" to name?.data,
        "organization" to organization, "phones" to phones.map { phone -> phone.data },
        "title" to title, "urls" to urls
    )

private val Barcode.Address.data: Map<String, Any?>
    get() = mapOf(
        "addressLines" to addressLines.map { addressLine -> addressLine.toString() },
        "type" to type
    )

private val Barcode.PersonName.data: Map<String, Any?>
    get() = mapOf(
        "first" to first, "formattedName" to formattedName, "last" to last,
        "middle" to middle, "prefix" to prefix, "pronunciation" to pronunciation,
        "suffix" to suffix
    )

private val Barcode.DriverLicense.data: Map<String, Any?>
    get() = mapOf(
        "addressCity" to addressCity, "addressState" to addressState,
        "addressStreet" to addressStreet, "addressZip" to addressZip, "birthDate" to birthDate,
        "documentType" to documentType, "expiryDate" to expiryDate, "firstName" to firstName,
        "gender" to gender, "issueDate" to issueDate, "issuingCountry" to issuingCountry,
        "lastName" to lastName, "licenseNumber" to licenseNumber, "middleName" to middleName
    )

private val Barcode.Email.data: Map<String, Any?>
    get() = mapOf("address" to address, "body" to body, "subject" to subject, "type" to type)

private val Barcode.GeoPoint.data: Map<String, Any?>
    get() = mapOf("latitude" to lat, "longitude" to lng)

private val Barcode.Phone.data: Map<String, Any?>
    get() = mapOf("number" to number, "type" to type)

private val Barcode.Sms.data: Map<String, Any?>
    get() = mapOf("message" to message, "phoneNumber" to phoneNumber)

private val Barcode.UrlBookmark.data: Map<String, Any?>
    get() = mapOf("title" to title, "url" to url)

private val Barcode.WiFi.data: Map<String, Any?>
    get() = mapOf("encryptionType" to encryptionType, "password" to password, "ssid" to ssid)