package com.swiftsync.review.client;

import com.swiftsync.mediaplayer.MediaPlayerPanel;
import com.swiftsync.review.Constants;
import com.swiftsync.review.exception.InvalidSessionID;
import com.swiftsync.review.util.ConsoleColors;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.media.InfoApi;
import uk.co.caprica.vlcj.media.Media;
import uk.co.caprica.vlcj.media.MediaEventAdapter;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.swiftsync.review.util.AdvancedOutput.println;

public class SSR_Client extends JFrame {

    private SessionClient client;
    private Color background = new Color(45, 49, 52);
    private Color background2 = new Color(62, 69, 79);
    private Color foreground = new Color(0, 0, 0);
    private boolean launched = false;
    private JDialog loadDialog;
    private JPanel dashPanel;
    private Role role = null;
    private JLabel proxyStatusLabel, titleLabel, currentMediaFileLabel;
    private MediaPlayerPanel mediaPlayerPanel;
    private JPanel sessionDetailsPanel, clientSettingsPanel;
    private File currentMediaFile;
    private File currentProxyDirectory;
    private static SortSetting sortSetting = SortSetting.NAME;

    public SSR_Client() {
        super();

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        displayLoadingDialog();

        try {
            Thread.sleep(1800);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (isConnectedToWiFi()) {
            instantiateClient();
            loadUI();
            loadDialog.setVisible(false);
            launched = true;
        } else {
            showConnectionErrorDialog();
            System.exit(0);
        }
    }

    private void instantiateClient(){
        client = new SessionClient() {
            @Override
            public void connectionError(Exception e) {
                String message;
                if (e instanceof IOException) {
                    message = "An I/O error occurred. Please check your network connection and try again.";
                } else if (e instanceof URISyntaxException) {
                    message = "A URI syntax error occurred. Please contact support.";
                } else {
                    message = "An unknown error occurred. Please contact support.";
                }
                JOptionPane.showMessageDialog(null, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
            }

            @Override
            public void setMediaPlayerTo(String mediaPath) {
                if(role == Role.HOST){
                    mediaPlayerPanel.playMedia(mediaPath);
                    client.broadcastChangeMediaTo(mediaPath);
                } else {
                    // find the filename of the media
                    String filename = mediaPath.substring(mediaPath.lastIndexOf(File.separator) + 1);

                    // search the proxy folder for the file
                    File mediaFile = findProxyFile(filename);

                    // if the file is found, play it
                    if(mediaFile != null){
                        mediaPlayerPanel.playMedia(mediaFile.getPath());
                    }
                }
            }
        };
    }

    private void loadUI() {
        setTitle("SSR Client " + Constants.VERSION);
        setSize(650, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        titleLabel = new JLabel("SwiftSync Review", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        JPanel joinPanel = createJoinPanel();
        JPanel hostPanel = createHostPanel();

        dashPanel = new JPanel(new GridBagLayout());
        dashPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 10, 0, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;

        dashPanel.add(joinPanel, gbc);
        dashPanel.add(hostPanel, gbc);

        add(dashPanel, BorderLayout.CENTER);

        setVisible(true);
        this.toFront();
    }

    private JPanel createJoinPanel() {
        JPanel joinPanel = new JPanel(new BorderLayout());
        joinPanel.setPreferredSize(new Dimension(250, 150));
        joinPanel.setMaximumSize(new Dimension(250, 150));
        joinPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Join Session", TitledBorder.CENTER, TitledBorder.TOP));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        PlaceholderTextField sessionIdField = new PlaceholderTextField("Enter session ID");
        sessionIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        sessionIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, sessionIdField.getPreferredSize().height));
        JButton joinButton = new JButton("Join Session");
        joinButton.setFont(new Font("Arial", Font.BOLD, 14));

        sessionIdField.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.addActionListener(e->joinSession(sessionIdField.getText()));

        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(sessionIdField);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(joinButton);

        joinPanel.add(contentPanel, BorderLayout.CENTER);
        return joinPanel;
    }

    private JPanel createHostPanel() {
        JPanel hostPanel = new JPanel(new BorderLayout());
        hostPanel.setPreferredSize(new Dimension(250, 150));
        hostPanel.setMaximumSize(new Dimension(250, 150));
        hostPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Host", TitledBorder.CENTER, TitledBorder.TOP));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton createButton = new JButton("Create New Session");
        createButton.setFont(new Font("Arial", Font.BOLD, 14));
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.addActionListener(e->createSession());

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(createButton);
        contentPanel.add(Box.createVerticalGlue());

        hostPanel.add(contentPanel, BorderLayout.CENTER);
        return hostPanel;
    }

    private void displayLoadingDialog(){
        ExecutorService thread = Executors.newCachedThreadPool();
        thread.submit(()->{
            // DISPLAY LOADING DIALOG
            loadDialog = new JDialog((Frame) null, "Loading...", true);
            loadDialog.setLayout(new BorderLayout());
            loadDialog.setSize(300, 150);
            loadDialog.setLocationRelativeTo(null);
            loadDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            loadDialog.setUndecorated(true);
            loadDialog.getContentPane().setBackground(new Color(230, 238, 255));

            loadDialog.setShape(new RoundRectangle2D.Double(0, 0, 300, 150, 30, 30));

            JPanel contents = new JPanel();
            contents.setLayout(new GridBagLayout()); // Use GridBagLayout for centering
            contents.setBackground(new Color(230, 238, 255)); // Match dialog background color
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = GridBagConstraints.RELATIVE;
            gbc.insets = new Insets(10, 10, 10, 10); // Add padding
            gbc.anchor = GridBagConstraints.CENTER;

            JLabel loadLabel = new JLabel("SwiftSync Review");
            loadLabel.setHorizontalAlignment(JLabel.CENTER);
            loadLabel.setFont(new Font("Arial", Font.BOLD, 16));
            contents.add(loadLabel, gbc);

            JLabel verLabel = new JLabel(Constants.VERSION);
            verLabel.setHorizontalAlignment(JLabel.CENTER);
            verLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            verLabel.setForeground(new Color(86, 86, 86));
            contents.add(verLabel, gbc);

            loadDialog.add(contents, BorderLayout.CENTER);

            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 15));
            progressBar.setBackground(new Color(31, 134, 189));
            loadDialog.add(progressBar, BorderLayout.SOUTH);

            loadDialog.setVisible(true);
        });
    }

    private void joinSession(String sessionId) {
        if (launched) {
            try {
                boolean success = client.enterSession(sessionId);
                if(success){
                    client.currentSession = sessionId;
                    JOptionPane.showMessageDialog(null, "Successfully entered session " + sessionId, "Session Joined", JOptionPane.INFORMATION_MESSAGE);
                    try {
                        titleLabel.setVisible(false);
                        dashPanel.setVisible(false);
                        loadParticipantUI();
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Failed to load participant UI: " + e.getMessage(), "UI Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to enter session " + sessionId, "Unknown Error Occurred", JOptionPane.ERROR_MESSAGE);
                }
            } catch (InvalidSessionID e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Invalid Session ID", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            showConnectionErrorDialog();
        }
    }

    private void createSession(){
        if (launched) {
            String sessionId = client.requestNewSession();
            if(sessionId == null){
                JOptionPane.showMessageDialog(null, "Failed to create new session", "Unknown Error Occurred", JOptionPane.ERROR_MESSAGE);
                return;
            }
            client.currentSession = sessionId;
            JOptionPane.showMessageDialog(null, "New session created with ID " + sessionId, "Session Created", JOptionPane.INFORMATION_MESSAGE);
                titleLabel.setVisible(false);
                dashPanel.setVisible(false);
            loadHostUI();
        } else {
            showConnectionErrorDialog();
        }
    }

    private void loadParticipantUI() {
        println("Loading participant UI...", ConsoleColors.GREEN);
        setTitle("SSR Client Session " + client.currentSession + " - Participant");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        this.role = Role.PARTICIPANT;

        // Main Layout for Participant UI
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Sidebar for Session Details and Client Settings
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Session Details Panel
        sessionDetailsPanel = new JPanel();
        sessionDetailsPanel.setMinimumSize(new Dimension(200, 100));
        sessionDetailsPanel.setBorder(BorderFactory.createTitledBorder("Session Details"));
        sessionDetailsPanel.setPreferredSize(new Dimension(200, 100)); // Adjusted size for more space in settings panel
        sidebarPanel.add(sessionDetailsPanel);

        // Client Settings Panel
        clientSettingsPanel = new JPanel();
        clientSettingsPanel.setBorder(BorderFactory.createTitledBorder("Client Settings"));
        JScrollPane clientSettingsScrollPane = new JScrollPane(clientSettingsPanel);
        clientSettingsScrollPane.setPreferredSize(new Dimension(200, 300)); // Adjusted size for more space in settings panel
        sidebarPanel.add(clientSettingsScrollPane);

        // JSplitPane to allow resizing
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainPanel);
        splitPane.setDividerLocation(200); // Initial position of the divider
        splitPane.setResizeWeight(0.2); // Give more weight to the main panel

        // Media and Proxy Controls Container
        JPanel mediaContainerPanel = new JPanel(new BorderLayout());

        // Media Player Panel
        mediaPlayerPanel = new MediaPlayerPanel(this.background, this.background2, this.foreground);
        mediaContainerPanel.add(mediaPlayerPanel, BorderLayout.CENTER);

        currentMediaFileLabel = new JLabel();

        // Proxy Control Panel
        JPanel proxyControlPanel = new JPanel();
        proxyControlPanel.setBorder(BorderFactory.createTitledBorder("Proxy Controls"));
        proxyControlPanel.setLayout(new GridBagLayout());
        proxyControlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        // Row 1: Link Proxy Media Button and Proxy Location Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JButton linkProxyButton = new JButton("Link Proxy Media");
        proxyControlPanel.add(linkProxyButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JTextField proxyLocationField = new JTextField("C:/User/home/examplelocation");
        proxyLocationField.setEditable(false);
        proxyControlPanel.add(proxyLocationField, gbc);

        // Row 2: Run Proxy Check Button and Proxy Status Label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JButton runProxyCheckButton = new JButton("Run Proxy Check");
        proxyControlPanel.add(runProxyCheckButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        proxyStatusLabel = new JLabel("All Media Found (0 clips missing)", SwingConstants.LEFT);
        proxyStatusLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        proxyStatusLabel.setForeground(Color.GREEN);
        proxyControlPanel.add(proxyStatusLabel, gbc);

        mediaContainerPanel.add(proxyControlPanel, BorderLayout.SOUTH);
        mainPanel.add(mediaContainerPanel, BorderLayout.CENTER);

        add(splitPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        println("Participant UI loaded successfully", ConsoleColors.GREEN);

        selectProxyDirectory();
    }

    public void setProxyStatus(String status) {
        proxyStatusLabel.setText(status);
    }

    public void setProxyStatusColor(Color color) {
        proxyStatusLabel.setForeground(color);
    }

    // region Host-Specific Methods

    private void loadHostUI() {
        setTitle("SSR Client Session " + client.currentSession + " - Host");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        this.role = Role.HOST;

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));

        // Session Details Panel
        sessionDetailsPanel = new JPanel();
        sessionDetailsPanel.setBorder(BorderFactory.createTitledBorder("Session Details"));
        sessionDetailsPanel.setPreferredSize(new Dimension(200, 100)); // Adjusted size for more space in settings panel
        sessionDetailsPanel.setMinimumSize(new Dimension(200, 100));
        sidebarPanel.add(sessionDetailsPanel);

        // Client Settings Panel
        clientSettingsPanel = new JPanel();
        clientSettingsPanel.setBorder(BorderFactory.createTitledBorder("Client Settings"));
        JScrollPane clientSettingsScrollPane = new JScrollPane(clientSettingsPanel);
        clientSettingsScrollPane.setPreferredSize(new Dimension(200, 300)); // Adjusted size for more space in settings panel
        sidebarPanel.add(clientSettingsScrollPane);

        sidebarPanel.add(sessionDetailsPanel);
        sidebarPanel.add(clientSettingsPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarPanel, mainPanel);
        splitPane.setDividerLocation(200);
        splitPane.setResizeWeight(0.2);

        mediaPlayerPanel = new MediaPlayerPanel(background, background2, foreground);
        JPanel mediaContainerPanel = new JPanel(new BorderLayout());
        mediaContainerPanel.add(mediaPlayerPanel, BorderLayout.CENTER);

        // Create a JPanel with key bindings
        JPanel mediaControlPanel = new JPanel();
        mediaControlPanel.setLayout(new BoxLayout(mediaControlPanel, BoxLayout.Y_AXIS));
        mediaControlPanel.setBorder(BorderFactory.createTitledBorder("Media Controls"));
            JPanel btnPanel = new JPanel();
            currentMediaFileLabel = new JLabel();

        // Create buttons for media file navigation
        JButton selectMediaButton = new JButton("Select Media");
            selectMediaButton.addActionListener(e -> selectMedia());
        JButton selectMediaDirButton = new JButton("Select Media Directory");
            selectMediaDirButton.addActionListener(e -> selectMediaDirectory());
        JButton previousMediaButton = new JButton("Previous");
            previousMediaButton.addActionListener(e -> prevMedia());
        JButton nextMediaButton = new JButton("Next");
            nextMediaButton.addActionListener(e -> nextMedia());

        btnPanel.add(selectMediaButton);
        btnPanel.add(selectMediaDirButton);
        btnPanel.add(previousMediaButton);
        btnPanel.add(nextMediaButton);

        mediaControlPanel.add(btnPanel);
        mediaControlPanel.add(currentMediaFileLabel);

        mediaContainerPanel.add(mediaControlPanel, BorderLayout.SOUTH);
        mainPanel.add(mediaContainerPanel, BorderLayout.CENTER);

        mediaContainerPanel.add(mediaControlPanel, BorderLayout.SOUTH);
        mainPanel.add(mediaContainerPanel, BorderLayout.CENTER);

        add(splitPane, BorderLayout.CENTER);
        revalidate();
        repaint();

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    nextMedia();
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    prevMedia();
                } else if (e.getKeyCode() == KeyEvent.VK_OPEN_BRACKET) {
                    prevFrame();
                } else if (e.getKeyCode() == KeyEvent.VK_CLOSE_BRACKET) {
                    nextFrame();
                }
            }
        });

        selectMediaDirectory();
        updateMediaFileLabel();

        nextMedia();
        prevMedia();
    }

    private void nextMedia() {
        File dir = currentMediaFile.getParentFile();
        File[] files = listVideoFiles(dir);
        int currentIndex = Arrays.asList(files).indexOf(currentMediaFile);
        if (currentIndex < files.length - 1) {
            currentMediaFile = files[currentIndex + 1];
            client.setMediaPlayerTo(currentMediaFile.getPath());
            updateMediaFileLabel();
        }
    }

    private void prevMedia() {
        File dir = currentMediaFile.getParentFile();
        File[] files = listVideoFiles(dir);
        int currentIndex = Arrays.asList(files).indexOf(currentMediaFile);
        if (currentIndex > 0) {
            currentMediaFile = files[currentIndex - 1];
            client.setMediaPlayerTo(currentMediaFile.getPath());
            updateMediaFileLabel();
        }
    }

    private void selectMediaDirectory() {
        JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Media Directory");
            fileChooser.setAcceptAllFileFilterUsed(false);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = fileChooser.getSelectedFile();
            File[] currentMediaDirectoryFiles = selectedDirectory.listFiles();

            if (currentMediaDirectoryFiles != null) {
                File marker = null;
                for (File file : currentMediaDirectoryFiles) {
                    if (file.isFile()) {
                        marker = file;
                    }
                }
                if(marker != null){
                    currentMediaFile = marker;
                    client.setMediaPlayerTo(currentMediaFile.getPath());
                    updateMediaFileLabel();
                }
            }
        }
    }

    private void selectMedia(){
        JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle("Select Media File");
            fileChooser.setAcceptAllFileFilterUsed(false);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentMediaFile = fileChooser.getSelectedFile();
            client.setMediaPlayerTo(currentMediaFile.getPath());
            updateMediaFileLabel();
        }
    }

    // endregion

    // region Participant-Specific Methods

    private void selectProxyDirectory(){
        JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setDialogTitle("Select Proxy Directory");
            fileChooser.setAcceptAllFileFilterUsed(false);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentProxyDirectory = fileChooser.getSelectedFile();
        }
    }

    private File findProxyFile(String filename){
        return findFilename(filename, currentProxyDirectory);
    }

    private File findFilename(String target, File dir){
        File[] files = dir.listFiles();
        if(files != null){
            for(File file : files){
                if(file.getName().equals(target) && file.isFile()){
                    return file;
                }
            }
            for(File file : files){
                if(file.isDirectory()){
                    File result = findFilename(target, file);
                    if(result != null){
                        return result;
                    }
                }
            }
        }
        return null;
    }

    // endregion

    private void nextFrame(){ // when the "]" key is pressed
        // go to the next frame but only for the local device
        mediaPlayerPanel.nextFrame();
    }

    private void prevFrame(){ // when the "[" key is pressed
        // go to the previous frame but only for the local device
        mediaPlayerPanel.prevFrame();
    }

    private void updateMediaFileLabel() {
        currentMediaFileLabel.setText(currentMediaFile.getName());
    }

    public static File[] listVideoFiles(File dir){
        File[] files = dir.listFiles();

        if (files != null) {
            // List of common video file extensions
            String[] videoExtensions = new String[] {
                    ".mp4", ".avi", ".mkv", ".flv", ".mov", ".wmv", ".m4v", ".webm", ".mpg", ".mpeg", ".3gp", ".ogv", ".vob"
            };

            files = Arrays.stream(files)
                    .filter(file -> {
                        String name = file.getName().toLowerCase();
                        return Arrays.stream(videoExtensions).anyMatch(name::endsWith);
                    })
                    .toArray(File[]::new);

            switch (sortSetting) {
                case DATE_CREATED:
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    break;
                case DATE_MODIFIED:
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                    break;
                case NAME:
                    Arrays.sort(files, Comparator.comparing(File::getName));
                    break;
            }
        }

        return files;
    }

    public static File[] listAllFiles(File dir){
        // Get the list of files in the directory
        File[] files = dir.listFiles();

        // Check if files is not null
        if (files != null) {
            // Sort the files based on the sort setting
            switch (sortSetting) {
                case DATE_CREATED:
                    // Sort by date created
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified));
                    break;
                case DATE_MODIFIED:
                    // Sort by date modified
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                    break;
                case NAME:
                    // Sort by name
                    Arrays.sort(files, Comparator.comparing(File::getName));
                    break;
            }
        }

        return files;
    }

    private static void showConnectionErrorDialog() {
        JOptionPane.showMessageDialog(null,
                "You need to be connected to WiFi or Ethernet to launch the application.",
                "Network Connection Required",
                JOptionPane.ERROR_MESSAGE);
    }

    public static boolean isConnectedToWiFi() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface networkInterface : Collections.list(networkInterfaces)) {
                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    String name = networkInterface.getDisplayName().toLowerCase();
                    if (name.contains("wifi") || name.contains ("wi-fi") || name.contains("wlan") || name.contains("ethernet") || name.contains("eth")) {
                        return true;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        new SSR_Client();
    }

    enum Role {
        HOST, PARTICIPANT
    }

    enum SortSetting {
        DATE_CREATED,
        DATE_MODIFIED,
        NAME
    }

    class PlaceholderTextField extends JTextField {
        public PlaceholderTextField(String placeholder) {
            setText(placeholder);
            setForeground(Color.GRAY);
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (getText().equals(placeholder)) {
                        setText("");
                        setForeground(Color.BLACK);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (getText().isEmpty()) {
                        setText(placeholder);
                        setForeground(Color.GRAY);
                    }
                }
            });
        }
    }
}
