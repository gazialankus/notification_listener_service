class TrackInfo {
  final String? id;
  final String? title;
  final String? album;
  final String? artist;
  final String? source;
  final Duration? duration;
  final Duration? position;
  final int? state;
  final int? volumePercent;

  TrackInfo({
    required this.id,
    required this.title,
    required this.album,
    required this.artist,
    required this.source,
    required this.duration,
    required this.position,
    required this.state,
    required this.volumePercent,
  });

  factory TrackInfo.fromMap(Map<dynamic, dynamic> map) {
    return TrackInfo(
      id: map['id'],
      title: map['title'],
      album: map['album'],
      artist: map['artist'],
      source: map['source'],
      duration: Duration(milliseconds: map['duration']),
      position: Duration(milliseconds: map['position']),
      state: map['state'],
      volumePercent: map['volumePercent'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'title': title,
      'album': album,
      'artist': artist,
      'source': source,
      'duration': duration?.inMilliseconds,
      'position': position?.inMilliseconds,
      'state': state,
      'volumePercent': volumePercent,
    };
  }
}
