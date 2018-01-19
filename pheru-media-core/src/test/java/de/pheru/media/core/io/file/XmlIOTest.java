package de.pheru.media.core.io.file;

import de.pheru.media.core.data.model.AudioFile;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class XmlIOTest {

    private static final String WRITE_FILE_NAME = "audiofile_write.xml";
    private static final String READ_FILE_NAME = "audiofile_read.xml";
    private static final String INVALID_FILE_NAME_UNKNOWN_TAG = "invalid_audiofile_unknown_tag.xml";
    private static final String INVALID_FILE_NAME_UNPARSABLE_VALUE = "invalid_audiofile_unparsable_value.xml";

    @Test
    public void writeCacheFile() throws Exception {
        final File cacheDir = new File(getClass().getResource("/xml").toURI());
        final File file = new File(cacheDir.getAbsolutePath() + "/" + WRITE_FILE_NAME);

        final AudioFile audioFile = createAudioFile("title1", "album1", "artist1");
        new XmlIO().write(file, AudioFile.class, audioFile, "AUDIOFILE");
    }

    @Test
    public void readCacheFile() throws Exception {
        final File file = new File(getClass().getResource("/xml/" + READ_FILE_NAME).toURI());
        final AudioFile audioFile = new XmlIO().read(file, AudioFile.class);

        assertEquals("title1", audioFile.getTitle());
        assertEquals("album1", audioFile.getAlbum());
        assertEquals("artist1", audioFile.getArtist());
        assertEquals("TestGenre", audioFile.getGenre());
        assertEquals(1, audioFile.getTrack());
        assertEquals("filenametitle1", audioFile.getFileName());
    }

    @Test(expected = IOException.class)
    public void readInvalidCacheFileUnknownTag() throws Exception {
        final File file = new File(getClass().getResource("/xml/" + INVALID_FILE_NAME_UNKNOWN_TAG).toURI());
        new XmlIO().read(file, AudioFile.class);
    }

    @Test(expected = IOException.class)
    public void readInvalidCacheFileUnparsableValue() throws Exception {
        final File file = new File(getClass().getResource("/xml/" + INVALID_FILE_NAME_UNPARSABLE_VALUE).toURI());
        new XmlIO().read(file, AudioFile.class);
    }

    private AudioFile createAudioFile(final String title, final String album, final String artist) {
        final AudioFile audioFile = new AudioFile();
        audioFile.setFileName("filename" + title);
        audioFile.setFilePath("filepath" + title);
        audioFile.setTitle(title);
        audioFile.setAlbum(album);
        audioFile.setArtist(artist);
        audioFile.setGenre("TestGenre");
        audioFile.setDuration(123);
        audioFile.setTrack((short) 1);
        audioFile.setYear((short) 2017);
        audioFile.setBitrate((short) 100);
        audioFile.setSize(1000);
        return audioFile;
    }
}