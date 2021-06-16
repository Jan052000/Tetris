package tetris;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Game extends JFrame {
	boolean running = false;
	Timer timer;
	TimerTask task;
	Logic l;

	public static void main(String[] args) {
		new Game();
	}

	public Game() {

		l = new Logic();
		running = true;
		if (running) {
			timer = new Timer();
			task = new TimerTask() {

				@Override
				public void run() {
					if (running) {
						l.doTick();
					}
				}
			};
			timer.scheduleAtFixedRate(task, l.getTickspeed(), l.getTickspeed());
		}

		this.setVisible(true);
		this.setTitle("Tetris (Keine Rückmeldung)");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 500);
		// frame.setResizable(false);
		this.add(l);
		this.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (running) {
					if (e.getKeyCode() == 39) {
						l.updatePos(17, 0);
					} else if (e.getKeyCode() == 37) {
						l.updatePos(-17, 0);
					} else if (e.getKeyCode() == 40) {
						l.updatePos(0, 17);
					} else if (e.getKeyCode() == 38) {
						l.rotate();
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == 10 && running) {
					running = false;
				} else if (e.getKeyCode() == 10 && !running) {
					running = true;
				} /*
					 * else if(e.getKeyCode() == 10) { l.updateActive(); }
					 */
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
		});
	}

}
