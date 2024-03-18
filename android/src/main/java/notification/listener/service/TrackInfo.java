package notification.listener.service;

public class TrackInfo {
    private String id;
    private String source;
    private Integer state;
    private String album;
    private String title;
    private String artist;
    private String genre;
    private Long duration;
    private Long position;
    private Integer volumePercent;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Integer getVolumePercent() {
        return volumePercent;
    }

    public void setVolumePercent(Integer volumePercent) {
        this.volumePercent = volumePercent;
    }

    public void clear() {
        id = null;
        source = null;
        state = null;
        album = null;
        title = null;
        artist = null;
        genre = null;
        duration = null;
        position = null;
        volumePercent = null;
    }
}
