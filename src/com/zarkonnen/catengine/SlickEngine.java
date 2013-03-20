package com.zarkonnen.catengine;

import com.zarkonnen.catengine.util.Clr;
import com.zarkonnen.catengine.util.Pt;
import com.zarkonnen.catengine.util.Rect;
import com.zarkonnen.catengine.util.ScreenMode;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.newdawn.slick.*;

public class SlickEngine extends BasicGame implements Engine, KeyListener {
	public SlickEngine(String title, String loadBase, String soundLoadBase, Integer fps) {
		super(title);
		this.loadBase = loadBase;
		this.soundLoadBase = soundLoadBase;
		this.fps = fps;
	}
	
	int fps;
	Music currentMusic;
	String loadBase;
	String soundLoadBase;
	MyAppGameContainer agc;
	Game g;
	boolean fullscreen;
	boolean cursorVisible = true;
	String lastKeyPressed;
	final HashMap<String, SoftReference<Image>> images = new HashMap<String, SoftReference<Image>>();
	final HashMap<String, SoftReference<Music>> musics = new HashMap<String, SoftReference<Music>>();
	final HashMap<String, ArrayList<SoftReference<Sound>>> sounds = new HashMap<String, ArrayList<SoftReference<Sound>>>();
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		gc.setTargetFrameRate(fps);
		gc.setVSync(true);
		gc.setShowFPS(false);
	}

	@Override
	public void update(GameContainer gc, int i) throws SlickException {
		g.input(new MyInput(gc));
	}

	@Override
	public void render(GameContainer gc, Graphics grphcs) throws SlickException {
		g.render(new MyFrame(gc, grphcs));
	}
	
	@Override
	public void setup(com.zarkonnen.catengine.Game g) {
		this.g = g;
		try {
			agc = new MyAppGameContainer(this);
			agc.setDisplayMode(800, 600, false);
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	class MyAppGameContainer extends AppGameContainer {
		public MyAppGameContainer(org.newdawn.slick.Game game) throws SlickException {
			super(game, 800, 600, false);
			setup();
			getDelta();
		}

		public void runUntil(Condition c) throws SlickException {
			getDelta();
			while (!c.satisfied() && running()) {
				gameLoop();
			}

			if (forceExit) {
				System.exit(0);
			}
		}
	}

	@Override
	public void runUntil(Condition u) {
		try {
			agc.runUntil(u);
		} catch (SlickException e) {
			e.printStackTrace();
			agc.destroy();
			System.exit(1);
		}
	}
	
	@Override
	public void destroy() {
		agc.destroy();
	}
	
	private class MyInput implements com.zarkonnen.catengine.Input {
		GameContainer gc;
		public MyInput(GameContainer gc) {
			this.gc = gc;
		}

		@Override
		public boolean keyDown(String key) {
			try {
				return gc.getInput().isKeyDown(org.newdawn.slick.Input.class.getField("KEY_" + key).getInt(null));
			} catch (Exception e) {
				return false;
			}
		}
		
		
		@Override
		public boolean keyPressed(String key) {
			try {
				return gc.getInput().isKeyPressed(org.newdawn.slick.Input.class.getField("KEY_" + key).getInt(null));
			} catch (Exception e) {
				return false;
			}
		}

		@Override
		public Pt cursor() {
			return new Pt(gc.getInput().getMouseX(), gc.getInput().getMouseY());
		}

		@Override
		public Pt click() {
			for (int i = 3; i >= 0; i--) {
				if (gc.getInput().isMouseButtonDown(i)) {
					return cursor();
				}
			}
			return null;
		}

		@Override
		public int clickButton() {
			for (int i = 3; i >= 0; i--) {
				if (gc.getInput().isMouseButtonDown(i)) {
					return i + 1;
				}
			}
			return 0;
		}

		@Override
		public ScreenMode mode() {
			return new ScreenMode(gc.getWidth(), gc.getHeight(), fullscreen);
		}

		@Override
		public Input setMode(ScreenMode mode) {
			try {
				gc.setMouseGrabbed(false);
				agc.setDisplayMode(mode.width, mode.height, mode.fullscreen);
				fullscreen = mode.fullscreen;
				setCursorVisible(cursorVisible);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return this;
		}

		@Override
		public ArrayList<ScreenMode> modes() {
			ArrayList<ScreenMode> sm =  new ArrayList<ScreenMode>();
			sm.add(new ScreenMode(800, 600, false));
			sm.add(new ScreenMode(640, 480, true));
			sm.add(new ScreenMode(800, 600, true));
			sm.add(new ScreenMode(1024, 768, true));
			sm.add(new ScreenMode(gc.getScreenWidth(), gc.getScreenHeight(), true));
			return sm;
		}

		@Override
		public void quit() {
			gc.exit();
		}

		@Override
		public boolean isCursorVisible() {
			return cursorVisible;
		}

		@Override
		public com.zarkonnen.catengine.Input setCursorVisible(boolean visible) {
			cursorVisible = visible;
			gc.setMouseGrabbed(!visible);
			return this;
		}

		@Override
		public void play(String sound, double pitch, double volume, double x, double y) {
			try {
				Sound s = getSound(sound);
				if (s != null) {
					s.playAt((float) pitch, (float) volume, (float) x, (float) y, 0);
				}
			} catch (SlickException e) {
				e.printStackTrace();
			}
		}
		
		private Sound getSound(String sound) throws SlickException {
			if (!sound.contains(".")) { sound += ".ogg"; }
			if (!sounds.containsKey(sound)) {
				sounds.put(sound, new ArrayList<SoftReference<Sound>>());
			}
			ArrayList<SoftReference<Sound>> l = sounds.get(sound);
			for (SoftReference<Sound> entry : l) {
				Sound snd = entry.get();
				if (snd != null && !snd.playing()) {
					return snd;
				}
			}
			
			for (int i = 0; i < l.size(); i++) {
				Sound snd = l.get(i).get();
				if (snd == null) {
					snd = new Sound(SlickEngine.class.getResource(soundLoadBase + sound));
					l.set(i, new SoftReference<Sound>(snd));
					return snd;
				}
			}
			URL url = SlickEngine.class.getResource(soundLoadBase + sound);
			if (url == null) {
				return null;
			}
			Sound snd = new Sound(url);
			l.add(new SoftReference<Sound>(snd));
			return snd;
		}
		
		private Music getMusic(String music) throws SlickException {
			synchronized (musics) {
				if (!music.contains(".")) { music += ".ogg"; }
				if (musics.containsKey(music)) {
					SoftReference<Music> sr = musics.get(music);
					Music m = sr.get();
					if (m != null) {
						return m;
					}
				}
				Music m = new Music(SlickEngine.class.getResource(soundLoadBase + music));
				musics.put(music, new SoftReference<Music>(m));
				return m;
			}
		}

		@Override
		public void playMusic(final String music, final double volume, final MusicCallback startCallback, final MusicCallback doneCallback) {
			new Thread("MusicStarter") {
				@Override
				public void run() {
					try {
						synchronized (musics) {
							stopMusic();
							currentMusic = getMusic(music);
							currentMusic.play(1.0f, (float) volume);
							if (startCallback != null) { startCallback.run(music, volume); }
							currentMusic.addListener(new MusicListener() {
								@Override
								public void musicEnded(Music m) {
									if (m == currentMusic && doneCallback != null) {
										doneCallback.run(music, volume);
									}
								}

								@Override
								public void musicSwapped(Music oldM, Music newM) {
									// Ignore.
								}
							});
						}
					} catch (SlickException e) {
						e.printStackTrace();
					}
				}
			}.start();
		}

		@Override
		public void stopMusic() {
			synchronized (musics) {
				if (currentMusic != null && currentMusic.playing()) {
					currentMusic.stop();
					currentMusic = null;
				}
			}
		}

		@Override
		public String lastKeyPressed() {
			return lastKeyPressed;
		}

		@Override
		public void preloadSounds(List<String> sound, final Runnable callback) {
			final ArrayList<String> ss = new ArrayList<String>(sound);
			Thread t = new Thread("Sound Preloader") {
				@Override
				public void run() {
					for (String s : ss) {
						try {
							getSound(s);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (callback != null) {
						callback.run();
					}
				}
			};
			t.setDaemon(true);
			t.start();
		}

		@Override
		public void preloadMusic(List<String> music, final Runnable callback) {
			final ArrayList<String> ms = new ArrayList<String>(music);
			Thread t = new Thread("Music Preloader") {
				@Override
				public void run() {
					for (String m : ms) {
						try {
							getMusic(m);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (callback != null) {
						callback.run();
					}
				}
			};
			t.setDaemon(true);
			t.start();
		}
	}

	private class MyFrame implements Frame {
		private MyFrame(GameContainer gc, Graphics grphcs) {
			this.gc = gc;
			this.g = grphcs;
			gcW = gc.getWidth();
			gcH = gc.getHeight();
		}
		
		boolean wasRecting = false;
		final int gcW;
		final int gcH;
		final GameContainer gc;
		final Graphics g;
		
		@Override
		public Object nativeRenderer() {
			return g;
		}
		
		@Override
		public int fps() {
			return gc.getFPS();
		}
		
		@Override
		public ScreenMode mode() {
			return new ScreenMode(gc.getWidth(), gc.getHeight(), fullscreen);
		}

		@Override
		public Rect rect(Clr tint, double x, double y, double width, double height, double angle) {
			if (angle == 0 && (x + width <= 0 || y + height <= 0 || x > gcW || y > gcH)) {
				return new Rect(x, y, width, height);
			}
			if (tint.machineColorCache == null) {
				tint.machineColorCache = new Color(tint.r, tint.g, tint.b, tint.a);
			}
			Color c = (Color) tint.machineColorCache;
			g.setColor(c);
			if (angle == 0) {
				g.fillRect((float) x, (float) y, (float) width, (float) height);
			} else {
				g.translate((float) x, (float) y);
				g.rotate(0, 0, (float)  (angle * 180 / Math.PI));
				g.fillRect(0, 0, (float) width, (float) height);
				g.rotate(0, 0, (float) - (angle * 180 / Math.PI));
				g.translate((float) -x, (float) -y);
			}
			wasRecting = true;
			return new Rect(x, y, width, height);
		}

		@Override
		public Rect blit(String img, Clr tint, double x, double y, double width, double height, double angle, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipped) {
			Image image = getImage(img);
			if (image == null) { return null; }
			if (srcWidth == 0) { srcWidth = image.getWidth(); }
			if (width == 0) { width = srcWidth; }
			if (srcHeight == 0) { srcHeight = image.getHeight(); }
			if (height == 0) { height = srcHeight; }
			if (angle == 0 && (x + width <= 0 || y + height <= 0 || x > gcW || y > gcH)) {
				return new Rect(x, y, width == 0 ? image.getWidth() : width, height == 0 ? image.getHeight() : height);
			}
			if (flipped) {
				image = image.getFlippedCopy(true, false);
				srcX = image.getWidth() - srcX - srcWidth;
			}
			g.translate((float) x, (float) y);
			if (angle != 0) { g.rotate(0, 0, (float) (angle * 180 / Math.PI)); }
			if (tint != null) {
				if (tint.machineColorCache == null) {
					tint.machineColorCache = new Color(tint.r, tint.g, tint.b, tint.a);
				}
				Color c = (Color) tint.machineColorCache;
				if (tint.a == 255) {
					g.drawImage(image, 0f, 0f, (float) width, (float) height, srcX, srcY, srcX + srcWidth, srcY + srcHeight, c);
				} else {
					g.drawImage(image, 0f, 0f, (float) width, (float) height, srcX, srcY, srcX + srcWidth, srcY + srcHeight, c);
					g.drawImage(image, 0f, 0f, (float) width, (float) height, srcX, srcY, srcX + srcWidth, srcY + srcHeight);
				}
				wasRecting = false;
			} else {
				if (wasRecting) {
					g.setColor(Color.white);
					wasRecting = false;
				}
				g.drawImage(image, 0f, 0f, (float) width, (float) height, srcX, srcY, srcX + srcWidth, srcY + srcHeight);
			}
			g.setColor(Color.white);
			if (angle != 0) { g.rotate(0, 0, (float) - (angle * 180 / Math.PI)); }
			g.translate((float) -x, (float) -y);
			return new Rect(x, y, width == 0 ? image.getWidth() : width, height == 0 ? image.getHeight() : height);
		}

		private Image getImage(String name) {
			if (images.containsKey(name)) {
				Image img = images.get(name).get();
				if (img != null) { return img; }
			}
			Image img = loadImage(name);
			images.put(name, new SoftReference<Image>(img));
			return img;
		}
		
		private Image loadImage(String name) {
			if (!name.contains(".")) {
				name = name + ".png";
			}
			InputStream is = SlickEngine.class.getResourceAsStream(loadBase + name);
			try {
				return new Image(is, name, false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public void keyPressed(int i, char c) {
		lastKeyPressed = org.newdawn.slick.Input.getKeyName(i);
	}

	@Override
	public void keyReleased(int i, char c) {
		String k = org.newdawn.slick.Input.getKeyName(i);
		if (k.equals(lastKeyPressed)) {
			lastKeyPressed = null;
		}
	}
}
