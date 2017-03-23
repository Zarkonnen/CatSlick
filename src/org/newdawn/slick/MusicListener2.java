package org.newdawn.slick;

/**
 * The description of any class needing to recieve notification of changes
 * to music state.
 *
 * @author kevin
 */
public interface MusicListener2 {

	/**
	 * Notification that a piece of music finished playing
	 * 
	 * @param music The music that finished playing
	 */
	public void musicEnded(Music2 music);
	
	/**
	 * Notification that a piece of music has been swapped 
	 * for another.
	 * 
	 * @param music The music that has been swapped out
	 * @param newMusic The new music we're playing
	 */
	public void musicSwapped(Music2 music, Music2 newMusic);
}