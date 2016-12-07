package sk.upjs.jpaz2.theater;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;

@SuppressWarnings("serial")
class SplashFrame extends JFrame {

	/**
	 * Instance of the splash screen.
	 */
	private static SplashFrame splashInstance = null;

	/**
	 * Label with splash text.
	 */
	private JLabel lblInfo;

	/**
	 * Create the frame.
	 */
	private SplashFrame() {
		initializeComponents();
	}

	/**
	 * Creates and initializes GUI components.
	 */
	private void initializeComponents() {
		setBackground(Color.WHITE);
		setUndecorated(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 296, 56);
		JPanel contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		lblInfo = new JLabel("");
		lblInfo.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblInfo.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblInfo, BorderLayout.CENTER);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBorder(new EmptyBorder(0, 5, 5, 5));
		progressBar.setIndeterminate(true);
		contentPane.add(progressBar, BorderLayout.SOUTH);
	}

	/**
	 * Displays splash frame.
	 */
	public static void showSplash(final String info) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (splashInstance == null) {
						splashInstance = new SplashFrame();
						splashInstance.setLocationRelativeTo(null);
					}
					splashInstance.lblInfo.setText(info);
					splashInstance.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Displays splash frame.
	 */
	public static void hideSplash() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (splashInstance == null) {
						return;
					}

					splashInstance.setVisible(false);
					splashInstance = null;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
