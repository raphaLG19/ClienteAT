package org.example;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        ClienteGUI clienteGUI = new ClienteGUI();
        clienteGUI.createAndShowGUI();
    }
}

class ClienteGUI {
    private String path = "http://localhost:8080/api";
    private JTextField dados;

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Cliente-AT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1));

        dados = new JTextField();
        JPanel result = new JPanel();

        result.setSize(100, 400);
        dados.setFont((new Font("Arial", Font.PLAIN, 15)));

        JLabel label1 = new JLabel("Código RFID: ");
        label1.setFont((new Font("Arial", Font.BOLD, 15)));

        panel.add(label1);
        panel.add(dados);
        panel.add(result);
        JButton button = new JButton();
        panel.add(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HttpURLConnection connection = HttpConnection(path, dados);

                    String resultResponse = LerResultado(connection);

                    JsonElement jsonElement = JsonParser.parseString(resultResponse);
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    System.out.println("Resposta do Servidor: " + resultResponse);
                    result.setBackground(jsonObject.get("resultado").getAsString().equals("NACK") ? Color.RED : Color.GREEN);

                    connection.disconnect();

                    Temporizador(result);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        dados.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                verificarEnviar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                verificarEnviar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                verificarEnviar();
            }

            private void verificarEnviar() {
                if (dados.getText().length() == 10) {

                    button.doClick();

                    SwingUtilities.invokeLater(() -> dados.setText(""));
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private String LerResultado(HttpURLConnection connection) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            return response.toString();
        }
    }

    private HttpURLConnection HttpConnection(String path, JTextField dados) throws IOException {
        URL url = new URL(path);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setDoOutput(true);

        String requestBody = "{\"dados\": \"" + dados.getText() + "\"}";

        try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
            byte[] postData = requestBody.getBytes(StandardCharsets.UTF_8);
            wr.write(postData);
        }
        return connection;
    }

    private void Temporizador(JPanel result) {
        Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result.setBackground(Color.WHITE);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
}
