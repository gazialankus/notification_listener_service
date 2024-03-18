import 'dart:async';
import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:notification_listener_service/notification_event.dart';
import 'package:notification_listener_service/notification_listener_service.dart';
import 'package:notification_listener_service/track_info.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamSubscription<ServiceNotificationEvent>? _subscription;
  StreamSubscription<TrackInfo>? _mediaSubscription;
  List<ServiceNotificationEvent> events = [];
  List<TrackInfo> tracks = [];

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    TextButton(
                      onPressed: () async {
                        final res = await NotificationListenerService
                            .requestPermission();
                        log("Is enabled: $res");
                      },
                      child: const Text("Request Permission"),
                    ),
                    const SizedBox(height: 20.0),
                    TextButton(
                      onPressed: () async {
                        final bool res = await NotificationListenerService
                            .isPermissionGranted();
                        log("Is enabled: $res");
                      },
                      child: const Text("Check Permission"),
                    ),
                    const SizedBox(height: 20.0),
                    TextButton(
                      onPressed: () {
                        _subscription = NotificationListenerService
                            .notificationsStream
                            .listen((event) {
                          log("$event");
                          setState(() {
                            events.add(event);
                          });
                        });
                      },
                      child: const Text("Start Stream"),
                    ),
                    const SizedBox(height: 20.0),
                    TextButton(
                      onPressed: () {
                        _mediaSubscription = NotificationListenerService
                            .mediaStream
                            .listen((event) {
                          log("$event");
                          setState(() {
                            tracks.add(event);
                          });
                        });
                      },
                      child: const Text("Start Media Stream"),
                    ),
                    const SizedBox(height: 20.0),
                    TextButton(
                      onPressed: () {
                        _subscription?.cancel();
                      },
                      child: const Text("Stop Stream"),
                    ),
                  ],
                ),
              ),
              Expanded(
                child: ListView.builder(
                  shrinkWrap: true,
                  itemCount: events.length + tracks.length,
                  itemBuilder: (_, index) {
                    if (index < events.length) {
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 8.0),
                        child: ListTile(
                          onTap: () async {
                            try {
                              await events[index]
                                  .sendReply("This is an auto response");
                            } catch (e) {
                              log(e.toString());
                            }
                          },
                          trailing: events[index].hasRemoved!
                              ? const Text(
                                  "Removed",
                                  style: TextStyle(color: Colors.red),
                                )
                              : const SizedBox.shrink(),
                          leading: events[index].appIcon == null
                              ? const SizedBox.shrink()
                              : Image.memory(
                                  events[index].appIcon!,
                                  width: 35.0,
                                  height: 35.0,
                                ),
                          title: Text(events[index].title ?? "No title"),
                          subtitle: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                events[index].content ?? "no content",
                                style: const TextStyle(
                                    fontWeight: FontWeight.bold),
                              ),
                              const SizedBox(height: 8.0),
                              events[index].canReply!
                                  ? const Text(
                                      "Replied with: This is an auto reply",
                                      style: TextStyle(color: Colors.purple),
                                    )
                                  : const SizedBox.shrink(),
                              events[index].largeIcon != null
                                  ? Image.memory(
                                      events[index].largeIcon!,
                                    )
                                  : const SizedBox.shrink(),
                            ],
                          ),
                          isThreeLine: true,
                        ),
                      );
                    }
                    return ListTile(
                      leading: const Icon(Icons.music_note),
                      title: Text(
                          tracks[index - events.length].title ?? 'Unknown'),
                      trailing: Builder(
                        builder: (context) {
                          final playingState =
                              tracks[index - events.length].state;
                          if (playingState == 0) {
                            return const Icon(Icons.play_arrow);
                          } else if (playingState == 1) {
                            return const Icon(Icons.pause);
                          } else if (playingState == 2) {
                            return const Icon(Icons.stop);
                          } else {
                            return const Icon(Icons.question_mark);
                          }
                        },
                      ),
                    );
                  },
                ),
              )
            ],
          ),
        ),
      ),
    );
  }
}
