package com.avicenna.icscanner;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.scanbot.sdk.ScanbotSDKInitializer;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.app.FlutterApplication;

/**
 * IcScannerPlugin
 */
public class IcScannerPlugin implements MethodCallHandler, ActivityResultListener {
    /**
     * Plugin registration.
     */

    private Result flutterResult = null;
    public static int SCAN_IC_REQUEST_CODE = 1;
    public static Application application;
    private Activity activity = null;

    public static void registerWith(Registrar registrar) {
        application = (Application) registrar.context();

        IcScannerPlugin plugin = new IcScannerPlugin(registrar.activity());
        registrar.addActivityResultListener(plugin);

        final MethodChannel channel = new MethodChannel(registrar.messenger(), "ic_scanner");
        channel.setMethodCallHandler(plugin);
    }

    private IcScannerPlugin(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("scanIC")) {
            scanIC(call, result);
        } else if (call.method.equals("initialize")) {
            initializeScanbot(call, result);
        } else {
            result.notImplemented();
        }
    }

    private void initializeScanbot(MethodCall call, Result result) {
        try {
            if (call.hasArgument("licenseKey")) {
                String licenseKey = call.argument("licenseKey");

                new ScanbotSDKInitializer().license(application, licenseKey).initialize(application);
            }

            result.success(true);
        } catch (Exception e) {
            result.success(false);
        }
    }

    private void scanIC(MethodCall call, Result result) {
        // Field[] fields = MainActivity.class.getDeclaredFields();
        // HashMap<String, Object> details = new HashMap<>();
        // for (Field f : fields) {
        // if (call.argument(f.getName()) != null) {
        // details.put(f.getName(), call.argument(f.getName()));
        // }
        // }

        this.flutterResult = result;

        if (null != activity) {
            try {
                Intent intent = new Intent(activity, MainActivity.class);
                activity.startActivityForResult(intent, SCAN_IC_REQUEST_CODE);
            } catch (Exception e) {
                this.flutterResult.error("Error", "Unable to snap image", e);
            }
        }

        // flutterResult.success("Result from camera");
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_IC_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra("FILEPATH");
            this.flutterResult.success(filePath);
        } else {
            this.flutterResult.success("abcd");
        }
        return true;
    }
}
