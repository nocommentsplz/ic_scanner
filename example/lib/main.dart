import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ic_scanner/ic_scanner.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _cameraResult;

  static const String licenseKey = "cJJ2wYPf1ivfKYrg7NKDzYOUTvSYna" +
      "F6SeXTbjZIfn7u2SrHsgNHjB3BeqrV" +
      "dbqekzN0jAfthtstdo3qr6/8vBAux9" +
      "7Quh0/b/lbDx14PSl/tlWr+T6ybY/x" +
      "WUg2rPvec2gx56SqnQJDSkImmBp939" +
      "CqTcQwjkAyNWigYWDPzLp7gJ88PC23" +
      "A82N8BPT9LTfEgizOo8U5Rs6Q30MOd" +
      "Mp5+r/q1LaZghtlrxU4kaL5Jp6W5QM" +
      "sD+xHVsDZzcmqUL7i/AwkJf2lm9Uqp" +
      "/5RExGPq+5tM5suwU8mfdFg4ACMp0G" +
      "MjQdDrQBD9FwdwMEIKlbiQ9ZyxDp6G" +
      "eN/hcdTbu1TA==\nU2NhbmJvdFNESw" +
      "pjb20uYXZpY2VubmEuaWNzY2FubmVy" +
      "ZXhhbXBsZQoxNTU2OTI3OTk5CjU5MA" +
      "oz\n";

  @override
  void initState() {
    super.initState();

    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    bool initialized = await IcScanner.initialize(licenseKey);
    String platformVersion = 'Failed to invoke IcScanner.';
    if (initialized) {
      try {
        platformVersion = await IcScanner.platformVersion;
      } on PlatformException {}
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('IC Scanner Example'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('Running on: $_platformVersion\n'),
              Padding(
                padding: const EdgeInsets.all(8.0),
                child: Text('Captured File: ${_cameraResult ?? ""}\n'),
              ),
              Container(
                padding: EdgeInsets.symmetric(horizontal: 10.0, vertical: 10.0),
                child: null != _cameraResult
                    ? Image.file(
                        File(_cameraResult),
                        fit: BoxFit.cover,
                        width: 200.0,
                      )
                    : Container(
                        width: 200.0,
                        height: 200.0 * 4 / 3,
                      ),
              ),
              InkWell(
                child: Icon(
                  Icons.camera,
                  size: 48.0,
                ),
                onTap: () async {
                  try {
                    _cameraResult = await IcScanner.scanIC();
                    print("_cameraResult = $_cameraResult");
                    setState(() {});
                  } on PlatformException {
                    setState(() {
                      _cameraResult = 'Failed to invoke IcScanner.';
                    });
                  } catch (e) {
                    print(e.message);
                  }
                },
              )
            ],
          ),
        ),
      ),
    );
  }
}
