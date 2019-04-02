import 'dart:async';

import 'package:flutter/services.dart';

class IcScanner {
  static const MethodChannel _channel =
      const MethodChannel('ic_scanner');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
