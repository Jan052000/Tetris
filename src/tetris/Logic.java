package tetris;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Random;

@SuppressWarnings("serial")
public class Logic extends Canvas {
	private ArrayList<Box> boxes = new ArrayList<Box>();
	private Piece active;
	private long tickspeed = 1000;
	private Rectangle bounds = new Rectangle(50, -1, 17 * 9 + 2, 17 * 21 + 1);// 17*10, 17*20
	private boolean generated = false;
	private int score = 0;

	public Logic() {
		updateActive();
	}

	/**
	 * Prüft ob bereits ein aktiver Stein vorhanden ist falls nicht, wird ein neuer
	 * Stein erstellt
	 */
	public void updateActive() {
		if (!generated) {
			active = getNextActive();
			generated = true;
		}
	}

	// erstellt neuen Stein
	public Piece getNextActive() {
		Random r = new Random();
		int n = r.nextInt(7);
		switch (n) {
		case 0:
			return new I(bounds);
		case 1:
			return new O(bounds);
		case 2:
			return new L(bounds);
		case 3:
			return new J(bounds);
		case 4:
			return new S(bounds);
		case 5:
			return new T(bounds);
		default:
			return new Z(bounds);
		}
	}

	public void doTick() {
		repaint();
		updateActive();
		int removed = 0;
		int[] lines = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0/* , 0 */ };// noch eine 0
		boolean remove[] = new boolean[lines.length];
		for (int i = 0; i < remove.length; i++) {
			remove[i] = false;
		}

//		Ist der Stein in dem Spielfeld bounds
//		Trifft der Stein beim Fallen auf den Rand des Spielfelds
		if (!isContained()) {
			for (Box box : active.getParts()) {
				if (!boxes.contains(box))
					boxes.add(box);
			}
			generated = false;
		}
//		Überlappen sich Boxen und aktiver Stein beim fallen?
//		Kann der Stein weiter fallen?
		boolean overlaps = false;
		for (Box part : active.getParts()) {
			for (Box box : boxes) {
				if (part.overlaps(box, 0, 17)) {
					overlaps = true;
				}
			}
		}
		if (overlaps) {
			for (Box box : active.getParts()) {
				if (!boxes.contains(box))
					boxes.add(box);
			}
			generated = false;
		}
		if (!overlaps && isContained()) {
			active.fall();
			repaint();
		}

// 		Zählt die Boxen pro Reihe
		ArrayList<Box> temp = new ArrayList<Box>();
		if (!boxes.isEmpty()) {
			for (Box box : boxes) {
				lines[(box.getYPos() / 17)]++;
			}
		}

//		Überlappen sich 2 Steine ist das Spielfeld voll und das Spiel vorbei
		for (int i = 0; i < lines.length; i++) {
			if (lines[0] != 0) {
				System.out.println("Game Over");
			} else {
				if (lines[i] == 9) {
					removed++;
					remove[i] = true;
					for (Box box : boxes) {
						if (box.getYPos() / 17 == i) {
							temp.add(box);
						}
					}
				}
			}
		}
//		Schiebt die Boxen über einer aufgelösten Reihe um eine Reihe nach unten
		for (Box box : temp) {
			boxes.remove(box);
		}
		for (int i = remove.length - 1; i >= 0; i--) {
			if (remove[i]) {
				for (Box box : boxes) {
					if (box.getYPos() / 17 < i + 1) {
						box.setYPos(box.getYPos() + 17);
					}
				}
			}
		}

//		Score
		if (removed == 1) {
			score += 40;
		} else if (removed == 2) {
			score += 100;
		} else if (removed == 3) {
			score += 300;
		} else if (removed == 4) {
			score += 1200;
		}
	}

//	 Bewegt den aktiven Steins
	public void updatePos(int x, int y) {
		boolean overlap = false;
		// Überlappen sich Boxen miteinander beim Bewegen(Links, Rechts)?
		for (Box part : active.getParts()) {
			for (Box box : boxes) {
				if (part.overlaps(box, x, y)) {
					overlap = true;
				}
			}
		}
		if (!overlap && isContained(x, y)) {
			active.updatePos(x, y);
		}
		this.repaint();
	}

	public void rotate() {
		if (!isContained(active.rotate(1))) {
			active.rotate(-1);
		}
	}

	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawRect(50, -1, 17 * 9 + 2, 17 * 21 + 1);// 17*10, 17*20
		g.drawString("Score", (int) (bounds.getMaxX() + 25), 10);
		g.drawString(score + "", (int) (bounds.getMaxX() + 25), 20);
		if (generated) {
			for (Box b : active.getParts()) {
				g.setColor(b.getColor());
				g.fillRect(b.getXPos(), b.getYPos(), b.getsize(), b.getsize());
			}
		}

		try {
			if (!boxes.isEmpty()) {
				Iterator<Box> iter = boxes.iterator();
				while (iter.hasNext()) {
					Box b = iter.next();
					g.setColor(b.getColor());
					g.fillRect(b.getXPos(), b.getYPos(), b.getsize(), b.getsize());
				}
			}
		} catch (ConcurrentModificationException e) {
			g.drawString("Exception beim Zeichnen", (int) bounds.getMinX(), (int) bounds.getMaxY()+10);
		}
		/*
		 * for (Box b : boxes) { g.setColor(b.getColor()); g.fillRect(b.getXPos(),
		 * b.getYPos(), b.getsize(), b.getsize()); }
		 */
	}

	// TODO tickspeed dynamisch an spieldauer anpassen
	public long getTickspeed() {
		return tickspeed;
	}

//	 Prüft ob der aktive Stein mit den änderungen
//	 x und y im Spielfeld bounds liegt
	public boolean isContained(int x, int y) {
		boolean contained = false;
		for (Box b : active.getParts()) {
			if (bounds.contains(b.getXPos() + x, b.getYPos() + y)) {
				contained = true;
			} else {
				return false;
			}
		}
		return contained;
	}

//	 prüft ob der Stein beim Fallen im Spielfeld liegt
	public boolean isContained() {
		boolean contained = false;
		for (Box b : active.getParts()) {
			if (bounds.contains(b.getXPos(), b.getYPos() + 17)) {
				contained = true;
			} else {
				return false;
			}
		}
		return contained;
	}

	public boolean isContained(ArrayList<Box> l) {
		boolean contained = false;
		for (Box b : l) {
			if (bounds.contains(b.getXPos(), b.getYPos() + 17)) {
				contained = true;
			} else {
				return false;
			}
		}
		return contained;
	}

}

abstract class Piece {
	private static ArrayList<Box> me = new ArrayList<Box>();

	public Piece(ArrayList<Box> box) {
		me = box;
	}

	public Piece() {
	}

//	Gravitation
	public void fall() {
		updatePos(0, 17);
	}

//	bewegt die einzelnen Boxen
	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	public abstract ArrayList<Box> rotate(int i);

}

class T extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	int rotated = 0;
	Color color = Color.PINK;

	public T(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid, 17, color));
		me.add(new Box(mid + 17, 17, color));
		me.add(new Box(mid - 17, 17, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	@Override
	public ArrayList<Box> rotate(int l) {
		rotated += l;
		if (rotated == 1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos() + 17);
						box.setYPos(me.get(1).getYPos());
					} else if (box.equals(me.get(2))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() + 17);
					} else {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() - 17);
					}
				}
			}
		} else if (rotated == 2) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() + 17);
					} else if (box.equals(me.get(2))) {
						box.setXPos(me.get(1).getXPos() - 17);
						box.setYPos(me.get(1).getYPos());
					} else {
						box.setXPos(me.get(1).getXPos() + 17);
						box.setYPos(me.get(1).getYPos());
					}
				}
			}
		} else if (rotated == 3 || rotated == -1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos() - 17);
						box.setYPos(me.get(1).getYPos());
					} else if (box.equals(me.get(2))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() - 17);
					} else {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() + 17);
					}
				}
			}
		} else if (rotated == 4 || rotated == 0) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() - 17);
					} else if (box.equals(me.get(2))) {
						box.setXPos(me.get(1).getXPos() + 17);
						box.setYPos(me.get(1).getYPos());
					} else {
						box.setXPos(me.get(1).getXPos() - 17);
						box.setYPos(me.get(1).getYPos());
					}
				}
			}
			rotated = 0;
		}

		return me;
	}

}

class Z extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	boolean rotated = false;
	Color color = Color.MAGENTA;

	public Z(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid - 17, 0, color));
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid, 17, color));
		me.add(new Box(mid + 17, 17, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	@Override
	public ArrayList<Box> rotate(int l) {
		int i = 0;
		if (rotated) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() - 17);
					} else {
						box.setXPos(me.get(1).getXPos() - 17);
						box.setYPos(me.get(1).getYPos() + 17 * i);
						i++;
					}
				}
			}
			rotated = false;
		} else {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos() - 17);
						box.setYPos(me.get(1).getYPos());
					} else {
						box.setXPos(me.get(1).getXPos() + 17 * i);
						box.setYPos(me.get(1).getYPos() + 17);
						i++;
					}
				}
			}
			rotated = true;
		}
		return me;
	}
}

class S extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	boolean rotated = false;
	Color color = Color.GREEN;

	public S(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid + 17, 0, color));
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid, 17, color));
		me.add(new Box(mid - 17, 17, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	@Override
	public ArrayList<Box> rotate(int l) {
		int i = 0;
		if (rotated) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos() + 17);
						box.setYPos(me.get(1).getYPos());
					} else {
						box.setXPos(me.get(1).getXPos() - (17 * i));
						box.setYPos(me.get(1).getYPos() + 17);
						i++;
					}
				}
			}
			rotated = false;
		} else {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					if (box.equals(me.get(0))) {
						box.setXPos(me.get(1).getXPos());
						box.setYPos(me.get(1).getYPos() - 17);
					} else {
						box.setXPos(me.get(1).getXPos() + 17);
						box.setYPos(me.get(1).getYPos() + 17 * i);
						i++;
					}
				}
			}
			rotated = true;
		}
		return me;
	}

}

class J extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	int rotated = 0;
	Color color = Color.BLUE;

	public J(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid + 17, 0, color));
		me.add(new Box(mid + 17, 17, color));
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid - 17, 0, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	// TODO vllt um die 3 statt 2 drehen
	@Override
	public ArrayList<Box> rotate(int l) {
		int i = 0;
		rotated += l;
		if (rotated == 1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int x = me.get(1).getXPos();
					box.setYPos(me.get(1).getYPos() - 17 * i);
					box.setXPos(x + 17);
					i++;
				}
			}
		} else if (rotated == 2) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int y = me.get(1).getYPos();
					box.setXPos(me.get(1).getXPos() + 17 * i);
					box.setYPos(y + 17);
					i++;
				}
			}
		} else if (rotated == 3 || rotated == -1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int x = me.get(1).getXPos();
					box.setYPos(me.get(1).getYPos() + 17 * i);
					box.setXPos(x - 17);
					i++;
				}
			}
		} else if (rotated == 4 || rotated == 0) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int y = me.get(1).getYPos();
					box.setXPos(me.get(1).getXPos() - (17 * i));
					box.setYPos(y - 17);
					i++;
				}
			}
			rotated = 0;
		}

		return me;
	}

}

class L extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	int rotated = 0;
	Color color = Color.ORANGE;

	public L(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid - 17, 0, color));
		me.add(new Box(mid - 17, 17, color));
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid + 17, 0, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	// TODO vllt um die 3 statt 2 drehen
	@Override
	public ArrayList<Box> rotate(int l) {
		int i = 0;
		rotated += l;
		// rotated++;
		if (rotated == 1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int x = me.get(1).getXPos();
					box.setYPos(me.get(1).getYPos() + 17 * i);
					box.setXPos(x + 17);
					i++;
				}
			}
		} else if (rotated == 2) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int y = me.get(1).getYPos();
					box.setXPos(me.get(1).getXPos() - (17 * i));
					box.setYPos(y + 17);
					i++;
				}
			}
		} else if (rotated == 3 || rotated == -1) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int y = me.get(1).getYPos();
					box.setXPos(me.get(1).getXPos() - 17);
					box.setYPos(y - (17 * i));
					i++;
				}
			}
		} else if (rotated == 4 || rotated == 0) {
			for (Box box : me) {
				if (!box.equals(me.get(1))) {
					int x = me.get(1).getXPos();
					box.setYPos(me.get(1).getYPos() - 17);
					box.setXPos(x + 17 * i);
					i++;
				}
			}
			rotated = 0;
		}

		return me;
	}
}

class O extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	boolean rotated = false;
	Color color = Color.YELLOW;

	public O(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid, 17, color));
		me.add(new Box(mid + 17, 0, color));
		me.add(new Box(mid + 17, 17, color));
	}

	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	@Override
	public ArrayList<Box> rotate(int i) {
		return me;
	}

}

class I extends Piece {
	ArrayList<Box> me = new ArrayList<Box>();
	boolean rotated = false;
	Color color = Color.CYAN;

	public I(Rectangle bounds) {
		int mid = (int) (bounds.getMinX() + 4 * 17 + 2);
		me.add(new Box(mid, 0, color));
		me.add(new Box(mid, 17, color));
		me.add(new Box(mid, 2 * 17, color));
		me.add(new Box(mid, 3 * 17, color));
	}

	/*
	 * // Gravitation public void fall() { updatePos(0, 17); }
	 */

	// bewegt die einzelnen Boxen
	public void updatePos(int x, int y) {
		for (Box b : me) {
			b.setXPos(b.getXPos() + x);
			b.setYPos(b.getYPos() + y);
		}
	}

	public ArrayList<Box> getParts() {
		return me;
	}

	public ArrayList<Box> rotate(int l) {
		int i = 0;
		if (rotated) {
			i = 0;
			for (Box part : me) {
				if (!part.equals(me.get(0))) {
					int x = me.get(0).getXPos();
					part.setXPos(x);
					part.setYPos(me.get(0).getYPos() + 17 * i);
				}
				i++;
				rotated = false;
			}
		} else {
			i = 0;
			for (Box part : me) {
				if (!part.equals(me.get(0))) {
					int y = me.get(0).getYPos();
					part.setYPos(y);
					part.setXPos(me.get(0).getXPos() + 17 * i);
				}
				i++;
			}
			rotated = true;
		}
		return me;
	}
}

class Box {
	private int xpos, ypos;
	private final int size = 16;
	private Color color;

	public Box(double x, int y, Color c) {
		xpos = (int) x;
		ypos = y;
		color = c;
	}

	public Box(int x, int y, Color c) {
		xpos = x;
		ypos = y;
		color = c;
	}

	/**
	 * Prüft ob this mit den veränderungen x und y mit Box b überlappt
	 */
	public boolean overlaps(Box b, int x, int y) {
		return this.getXPos() + x == b.getXPos() && this.getYPos() + y == b.getYPos();
	}

	public void setXPos(int x) {
		xpos = x;
	}

	public void setYPos(int y) {
		ypos = y;
	}

	public int getXPos() {
		return xpos;
	}

	public int getYPos() {
		return ypos;
	}

	public int getsize() {
		return size;
	}

	public Color getColor() {
		return color;
	}
}
