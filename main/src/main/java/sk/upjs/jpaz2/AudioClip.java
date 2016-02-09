package sk.upjs.jpaz2;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.concurrent.*;

import javax.sound.midi.*;
import javax.sound.sampled.*;

/**
 * Audio clip for playing an uncompressed sampled audio (AIFF, AU and WAV) or a
 * midi file (MID).
 */
public class AudioClip {

    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------
    // Internal implementations of audio clips with different audio formats
    // ---------------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------------

    /**
     * Interface for internal implementation of an audio clip.
     */
    private interface InternalAudioClip {
	/**
	 * Plays the audio clip
	 */
	void play();

	/**
	 * Stops the audio clip
	 */
	void stop();

	/**
	 * Plays the audio clip in loop
	 */
	void playInLoop();

	/**
	 * Returns whether the clip is playing.
	 * 
	 * @return true, if the audio clip is playing, false otherwise.
	 */
	boolean isPlaying();

	/**
	 * Plays the audio clip as an action sound. The sound is played
	 * simultaneously with other action sounds.
	 */
	void playAsActionSound();

	/**
	 * Stops all action sounds of the audio clip.
	 */
	void stopAllActionSounds();

	/**
	 * Returns volume of audio clip. Volume is a number between 0 and 1
	 * (loudest).
	 * 
	 * @return the volume of audio clip.
	 */
	double getVolume();

	/**
	 * Sets volume of audio clip. Volume is a number between 0 and 1
	 * (loudest).
	 * 
	 * @param volume
	 *            the desired volume of audio clip.
	 */
	void setVolume(double volume);
    }

    // ---------------------------------------------------------------------------------------------------
    // Internal implementations of raw uncompressed audio clip
    // ---------------------------------------------------------------------------------------------------

    private static class RawAudioClip implements InternalAudioClip {
	/**
	 * The size of buffer that is used to load sampled data from an audio
	 * resource during pre-loading.
	 */
	private static final int PRELOAD_BUFFER_SIZE = 10240;

	/**
	 * Immutable URL of the audio resource.
	 */
	final private URL url;

	/**
	 * Immutable loaded data of the audio resource.
	 */
	final private byte[] audioData;

	/**
	 * Format of loaded data of the audio resource.
	 */
	final private AudioFormat audioFormat;

	/**
	 * Runnable currently playing this audio clip.
	 */
	private RawAudioClipPlay clipPlay = null;

	/**
	 * Volume used for playing this audio clip.
	 */
	private double volume = 1;

	/**
	 * List of currently playing audio clips based on this clips that were
	 * started as action sound.
	 */
	final private List<RawAudioClipPlay> actionClipPlays = new ArrayList<RawAudioClipPlay>();

	/**
	 * Constructs audio clip for uncompressed audio file.
	 * 
	 * @param url
	 *            the URL of the audio resource.
	 * @param preloadInMemory
	 *            true, for loading audio data to memory.
	 */
	public RawAudioClip(URL url, boolean preloadInMemory) {
	    this.url = url;

	    // load all audio resource data to memory
	    if (preloadInMemory) {
		try {
		    AudioInputStream audioIS = null;
		    try {
			audioIS = AudioSystem.getAudioInputStream(url);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] copyBuffer = new byte[PRELOAD_BUFFER_SIZE];
			int nBytesRead = 0;
			while ((nBytesRead = audioIS.read(copyBuffer)) != -1) {
			    bos.write(copyBuffer, 0, nBytesRead);
			}
			audioData = bos.toByteArray();
			audioFormat = audioIS.getFormat();
		    } finally {
			if (audioIS != null)
			    audioIS.close();
		    }
		} catch (Exception e) {
		    throw new RuntimeException("Loading of audio resource failed.", e);
		}
	    } else {
		audioFormat = null;
		audioData = null;
	    }
	}

	synchronized public void play() {
	    stop();

	    clipPlay = new RawAudioClipPlay(this, false);
	    clipPlay.setVolume(volume);
	    submitToAudioThread(clipPlay);
	}

	synchronized public void stop() {
	    if (clipPlay == null)
		return;

	    clipPlay.stop();
	    clipPlay = null;
	}

	synchronized public void playInLoop() {
	    stop();

	    clipPlay = new RawAudioClipPlay(this, true);
	    clipPlay.setVolume(volume);
	    submitToAudioThread(clipPlay);
	}
	
	synchronized public boolean isPlaying() {
	    return (clipPlay != null);
	}

	synchronized public void playAsActionSound() {
	    RawAudioClipPlay clipPlay = new RawAudioClipPlay(this, false);
	    clipPlay.setVolume(volume);

	    actionClipPlays.add(clipPlay);
	    submitToAudioThread(clipPlay);
	}

	public void stopAllActionSounds() {
	    ArrayList<RawAudioClipPlay> toStopClips = null;

	    synchronized (this) {
		toStopClips = new ArrayList<RawAudioClipPlay>(actionClipPlays);
		actionClipPlays.clear();
	    }

	    for (RawAudioClipPlay clipPlay : toStopClips)
		clipPlay.stop();
	}

	/**
	 * Processes a notification that an playing of an audio clip was
	 * finished.
	 */
	synchronized private void processClipPlayingFinish(RawAudioClipPlay play) {
	    if (clipPlay == play)
		clipPlay = null;
	    else
		actionClipPlays.remove(play);
	}

	/**
	 * Submits runnable to audio thread pool for execution (playing the
	 * clip).
	 * 
	 * @param play
	 */
	private void submitToAudioThread(RawAudioClipPlay clipPlay) {
	    try {
		getAudioThreadPoolExecutor().execute(clipPlay);
	    } catch (Exception e) {
		throw new RuntimeException("Too much audio clips played simultaneously.");
	    }
	}

	synchronized public double getVolume() {
	    return volume;
	}

	synchronized public void setVolume(double volume) {
	    if (volume != this.volume) {
		this.volume = volume;

		if (clipPlay != null)
		    clipPlay.setVolume(volume);
	    }
	}
    }

    /**
     * Runnable that "feeds" the SourceDataLine with audio data in an audio
     * thread.
     */
    private static class RawAudioClipPlay implements Runnable {
	/**
	 * Maximum number of frames to be stored in the buffer for playing audio
	 * clips directly from an audio stream.
	 */
	private static final int FRAME_BUFFER_SIZE = 10240;

	/**
	 * AudioClip that is played.
	 */
	final private RawAudioClip clip;

	/**
	 * Indicated, whether audio clip will be looped.
	 */
	final private boolean loop;

	/**
	 * Audio line used to play the audio clip.
	 */
	private SourceDataLine audioLine;

	/**
	 * Indicates that the stop method was invoked.
	 */
	private boolean stopped = false;

	/**
	 * Volume value received by the most recent setVolume call.
	 */
	private double volume = 1;

	/**
	 * Constructs a runnable for audio thread to sample an audio clip.
	 * 
	 * @param clip
	 *            the clip to be sampled.
	 * @param loop
	 *            true, if clip should be looped.
	 */
	public RawAudioClipPlay(RawAudioClip clip, boolean loop) {
	    this.clip = clip;
	    this.loop = loop;
	    audioLine = null;
	}

	public void run() {
	    // audio format of the clip
	    AudioFormat format = null;
	    // buffer for sampled data of clips that are not load in the memory
	    byte[] sampledDataBuffer = null;
	    // true, if audio clip data were fully loaded into memory
	    boolean isClipInMemory = (clip.audioData != null);
	    // indicates that this is the first loop
	    boolean firstLoop = true;
	    try {
		if (isStopped())
		    return;

		do {
		    // create audio input stream for audio clip that is not
		    // pre-loaded in the memory
		    AudioInputStream audioStream = null;
		    try {
			if (isClipInMemory) {
			    format = clip.audioFormat;
			} else {
			    audioStream = AudioSystem.getAudioInputStream(clip.url);
			    format = audioStream.getFormat();
			}

			// if this is the first loop, create audio line
			if (firstLoop) {
			    SourceDataLine newAudioLine = AudioSystem.getSourceDataLine(format);
			    newAudioLine.open(format);
			    newAudioLine.start();
			    firstLoop = false;

			    synchronized (this) {
				audioLine = newAudioLine;
				updateVolume(volume);
			    }
			}

			if (isStopped())
			    return;

			if (isClipInMemory) {
			    // for buffered data, one write is enough
			    audioLine.write(clip.audioData, 0, clip.audioData.length);
			} else {
			    // create buffer for sampled data, if it does not
			    // exist
			    if (sampledDataBuffer == null) {
				long frameSize = format.getFrameSize();
				if ((frameSize == AudioSystem.NOT_SPECIFIED) || (frameSize <= 0))
				    frameSize = 1;

				// create buffer
				sampledDataBuffer = new byte[(int) (frameSize * FRAME_BUFFER_SIZE)];
			    }

			    // write data from stream to audio line
			    int nBytesRead = 0;
			    while ((nBytesRead = audioStream.read(sampledDataBuffer, 0, sampledDataBuffer.length)) != -1) {
				audioLine.write(sampledDataBuffer, 0, nBytesRead);
				if (isStopped())
				    return;
			    }
			}
		    } finally {
			if (audioStream != null)
			    audioStream.close();
		    }
		} while (loop && (!isStopped()));

		// wait until all buffered data are sampled
		if (!isStopped())
		    audioLine.drain();

	    } catch (Exception e) {
		System.err.println("Playing of an audio clip failed: " + e.toString());
	    } finally {
		// notify to audio clip that sampling of the clip was finished
		clip.processClipPlayingFinish(this);

		// close audio line
		synchronized (this) {
		    stopped = true;
		    if (audioLine != null) {
			audioLine.close();
			audioLine = null;
		    }
		}
	    }
	}

	/**
	 * Stops playing of the audio clip.
	 */
	synchronized public void stop() {
	    stopped = true;
	    if (audioLine != null) {
		audioLine.stop();
		audioLine.flush();
	    }
	}

	/**
	 * Returns whether request for stopping this clip was received.
	 */
	synchronized private boolean isStopped() {
	    return stopped;
	}

	/**
	 * Updates current volume (gain) for audio line.
	 */
	private void updateVolume(double volume) {
	    if (audioLine != null) {
		FloatControl gainControl = (FloatControl) audioLine.getControl(FloatControl.Type.MASTER_GAIN);
		float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
		if (dB < gainControl.getMinimum())
		    dB = gainControl.getMinimum();

		gainControl.setValue(dB);
	    }
	}

	/**
	 * Sets the volume.
	 */
	synchronized public void setVolume(double volume) {
	    this.volume = volume;
	    updateVolume(volume);
	}
    }

    // ---------------------------------------------------------------------------------------------------
    // ThreadPool management for playing audio clips in background.
    // ---------------------------------------------------------------------------------------------------

    /**
     * ThreadPoolExecutor managing threads for playing audio clips.
     */
    private static ThreadPoolExecutor audioThreadPoolExecutor = null;

    /**
     * Timeout in seconds after which an unused audio thread is stopped.
     */
    private static final int KILL_AUDIOTHREAD_TIMEOUT = 30;

    /**
     * Maximum number of threads that can simultaneously play an audio clip.
     */
    private static final int MAX_AUDIOTHREAD_NUMBER = 100;

    /**
     * Returns a ThreadPoolExecutor managing threads for playing audio clips.
     */
    synchronized private static ThreadPoolExecutor getAudioThreadPoolExecutor() {
	if (audioThreadPoolExecutor == null) {
	    // create ThreadFactory producing daemon threads
	    ThreadFactory daemonThreadFactory = new ThreadFactory() {
		/**
		 * Default thread factory
		 */
		private ThreadFactory defaultFactory = Executors.defaultThreadFactory();

		public Thread newThread(Runnable r) {
		    // create a new thread using default thread factory and
		    // change it to a daemon
		    Thread thread = defaultFactory.newThread(r);
		    thread.setDaemon(true);
		    return thread;
		}
	    };

	    // create thread pool executor
	    audioThreadPoolExecutor = new ThreadPoolExecutor(0, MAX_AUDIOTHREAD_NUMBER, KILL_AUDIOTHREAD_TIMEOUT,
		    TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), daemonThreadFactory);
	}

	return audioThreadPoolExecutor;
    }

    // ---------------------------------------------------------------------------------------------------
    // Internal implementations of MIDI sequences
    // ---------------------------------------------------------------------------------------------------

    private static class MidiAudioClip implements InternalAudioClip, MetaEventListener {

	// MIDI INTERNAL CONSTANTS
	private static final int VOLUME_CONTROLLER = 7;
	private static final int MAX_CHANNELS = 16;
	private static final int END_OF_TRACK_MESSAGE = 47;

	/**
	 * MIDI sequence to play.
	 */
	private Sequence midiSequence;

	/**
	 * MIDI Sequencer for playing the sequence.
	 */
	private Sequencer midiSequencer;

	/**
	 * MIDI Receiver that receives generated MIDI events.
	 */
	private Receiver midiReceiver;

	/**
	 * MIDI synthesizer
	 */
	private Synthesizer midiSynthesizer;

	/**
	 * URL of resource with MIDI sequence.
	 */
	final private URL url;

	/**
	 * Current volume of the clip - a number between 0 and 1 (loudest).
	 */
	private double volume = 1;

	/**
	 * Indicates whether volume of this clip was already updated.
	 */
	private boolean volumeUpdated = false;

	/**
	 * Indicates that this clip is played in the loop.
	 */
	private boolean isInLoop = false;

	/**
	 * Constructs audio clip based on a MIDI sequence.
	 * 
	 * @param url
	 *            the URL of the resource with MIDI sequence.
	 * @param preloadInMemory
	 *            true, for loading audio data to memory.
	 */
	public MidiAudioClip(URL url, boolean preloadInMemory) {
	    this.url = url;
	    if (preloadInMemory) {
		try {
		    midiSequence = MidiSystem.getSequence(url);
		} catch (Exception e) {
		    throw new RuntimeException("Loading of MIDI sequence failed", e);
		}
	    }
	}

	synchronized public void play() {
	    stop();

	    isInLoop = false;
	    prepareMIDI(false);
	    midiSequencer.start();
	}

	synchronized public void stop() {
	    if (midiSequencer != null) {
		midiSequencer.stop();
		midiSequencer.close();
		midiSequencer = null;

		if (midiReceiver != null) {
		    midiReceiver.close();
		    midiReceiver = null;
		}

		if (midiSynthesizer != null) {
		    midiSynthesizer.close();
		    midiSynthesizer = null;
		}
	    }
	}

	synchronized public void playInLoop() {
	    stop();

	    isInLoop = true;
	    prepareMIDI(true);
	    midiSequencer.start();
	}
	
	synchronized public boolean isPlaying() {
	    return (midiSequencer != null);	
	}

	public void playAsActionSound() {
	    throw new RuntimeException("Operation is not supported. MIDI sequence cannot be played as an action sound.");
	}

	public void stopAllActionSounds() {
	    // nothing to do (playAsActionSound is not supported).
	}

	/**
	 * Prepares MIDI system objects for playing a midi sequence.
	 */
	private void prepareMIDI(boolean loop) {
	    if (midiSequence == null) {
		try {
		    midiSequence = MidiSystem.getSequence(url);
		} catch (Exception e) {
		    System.err.println("Playing of an audio file with midi sequence failed:" + e.toString());
		    return;
		}
	    }

	    try {
		volumeUpdated = false;
		midiSequencer = MidiSystem.getSequencer(false);
		midiSynthesizer = MidiSystem.getSynthesizer();

		if (midiSynthesizer.getDefaultSoundbank() == null) {
		    midiSynthesizer = null;
		    midiReceiver = MidiSystem.getReceiver();
		} else {
		    midiSynthesizer.open();
		    midiReceiver = midiSynthesizer.getReceiver();
		}

		midiSequencer.addMetaEventListener(this);
		midiSequencer.open();
		midiSequencer.getTransmitter().setReceiver(midiReceiver);
		midiSequencer.setSequence(midiSequence);
		if (loop)
		    midiSequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
	    } catch (Exception e) {
		System.err.println("Playing of an audio file with midi sequence failed:" + e.toString());

		if (midiSequencer != null) {
		    midiSequencer.close();
		    midiSequencer = null;
		}

		if (midiReceiver != null) {
		    midiReceiver.close();
		    midiReceiver = null;
		}

		if (midiSynthesizer != null) {
		    midiSynthesizer.close();
		    midiSynthesizer = null;
		}
	    }
	}

	/**
	 * Updates volume.
	 */
	private void updateVolume() {
	    double volume = this.volume;
	    this.volume = -1000;
	    setVolume(volume);
	    volumeUpdated = true;
	}

	synchronized public double getVolume() {
	    return volume;
	}

	synchronized public void setVolume(double volume) {
	    int oldMidiVolume = (int) (this.volume * 127);
	    this.volume = volume;
	    int midiVolume = (int) (volume * 127);

	    if (midiVolume == oldMidiVolume)
		return;

	    if (midiSynthesizer != null) {
		MidiChannel[] channels = midiSynthesizer.getChannels();
		for (int i = 0; i < channels.length; i++) {
		    channels[i].controlChange(VOLUME_CONTROLLER, midiVolume);
		}
	    } else if (midiReceiver != null) {
		ShortMessage volMessage = new ShortMessage();
		for (int i = 0; i < MAX_CHANNELS; i++) {
		    try {
			volMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, VOLUME_CONTROLLER, midiVolume);
		    } catch (Exception e) {
		    }
		    midiReceiver.send(volMessage, -1);
		}
	    }
	}

	synchronized public void meta(MetaMessage msg) {
	    if (!volumeUpdated)
		updateVolume();

	    if (msg.getType() == END_OF_TRACK_MESSAGE) {
		if (!isInLoop)
		    stop();
	    }
	}
    }

    // ---------------------------------------------------------------------------------------------------
    // Methods and constructors for public use. Inheritance is not used in order
    // to make the use of this
    // "subsystem" as much simple as possible.
    // ---------------------------------------------------------------------------------------------------

    /**
     * Real implementation of the audio clip.
     */
    private InternalAudioClip audioClip;

    /**
     * Constructs a new audio clip from a resource specified by an URL.
     * 
     * @param url
     *            the URL of the audio file
     * @param storeInMemory
     *            true, if the audio file data should be loaded to memory, false
     *            otherwise.
     */
    public AudioClip(URL url, boolean storeInMemory) {
	createImplementation(url, storeInMemory);
    }

    /**
     * Constructs a new audio clip from a resource specified by an URL. The
     * resource is loaded into memory.
     * 
     * @param url
     *            the URL of the audio file
     */
    public AudioClip(URL url) {
	this(url, true);
    }

    /**
     * Constructs a new audio clip from an audio file.
     * 
     * @param file
     *            the audio file.
     * @param storeInMemory
     *            true, if the audio file data should be loaded to memory, false
     *            otherwise.
     */
    public AudioClip(File file, boolean storeInMemory) {
	URL fileURL = null;
	try {
	    fileURL = file.toURI().toURL();
	} catch (Exception e) {
	    throw new RuntimeException("Incorrect location of audio file.", e);
	}

	createImplementation(fileURL, storeInMemory);
    }

    /**
     * Constructs a new audio clip from an audio file. The file is loaded into
     * memory.
     * 
     * @param file
     *            the audio file.
     */
    public AudioClip(File file) {
	this(file, true);
    }

    /**
     * Constructs a new audio clip from an audio file at a specified location.
     * If location string starts with /, it is first considered to be a resource
     * location. If resource localization failed, location string is considered
     * as a file name.
     * 
     * @param audioLocation
     *            the string with location of an audio file (file or resource).
     * 
     * @param storeInMemory
     *            true, if the audio file data should be loaded to memory, false
     *            otherwise.
     */
    public AudioClip(String audioLocation, boolean storeInMemory) {
	URL audioResourceUrl = null;

	// if audioLocation starts with /, then try to locate image in resources
	if (audioLocation.startsWith("/")) {
	    try {
		audioResourceUrl = JPAZUtilities.getResourceAsURL(audioLocation);
	    } catch (Exception e) {
		// nothing to do
	    }
	}

	// try to locate the image considering imageLocation to be a filename
	if (audioResourceUrl == null) {
	    try {
		File audioFile = new File(audioLocation);
		if (audioFile.exists()) {
		    audioResourceUrl = audioFile.toURI().toURL();
		}
	    } catch (Exception e) {
		// nothing to do
	    }
	}

	if (audioResourceUrl == null)
	    throw new RuntimeException("Parameter audioLocation (" + audioLocation
		    + ") does not refer to an audio file.");

	createImplementation(audioResourceUrl, storeInMemory);
    }

    /**
     * Constructs a new audio clip from an audio file at a specified location.
     * If location string starts with /, it is first considered to be a resource
     * location. If resource localization failed, location string is considered
     * as a file name. The audio file is loaded into memory.
     * 
     * @param audioLocation
     *            the string with location of an audio file (file or resource).
     */
    public AudioClip(String audioLocation) {
	this(audioLocation, true);
    }

    /**
     * Constructs a new audio clip from an audio resource located in specified
     * package and a given filename.
     * 
     * @param packageName
     *            the package where resource file is searched
     * @param fileName
     *            the filename (in the package given by parameter packageName)
     * @param storeInMemory
     *            true, if the audio file data should be loaded to memory, false
     *            otherwise.
     */
    public AudioClip(String packageName, String fileName, boolean storeInMemory) {
	URL audioResourceUrl = null;
	if (packageName == null)
	    packageName = "";

	String resourceLocation = '/' + packageName.trim().replace('.', '/') + '/' + fileName.trim();
	if (resourceLocation.startsWith("//"))
	    resourceLocation = resourceLocation.substring(1);

	try {
	    audioResourceUrl = JPAZUtilities.getResourceAsURL(resourceLocation);
	} catch (Exception e) {
	    throw new RuntimeException("Specified resource was not found.");
	}

	if (audioResourceUrl == null)
	    throw new RuntimeException("Specified resource was not found.");

	createImplementation(audioResourceUrl, storeInMemory);
    }

    /**
     * Constructs a new audio clip from an audio resource located in specified
     * package and a given filename. The audio resource is loaded into memory.
     * 
     * @param packageName
     *            the package where resource file is searched
     * @param fileName
     *            the filename (in the package given by parameter packageName)
     */
    public AudioClip(String packageName, String fileName) {
	this(packageName, fileName, true);
    }

    /**
     * Creates a proper implementation of AudioClip for a given type of audio
     * resource.
     * 
     * @param url
     *            the URL of the audio resource.
     * @param storeInMemory
     *            true, if the audio file data should be loaded to memory, false
     *            otherwise.
     */
    private void createImplementation(URL url, boolean storeInMemory) {
	String resourceName = url.getFile();
	if (resourceName.endsWith(".mid"))
	    audioClip = new MidiAudioClip(url, storeInMemory);
	else
	    audioClip = new RawAudioClip(url, storeInMemory);
    }

    /**
     * Plays this audio clip. If it is played, it will be started again.
     */
    public void play() {
	audioClip.play();
    }

    /**
     * Stops this audio clip.
     */
    public void stop() {
	audioClip.stop();
    }

    /**
     * Plays this audio clip in the infinite loop.
     */
    public void playInLoop() {
	audioClip.playInLoop();
    }

    /**
     * Returns whether this audio clip is playing.
     * 
     * @return true, if this audio clip is playing, false otherwise.
     */
    public boolean isPlaying() {
	return audioClip.isPlaying();
    }

    /**
     * Plays this audio clip as an action sound. Action sounds are played
     * simultaneously with all other action sounds.
     */
    public void playAsActionSound() {
	audioClip.playAsActionSound();
    }

    /**
     * Stops all action sounds of this audio clip.
     */
    public void stopAllActionSounds() {
	audioClip.stopAllActionSounds();
    }

    /**
     * Returns volume of audio clip. Volume is a number between 0 and 1
     * (loudest).
     * 
     * @return the volume of audio clip.
     */
    public double getVolume() {
	return audioClip.getVolume();
    }

    /**
     * Sets volume of audio clip. Volume is a number between 0 and 1 (loudest).
     * 
     * @param volume
     *            the desired volume of audio clip.
     */
    public void setVolume(double volume) {
	volume = Math.min(1, volume);
	volume = Math.max(0, volume);
	audioClip.setVolume(volume);
    }
}
