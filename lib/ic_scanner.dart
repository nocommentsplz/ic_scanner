import 'dart:async';

import 'package:flutter/services.dart';

class IcScanner {
  static const MethodChannel _channel = const MethodChannel('ic_scanner');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String> scanIC() async {
    final String result = await _channel.invokeMethod('scanIC');
    return result;
  }

  static Future<bool> initialize(String licenseKey) async {
    Map<String, dynamic> args = {"licenseKey": licenseKey};
    final bool result = await _channel.invokeMethod('initialize', args);
    return result;
  }
}
