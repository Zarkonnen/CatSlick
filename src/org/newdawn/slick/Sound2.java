package org.newdawn.slick;

import java.io.InputStream;
import java.net.URL;

import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioImpl2;
import org.newdawn.slick.openal.SoundStore2;
import org.newdawn.slick.util.Log;

/**
 * A single sound effect loaded from either OGG or XM/MOD file. Sounds are allocated to 
 * channels dynamically - if not channel is available the sound will not play. 
 *
 * @author kevin
 */
public class Sound2 {
	/** The internal sound effect represent this sound */
	private Audio sound;
	
	public Sound2(Sound2 original) {
		sound = new AudioImpl2(SoundStore2.get(), ((AudioImpl2) original.sound).getBufferID());
	}

	/**
	 * Create a new Sound 
	 * 
	 * @param in The location of the OGG or MOD/XM to load
	 * @param ref The name to associate this stream
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound2(InputStream in, String ref) throws SlickException {
		SoundStore2.get().init();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				sound = SoundStore2.get().getOgg(in);
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore2.get().getWAV(in);
			} else if (ref.toLowerCase().endsWith(".aif")) {
				sound = SoundStore2.get().getAIF(in);
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore2.get().getMOD(in);
			} else {
				throw new SlickException("Only .xm, .mod, .aif, .wav and .ogg are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+ref);
		}
	}
	
	/**
	 * Create a new Sound 
	 * 
	 * @param url The location of the OGG or MOD/XM to load
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound2(URL url) throws SlickException {
		SoundStore2.get().init();
		String ref = url.getFile();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				sound = SoundStore2.get().getOgg(url.openStream());
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore2.get().getWAV(url.openStream());
			} else if (ref.toLowerCase().endsWith(".aif")) {
				sound = SoundStore2.get().getAIF(url.openStream());
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore2.get().getMOD(url.openStream());
			} else {
				throw new SlickException("Only .xm, .mod, .aif, .wav and .ogg are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+ref);
		}
	}
	
	/**
	 * Create a new Sound 
	 * 
	 * @param ref The location of the OGG or MOD/XM to load
	 * @throws SlickException Indicates a failure to load the sound effect
	 */
	public Sound2(String ref) throws SlickException {
		SoundStore2.get().init();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				sound = SoundStore2.get().getOgg(ref);
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore2.get().getWAV(ref);
			} else if (ref.toLowerCase().endsWith(".aif")) {
				sound = SoundStore2.get().getAIF(ref);
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore2.get().getMOD(ref);
			} else {
				throw new SlickException("Only .xm, .mod, .aif, .wav and .ogg are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+ref);
		}
	}
	
	/**
	 * Play this sound effect at default volume and pitch
	 */
	public void play() {
		play(1.0f, 1.0f);
	}
	
	/**
	 * Play this sound effect at a given volume and pitch
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 */
	public void play(float pitch, float volume) {
		sound.playAsSoundEffect(pitch, volume * SoundStore2.get().getSoundVolume(), false);
	}

	/**
	 * Play a sound effect from a particular location
	 * 
	 * @param x The x position of the source of the effect
 	 * @param y The y position of the source of the effect
	 * @param z The z position of the source of the effect
	 */
	public void playAt(float x, float y, float z) {
		playAt(1.0f, 1.0f, x,y,z);
	}
	
	/**
	 * Play a sound effect from a particular location
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 * @param x The x position of the source of the effect
 	 * @param y The y position of the source of the effect
	 * @param z The z position of the source of the effect
	 */
	public void playAt(float pitch, float volume, float x, float y, float z) {
		sound.playAsSoundEffect(pitch, volume * SoundStore2.get().getSoundVolume(), false, x,y,z);
	}
	
	/**
	 * Loop a sound effect in a particular location
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 * @param x The x position of the source of the effect
 	 * @param y The y position of the source of the effect
	 * @param z The z position of the source of the effect
	 */
	public void loopAt(float pitch, float volume, float x, float y, float z) {
		sound.playAsSoundEffect(pitch, volume * SoundStore2.get().getSoundVolume(), true, x,y,z);
	}
	
	/**
	 * Loop this sound effect at default volume and pitch
	 */
	public void loop() {
		loop(1.0f, 1.0f);
	}
	
	/**
	 * Loop this sound effect at a given volume and pitch
	 * 
	 * @param pitch The pitch to play the sound effect at
	 * @param volume The volumen to play the sound effect at
	 */
	public void loop(float pitch, float volume) {
		sound.playAsSoundEffect(pitch, volume * SoundStore2.get().getSoundVolume(), true);
	}
	
	/**
	 * Check if the sound is currently playing
	 * 
	 * @return True if the sound is playing
	 */
	public boolean playing() {
		return sound.isPlaying();
	}
	
	/**
	 * Stop the sound being played
	 */
	public void stop() {
		sound.stop();
	}

	public void setVolume(float volume) {
		((AudioImpl2) sound).setVolume(volume);
	}

	public void setPitch(float pitch) {
		((AudioImpl2) sound).setPitch(pitch);
	}

	public void setLocation(float x, float y, int z) {
		((AudioImpl2) sound).setLocation(x, y, z);
	}
}