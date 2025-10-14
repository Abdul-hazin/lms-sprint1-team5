package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.AuthController;

public class LoginPanel extends JPanel {
    public interface LoginSuccess { void run(); }

    public LoginPanel(AuthController auth, LoginSuccess success) {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel lblId = new JLabel("User ID:");
        JTextField txtId = new JTextField(15);
        JLabel lblPw = new JLabel("Password:");
        JPasswordField txtPw = new JPasswordField(15);
        JButton btn = new JButton("Login");
        JLabel msg = new JLabel(" ");

        c.gridx=0; c.gridy=0; add(lblId, c);
        c.gridx=1; c.gridy=0; add(txtId, c);
        c.gridx=0; c.gridy=1; add(lblPw, c);
        c.gridx=1; c.gridy=1; add(txtPw, c);
        c.gridx=1; c.gridy=2; add(btn, c);
        c.gridx=0; c.gridy=3; c.gridwidth=2; add(msg, c);

        btn.addActionListener(e -> {
            String id = txtId.getText().trim();
            String pw = new String(txtPw.getPassword());
            if (auth.login(id, pw)) {
                msg.setText("Login successful.");
                success.run();
            } else {
                msg.setText("Login failed. Check user id/password or suspension.");
            }
        });
    }
}
