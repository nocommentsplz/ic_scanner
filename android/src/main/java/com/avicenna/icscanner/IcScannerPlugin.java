package com.avicenna.icscanner;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.scanbot.sdk.ScanbotSDKInitializer;

/** IcScannerPlugin */
public class IcScannerPlugin implements MethodCallHandler {
  /** Plugin registration. */
  private final String licenseKey =
          "JBZRyeYlFpV0uStynGP4VcarkUmXG8" +
                  "fc8viNycfo5mXfgpBiVSEO/HSGGaem" +
                  "az74NKVy/8zVAAv8pB7v2d7uQm+yFH" +
                  "dE0MJpJoeIkst29yJpFHOZCTvc96To" +
                  "CUOWj0tKVEiw0h9oQeNMyveVT2QO/z" +
                  "xKU7G/GltAnGDMAhG0kwdK8qBJMdsl" +
                  "1feUZ92tO2+mOZ0y2lzfKVLnYDYU3Z" +
                  "gW1pAtqZK51IEvE3D3dhVW2bO5Ctr+" +
                  "EOa9oC5szMyChq+eFhyQXZvv4oPfyg" +
                  "9CgHWG1GphwT7vHXunJ/o/1mgyB09Z" +
                  "snbuq9isbolHpE6EFYhbC5P8N1MrHE" +
                  "B2FNVK3xU2jw==\nU2NhbmJvdFNESw" +
                  "ppby5zY2FuYm90LmV4YW1wbGUKMTU1" +
                  "Njg0MTU5OQozMjc2Nwoz\n";


  public static void registerWith(Registrar registrar) {
    new ScanbotSDKInitializer()
            // TODO add your license
            .license(this, licenseKey)
            .initialize(this);

    final MethodChannel channel = new MethodChannel(registrar.messenger(), "ic_scanner");
    channel.setMethodCallHandler(new IcScannerPlugin());
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);
    } else {
      result.notImplemented();
    }
  }
}
