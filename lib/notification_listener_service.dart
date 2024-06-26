import 'dart:async';
import 'dart:developer';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:notification_listener_service/notification_event.dart';
import 'package:notification_listener_service/track_info.dart';

const MethodChannel methodeChannel =
    MethodChannel('x-slayer/notifications_channel');
const EventChannel _eventChannel = EventChannel('x-slayer/notifications_event');
const EventChannel _mediaEventChannel = EventChannel('x-slayer/media_event');
Stream<TrackInfo>? _mediaStream;
Stream<ServiceNotificationEvent>? _stream;

class NotificationListenerService {
  NotificationListenerService._();

  /// stream the incoming notifications events
  static Stream<ServiceNotificationEvent> get notificationsStream {
    if (Platform.isAndroid) {
      _stream ??=
          _eventChannel.receiveBroadcastStream().map<ServiceNotificationEvent>(
                (event) => ServiceNotificationEvent.fromMap(event),
              );
      return _stream!;
    }
    throw Exception("Notifications API exclusively available on Android!");
  }

  static Stream<TrackInfo> get mediaStream {
    if (Platform.isAndroid) {
      _mediaStream ??= _mediaEventChannel
          .receiveBroadcastStream()
          .map<TrackInfo>((event) => TrackInfo.fromMap(event));
      return _mediaStream!;
    }
    throw Exception("Media API exclusively available on Android!");
  }

  /// request notification permission
  /// it will open the notification settings page and return `true` once the permission granted.
  static Future<bool> requestPermission() async {
    try {
      return await methodeChannel.invokeMethod('requestPermission');
    } on PlatformException catch (error) {
      log("$error");
      return Future.value(false);
    }
  }

  /// check if notification permission is enebaled
  static Future<bool> isPermissionGranted() async {
    try {
      return await methodeChannel.invokeMethod('isPermissionGranted');
    } on PlatformException catch (error) {
      log("$error");
      return false;
    }
  }
}
