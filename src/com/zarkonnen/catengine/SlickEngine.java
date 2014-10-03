package com.zarkonnen.catengine;

import com.zarkonnen.catengine.util.Clr;
import com.zarkonnen.catengine.util.Pt;
import com.zarkonnen.catengine.util.ScreenMode;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.newdawn.slick.*;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.renderer.Renderer;
import org.newdawn.slick.opengl.renderer.SGL;

public class SlickEngine extends BasicGame implements Engine, KeyListener, ExceptionHandler, MouseListener {
	public SlickEngine(String title, String loadBase, String soundLoadBase, Integer fps) {
		super(title);
		this.loadBase = loadBase;
		this.soundLoadBase = soundLoadBase;
		this.fps = fps;
	}
	
	protected static SGL GL = Renderer.get();
	boolean doExit = false;
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
	final HashMap<String, SoftReference<Sound>> sounds = new HashMap<String, SoftReference<Sound>>();
	final Object soundLoadMutex = new Object();
	ExceptionHandler eh = this;
	int mouseWheelMovement = 0;
	char lastChar = 0;
	
	Pt lastClick; int clickButton;
	
	@Override
	public void mouseWheelMoved(int i) {
		mouseWheelMovement += i;
	}
	
	@Override
	public void handle(Exception e, boolean fatal) {
		e.printStackTrace();
	}
	
	@Override
	public void setExceptionHandler(ExceptionHandler eh) {
		this.eh = eh;
	}
	
	@Override
	public void init(GameContainer gc) throws SlickException {
		gc.setTargetFrameRate(fps);
		gc.setVSync(true);
		gc.setShowFPS(false);
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		g.input(new MyInput(gc, delta));
		gc.getInput().clearKeyPressedRecord();
		gc.getInput().setMouseClickTolerance(10);
		mouseWheelMovement = 0;
		lastClick = null;
		clickButton = 0;
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
			eh.handle(e, true);
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

			if (doExit) {
				System.exit(0);
			}
		}
	}

	@Override
	public void runUntil(Condition u) {
		try {
			agc.runUntil(u);
		} catch (SlickException e) {
			eh.handle(e, true);
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
		int delta;
		
		public MyInput(GameContainer gc, int delta) {
			this.gc = gc;
			this.delta = delta;
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
		
		public char getTypedChar() {
			return 'x';
		}

		@Override
		public Pt cursor() {
			return new Pt(gc.getInput().getMouseX(), gc.getInput().getMouseY());
		}

		@Override
		public Pt mouseDown() {
			for (int i = 3; i >= 0; i--) {
				if (gc.getInput().isMouseButtonDown(i)) {
					return cursor();
				}
			}
			return null;
		}
		
		@Override
		public Pt clicked() {
			return lastClick;
		}

		@Override
		public int clickButton() {
			return clickButton;
		}

		@Override
		public ScreenMode mode() {
			return new ScreenMode(gc.getWidth(), gc.getHeight(), fullscreen);
		}
		
		@Override
		public int msDelta() { return delta; }

		@Override
		public Input setMode(ScreenMode mode) {
			try {
				gc.setMouseGrabbed(false);
				agc.setDisplayMode(mode.width, mode.height, mode.fullscreen);
				fullscreen = mode.fullscreen;
				setCursorVisible(cursorVisible);
			} catch (Exception e) {
				eh.handle(e, false);
			}
			return this;
		}

		@Override
		public ArrayList<ScreenMode> modes() {
			ArrayList<ScreenMode> sm = new ArrayList<ScreenMode>();
			try {
				for (DisplayMode dm : Display.getAvailableDisplayModes()) {
					sm.add(new ScreenMode(dm.getWidth(), dm.getHeight(), dm.isFullscreenCapable()));
				}
			} catch (Exception e) {
				eh.handle(e, false);
				sm.add(new ScreenMode(800, 600, false));
			}
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
		public void preload(List<Img> l) {
			for (Img img : l) {
				getImage(img);
			}
		}
		
		@Override
		public void preloadSounds(List<String> l) {
			synchronized (soundLoadMutex) {
				for (String snd : l) {
					getSound(snd);
				}
			}
		}

		@Override
		public void play(String sound, double pitch, double volume, double x, double y) {
			if (volume == 0) { return; }
			synchronized (soundLoadMutex) {
				Sound s = getSound(sound);
				if (s != null) {
					s.playAt((float) pitch, (float) volume, (float) x, (float) y, 0);
				}
			}
		}
		
		private Sound getSound(String sound) {
			synchronized (soundLoadMutex) {
				if (!sound.contains(".")) { sound += ".ogg"; }
				SoftReference<Sound> ref = sounds.get(sound);
				if (ref != null) {
					Sound snd = ref.get();
					if (snd != null) {
						return snd;
					}
				}
				
				for (int i = 0; i < 5; i++) {
					try {
						Sound snd = new Sound(SlickEngine.class.getResource(soundLoadBase + sound));
						sounds.put(sound, new SoftReference<Sound>(snd));
						return snd;
					} catch (Exception e) {}
				}
				return null;
			}
		}
		
		private Music getMusic(String music) throws SlickException {
			synchronized (soundLoadMutex) {
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
						synchronized (soundLoadMutex) {
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
					} catch (Exception e) {
						eh.handle(new RuntimeException("Could not play " + music, e), false);
					}
				}
			}.start();
		}

		@Override
		public void stopMusic() {
			synchronized (soundLoadMutex) {
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
		public int scrollAmount() {
			return mouseWheelMovement;
		}

		@Override
		public char lastInput() {
			return lastChar;
		}
	}

	private class MyFrame implements Frame {
		private MyFrame(GameContainer gc, Graphics grphcs) {
			this.gc = gc;
			this.g = grphcs;
			gcW = gc.getWidth();
			gcH = gc.getHeight();
		}
		
		boolean colorNotWhite;
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
		public void rect(Clr tint, double x, double y, double width, double height, double angle) {
			if (tint.machineColorCache == null) {
				tint.machineColorCache = new Color(tint.r, tint.g, tint.b, tint.a);
			}
			Color c = (Color) tint.machineColorCache;
			//g.setColor(c);
			Graphics.setCurrent(g);
			TextureImpl.bindNone();
			c.bind();
			//colorNotWhite = true;
			float x1 = (float) x;
			float y1 = (float) y;
			float w = (float) width;
			float h = (float) height;
			if (angle == 0) {
				GL.glBegin(SGL.GL_QUADS);
				GL.glVertex2f(x1, y1);
				GL.glVertex2f(x1 + w, y1);
				GL.glVertex2f(x1 + w, y1 + h);
				GL.glVertex2f(x1, y1 + h);
				GL.glEnd();
				//g.fillRect((float) x, (float) y, (float) width, (float) height);
			} else {
				g.rotate((float) (x + width / 2), (float) (y + height / 2), (float) (angle * 180 / Math.PI));
				GL.glBegin(SGL.GL_QUADS);
				GL.glVertex2f(x1, y1);
				GL.glVertex2f(x1 + w, y1);
				GL.glVertex2f(x1 + w, y1 + h);
				GL.glVertex2f(x1, y1 + h);
				GL.glEnd();
				//g.fillRect((float) x, (float) y, (float) width, (float) height);
				g.rotate((float) (x + width / 2), (float) (y + height / 2), -(float) (angle * 180 / Math.PI));
			}
		}

		@Override
		public void blit(Img img, Clr tint, double alpha, double x, double y, double width, double height, double angle) {
			if (img == null) { return; }
			if (img.machineImgCache == null) {
				getImage(img);
			}
			Image image = (Image) img.machineImgCache;
			if (image == null) { return; }
			width = width == 0 ? img.machineWCache : width;
			height = height == 0 ? img.machineHCache : height;
			
			/*if (angle == 0 && (x + width <= 0 || y + height <= 0 || x > gcW || y > gcH)) {
				return;
			}*/
			
			image.setRotation((float) (angle * 180 / Math.PI));
			
			if (tint == null) {
				/*if (colorNotWhite) {
					g.setColor(Color.white);
					colorNotWhite = false;
				}*/
				image.setAlpha((float) alpha);
				image.draw((float) x, (float) y, (float) (width), (float) (height));
			} else {
				if (tint.machineColorCache == null) {
					tint.machineColorCache = new Color(tint.r, tint.g, tint.b);
				}
				Color c = (Color) tint.machineColorCache;
				if (tint.a == 255) {
					image.setAlpha((float) alpha);
					image.draw((float) x, (float) y, (float) (width), (float) (height), c);
				} else {
					if (alpha == 1) {
						image.setAlpha(1);
						image.draw((float) x, (float) y, (float) (width), (float) (height));
						image.setAlpha(tint.a / 255.0f);
						image.draw((float) x, (float) y, (float) (width), (float) (height), c);
					} else {
						image.setAlpha((float) (alpha * (255 - tint.a) / 255.0));
						image.draw((float) x, (float) y, (float) (width), (float) (height));
						image.setAlpha((float) (alpha * tint.a / 255.0));
						image.draw((float) x, (float) y, (float) (width), (float) (height), c);
					}
				}
			}
		}

		@Override
		public void shift(double dx, double dy) {
			g.translate((float) dx, (float) dy);
		}

		@Override
		public void scale(double xScale, double yScale) {
			g.scale((float) xScale, (float) yScale);
		}

		@Override
		public void rotate(double angle) {
			g.rotate(0, 0, (float) angle);
		}

		@Override
		public void resetTransforms() {
			g.resetTransform();
		}

		@Override
		public double getWidth(Img img) {
			if (img == null) { return 0; }
			if (img.srcWidth != 0) { return img.srcWidth; }
			if (img.machineImgCache == null) {
				getImage(img);
			}
			return img.machineWCache;
		}

		@Override
		public double getHeight(Img img) {
			if (img == null) { return 0; }
			if (img.srcHeight != 0) { return img.srcHeight; }
			if (img.machineImgCache == null) {
				getImage(img);
			}
			return img.machineHCache;
		}

		@Override
		public Pt cursor() {
			return new Pt(gc.getInput().getMouseX(), gc.getInput().getMouseY());
		}
	}
	
	private void getImage(Img img) {
		Image image = null;
		if (images.containsKey(img.key)) {
			image = images.get(img.key).get();
		}
		if (image == null && img.flipped && images.containsKey(img.src)) {
			image = images.get(img.src).get();
			if (image != null) {
				image = image.getFlippedCopy(true, false);
				images.put(img.key, new SoftReference<Image>(image));
			}
		}
		if (image == null) {
			image = loadImage(img.src);
			images.put(img.src, new SoftReference<Image>(image));
			if (img.flipped) {
				image = image.getFlippedCopy(true, false);
				images.put(img.key, new SoftReference<Image>(image));
			}
		}
		if (image == null) { return; }
		if (img.srcWidth != 0 && img.srcHeight != 0) {
			image = image.getSubImage(img.flipped ? (image.getWidth() - img.srcX - img.srcWidth) : img.srcX, img.srcY, img.srcWidth, img.srcHeight);
		}
		image.setCenterOfRotation(image.getWidth() / 2.0f, image.getHeight() / 2.0f);
		img.machineImgCache = image;
		img.machineWCache = image.getWidth();
		img.machineHCache = image.getHeight();
	}

	private Image loadImage(String name) {
		if (!name.contains(".")) {
			name = name + ".png";
		}
		InputStream is = SlickEngine.class.getResourceAsStream(loadBase + name);
		try {
			return new Image(is, name, false);
		} catch (Exception e) {
			eh.handle(e, false);
			return null;
		}
	}

	@Override
	public void keyPressed(int i, char c) {
		lastKeyPressed = org.newdawn.slick.Input.getKeyName(i);
		lastChar = c;
	}

	@Override
	public void keyReleased(int i, char c) {
		String k = org.newdawn.slick.Input.getKeyName(i);
		if (k.equals(lastKeyPressed)) {
			lastKeyPressed = null;
		}
		if (c == lastChar) {
			lastChar = 0;
		}
	}
	
	@Override
	public void mouseClicked(int button, int x, int y, int clickCount) {
		super.mouseClicked(button, x, y, clickCount);
		lastClick = new Pt(x, y);
		clickButton = button + 1;
	}
}
