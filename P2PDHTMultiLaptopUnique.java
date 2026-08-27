import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class P2PDHTMultiLaptopUnique extends JFrame {

    // =========================================================================
    // [KODE INTI - DATA STRUKTUR JARINGAN DHT]
    // =========================================================================
    private final ArrayList<Integer> activeNodes = new ArrayList<>(Arrays.asList(3, 7, 11, 17, 22, 28));
    private int myNodeId = 3; 
    private final ArrayList<String> localStorage = new ArrayList<>();
    private final Map<Integer, String> nodeIpMap = new HashMap<>();
    private final Map<Integer, String> nodeNameMap = new HashMap<>();

    // =========================================================================
    // [KODE GUI - DEKLARASI ELEMEN KOMPONEN JAVASWING]
    // =========================================================================
    private final Map<Integer, JPanel> nodeRowPanels = new HashMap<>();  
    private final Map<Integer, JLabel> statusIndicators = new HashMap<>();
    private final Map<Integer, JLabel> nodeNameLabels = new HashMap<>();   
    private final Map<Integer, JTextArea> fileLists = new HashMap<>();    
    private final Map<Integer, JProgressBar> storageBars = new HashMap<>();
    
    private JComboBox<Integer> cbMyNode; 
    private JTextField txtIp3, txtIp7, txtIp11, txtIp17, txtIp22, txtIp28; 
    private JTextField txtName3, txtName7, txtName11, txtName17, txtName22, txtName28; 
    private JTextField txtFileName; 
    private JTextArea logArea;      
    private ServerSocket serverSocket; 
    private JButton btnClearLog;    
    private File fileTargetYangDipilih; 

    // =========================================================================
    // [KODE GUI - DEKLARASI TEMA WARNA (PALETTE)]
    // =========================================================================
    private final Color BG_LIGHT_MAIN = new Color(244, 246, 249); 
    private final Color BG_CARD_WHITE = new Color(255, 255, 255);  
    private final Color TEXT_DARK = new Color(45, 52, 54);         
    private final Color COLOR_TEAL = new Color(0, 151, 178);       
    private final Color COLOR_EMERALD = new Color(46, 204, 113);   
    private final Color COLOR_CORAL = new Color(231, 76, 60);      
    private final Color BG_CONSOLE = new Color(248, 249, 250);     

    // =========================================================================
    // [KODE GUI & INTI - KONSTRUKTOR UTAMA]
    // =========================================================================
    public P2PDHTMultiLaptopUnique() {
        nodeNameMap.put(3, "Nama 1");
        nodeNameMap.put(7, "Nama 2");
        nodeNameMap.put(11, "Nama 3");
        nodeNameMap.put(17, "Nama 4");
        nodeNameMap.put(22, "Nama 5");
        nodeNameMap.put(28, "Nama 6");

        for (int node : activeNodes) {
            nodeIpMap.put(node, "127.0.0.1");
        }

        setTitle("ENTERPRISE MONITORING HUB - P2P DHT TOPIK 3");
        setSize(1240, 780); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_LIGHT_MAIN);
        setLayout(new BorderLayout(15, 15));

        // =========================================================================
        // [PANEL KIRI (CONTROL CENTER)]
        // =========================================================================
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BG_LIGHT_MAIN);
        leftPanel.setPreferredSize(new Dimension(380, 0));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 10));

        JPanel identityPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        identityPanel.setBackground(BG_CARD_WHITE);
        identityPanel.setBorder(createCustomTitledBorder("IDENTITY CONFIG"));
        
        cbMyNode = new JComboBox<>(activeNodes.toArray(new Integer[0]));
        cbMyNode.setSelectedItem(3); 
        cbMyNode.setBackground(BG_LIGHT_MAIN);
        cbMyNode.setForeground(TEXT_DARK);
        
        JLabel lblChooseNode = new JLabel("Pilih Node ID Laptop Ini:", SwingConstants.LEFT);
        lblChooseNode.setForeground(TEXT_DARK);
        lblChooseNode.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        identityPanel.add(lblChooseNode);
        identityPanel.add(cbMyNode);
        leftPanel.add(identityPanel);
        leftPanel.add(Box.createVerticalStrut(15));

        JPanel actionPanel = new JPanel(new BorderLayout(5, 10));
        actionPanel.setBackground(BG_CARD_WHITE);
        actionPanel.setBorder(createCustomTitledBorder("ACTION CENTER"));

        JPanel fileInputPanel = new JPanel(new BorderLayout(5, 5));
        fileInputPanel.setOpaque(false);

        txtFileName = new JTextField();
        txtFileName.setBackground(BG_LIGHT_MAIN);
        txtFileName.setForeground(TEXT_DARK);
        txtFileName.setCaretColor(TEXT_DARK);
        txtFileName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtFileName.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Nama File / Path", TitledBorder.LEFT, TitledBorder.TOP, null, COLOR_TEAL));
        
        JButton btnBrowse = new JButton("📂 BROWSE");
        btnBrowse.setBackground(new Color(52, 73, 94));
        btnBrowse.setForeground(Color.WHITE);
        btnBrowse.setFont(new Font("Segoe UI", Font.BOLD, 10));
        
        fileInputPanel.add(txtFileName, BorderLayout.CENTER);
        fileInputPanel.add(btnBrowse, BorderLayout.EAST);
        actionPanel.add(fileInputPanel, BorderLayout.NORTH);

        JPanel btnGrid = new JPanel(new GridLayout(4, 1, 0, 10));
        btnGrid.setOpaque(false);

        JButton btnUpload = createFuturisticButton("UPLOAD TO NETWORK", COLOR_EMERALD);
        JButton btnSearch = createFuturisticButton("SEARCH FILE (LINEAR RING)", COLOR_TEAL);
        JButton btnDownload = createFuturisticButton("DOWNLOAD FROM PEER", new Color(155, 89, 182));
        btnClearLog = createFuturisticButton("CLEAR LOG FILE", COLOR_CORAL);

        btnGrid.add(btnUpload);
        btnGrid.add(btnSearch);
        btnGrid.add(btnDownload);
        btnGrid.add(btnClearLog); 
        actionPanel.add(btnGrid, BorderLayout.CENTER);
        
        leftPanel.add(actionPanel);
        add(leftPanel, BorderLayout.WEST);

        // =========================================================================
        // [PANEL KANAN (LIVE NETWORK MONITOR)]
        // =========================================================================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BG_LIGHT_MAIN);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 15));

        JPanel listNodesPanel = new JPanel();
        listNodesPanel.setLayout(new BoxLayout(listNodesPanel, BoxLayout.Y_AXIS));
        listNodesPanel.setBackground(BG_LIGHT_MAIN);

        for (int nodeId : activeNodes) {
            JPanel rowCard = new JPanel(new BorderLayout(15, 5));
            rowCard.setBackground(BG_CARD_WHITE);
            rowCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            JPanel nodeInfoSub = new JPanel(new GridLayout(2, 1));
            nodeInfoSub.setOpaque(false);
            
            JLabel lblNode = new JLabel("NODE " + nodeId + " (" + nodeNameMap.get(nodeId) + ")");
            lblNode.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblNode.setForeground(TEXT_DARK);
            
            JLabel lblIndicator = new JLabel("OFFLINE"); 
            lblIndicator.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblIndicator.setForeground(COLOR_CORAL);
            
            nodeInfoSub.add(lblNode);
            nodeInfoSub.add(lblIndicator);
            rowCard.add(nodeInfoSub, BorderLayout.WEST);

            JPanel centerSub = new JPanel(new BorderLayout(5, 5));
            centerSub.setOpaque(false);
            
            JTextArea txtFiles = new JTextArea(2, 30);
            txtFiles.setEditable(false);
            txtFiles.setBackground(BG_LIGHT_MAIN);
            txtFiles.setForeground(TEXT_DARK);
            txtFiles.setFont(new Font("Consolas", Font.PLAIN, 11));
            
            // Menolak pembungkusan teks ke bawah secara mutlak
            txtFiles.setLineWrap(false);       
            txtFiles.setWrapStyleWord(false);
            
            // Memaksa Scrollbar Horizontal untuk selalu muncul agar teks memanjang ke samping[cite: 1]
            JScrollPane spFiles = new JScrollPane(
                txtFiles,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
            );
            
            // Mengunci ukuran area agar layout manager tidak mencekik lebar komponen[cite: 1]
            spFiles.setPreferredSize(new Dimension(350, 60));
            spFiles.setMinimumSize(new Dimension(350, 60));
            spFiles.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 237)));

            JProgressBar progBar = new JProgressBar(0, 10);
            progBar.setValue(0);
            progBar.setStringPainted(true);
            progBar.setString("0/10 Files Loaded");
            progBar.setForeground(COLOR_TEAL);
            progBar.setBackground(new Color(230, 233, 237));

            centerSub.add(spFiles, BorderLayout.CENTER);
            centerSub.add(progBar, BorderLayout.SOUTH);
            rowCard.add(centerSub, BorderLayout.CENTER);

            nodeRowPanels.put(nodeId, rowCard);
            statusIndicators.put(nodeId, lblIndicator);
            nodeNameLabels.put(nodeId, lblNode); 
            fileLists.put(nodeId, txtFiles);
            storageBars.put(nodeId, progBar);

            listNodesPanel.add(rowCard);
            listNodesPanel.add(Box.createVerticalStrut(10)); 
        }

        JScrollPane scrollNodes = new JScrollPane(listNodesPanel);
        scrollNodes.setBorder(createCustomTitledBorder("LIVE CLUSTER MONITOR (6 NODES RING)"));
        scrollNodes.getViewport().setBackground(BG_LIGHT_MAIN);
        rightPanel.add(scrollNodes, BorderLayout.CENTER);
        
        add(rightPanel, BorderLayout.CENTER);

        // =========================================================================
        // [PANEL BAWAH (IP GRID, INPUT NAMA, DAN KONSOL LOG)]
        // =========================================================================
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(BG_LIGHT_MAIN);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JPanel ipLayout = new JPanel(new BorderLayout(5, 5));
        ipLayout.setBackground(BG_CARD_WHITE);
        ipLayout.setBorder(createCustomTitledBorder("IP & NODE IDENTITY CLOUD MANAGEMENT"));

        JPanel ipGrid = new JPanel(new GridLayout(4, 6, 10, 5));
        ipGrid.setOpaque(false);
        
        txtName3 = createFuturisticField("Nama 1");
        txtName7 = createFuturisticField("Nama 2");
        txtName11 = createFuturisticField("Nama 3");
        txtName17 = createFuturisticField("Nama 4");
        txtName22 = createFuturisticField("Nama 5");
        txtName28 = createFuturisticField("Nama 6");

        txtIp3 = createFuturisticField("10.195.213.250");
        txtIp7 = createFuturisticField("10.195.213.214");
        txtIp11 = createFuturisticField("10.195.213.10");
        txtIp17 = createFuturisticField("10.195.213.22");
        txtIp22 = createFuturisticField("10.195.213.252");
        txtIp28 = createFuturisticField("10.195.213.172");

        String[] nameLabels = {"Nama Node 3:", "Nama Node 7:", "Nama Node 11:", "Nama Node 17:", "Nama Node 22:", "Nama Node 28:"};
        for (String name : nameLabels) {
            JLabel lbl = new JLabel(name, SwingConstants.CENTER);
            lbl.setForeground(COLOR_TEAL);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            ipGrid.add(lbl);
        }
        
        ipGrid.add(txtName3); ipGrid.add(txtName7); ipGrid.add(txtName11);
        ipGrid.add(txtName17); ipGrid.add(txtName22); ipGrid.add(txtName28);

        String[] ipLabels = {"Node 3 IP Address:", "Node 7 IP Address:", "Node 11 IP Address:", "Node 17 IP Address:", "Node 22 IP Address:", "Node 28 IP Address:"};
        for (String ip : ipLabels) {
            JLabel lbl = new JLabel(ip, SwingConstants.CENTER);
            lbl.setForeground(TEXT_DARK);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            ipGrid.add(lbl);
        }
        
        ipGrid.add(txtIp3); ipGrid.add(txtIp7); ipGrid.add(txtIp11);
        ipGrid.add(txtIp17); ipGrid.add(txtIp22); ipGrid.add(txtIp28);

        JButton btnConnect = createFuturisticButton("CONNECT TO CLUSTER", COLOR_TEAL);
        ipLayout.add(ipGrid, BorderLayout.CENTER);
        ipLayout.add(btnConnect, BorderLayout.EAST);

        logArea = new JTextArea(8, 0);
        logArea.setEditable(false);
        logArea.setBackground(BG_CONSOLE);
        logArea.setForeground(new Color(44, 62, 80)); 
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(createCustomTitledBorder("CENTRALIZED TOPOLOGY PROCESS MONITOR"));

        bottomPanel.add(ipLayout, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // =========================================================================
        // [MOUSE LISTENER - AKSI KLIK FILE DI TABEL MONITOR KANAN]
        // =========================================================================
        for (Integer nodeId : fileLists.keySet()) {
            JTextArea view = fileLists.get(nodeId);
            view.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        int caretPos = view.getCaretPosition();
                        int lineNum = view.getLineOfOffset(caretPos);
                        int start = view.getLineStartOffset(lineNum);
                        int end = view.getLineEndOffset(lineNum);
                        
                        String lineText = view.getText().substring(start, end).trim();
                        
                        if (lineText.startsWith("LIST OF") || lineText.startsWith("---") || lineText.isEmpty() || lineText.equals("[Storage Empty]")) {
                            return;
                        }
                        
                        txtFileName.setText(lineText);
                        
                    } catch (Exception ex) {
                        // Aman dari crash jika area kosong diklik
                    }
                }
            });
        }

        // =========================================================================
        // [INTERAKSI LISTENERS (LOGIKA TOMBOL)]
        // =========================================================================
        btnBrowse.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih Berkas untuk Sistem P2P DHT");
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                fileTargetYangDipilih = fileChooser.getSelectedFile();
                txtFileName.setText(fileTargetYangDipilih.getName());
                appendRealTimeLog("[FILE EXPLORER] Selected file target: " + fileTargetYangDipilih.getName());
            }
        });

        btnClearLog.addActionListener(e -> {
            logArea.setText(""); 
            appendRealTimeLog("[CONSOLE STATUS] System process trace logs wiped clean by user.");
        });

        btnConnect.addActionListener(e -> {
            myNodeId = (int) cbMyNode.getSelectedItem(); 
            
            nodeNameMap.put(3, txtName3.getText().trim());
            nodeNameMap.put(7, txtName7.getText().trim());
            nodeNameMap.put(11, txtName11.getText().trim());
            nodeNameMap.put(17, txtName17.getText().trim());
            nodeNameMap.put(22, txtName22.getText().trim());
            nodeNameMap.put(28, txtName28.getText().trim());

            nodeIpMap.put(3, txtIp3.getText().trim());
            nodeIpMap.put(7, txtIp7.getText().trim());
            nodeIpMap.put(11, txtIp11.getText().trim());
            nodeIpMap.put(17, txtIp17.getText().trim());
            nodeIpMap.put(22, txtIp22.getText().trim());
            nodeIpMap.put(28, txtIp28.getText().trim());

            for (int nodeId : activeNodes) {
                nodeNameLabels.get(nodeId).setText("NODE " + nodeId + " (" + nodeNameMap.get(nodeId) + ")");
            }

            cbMyNode.setEnabled(false);
            txtName3.setEditable(false); txtName7.setEditable(false); txtName11.setEditable(false);
            txtName17.setEditable(false); txtName22.setEditable(false); txtName28.setEditable(false);
            txtIp3.setEditable(false); txtIp7.setEditable(false); txtIp11.setEditable(false);
            txtIp17.setEditable(false); txtIp22.setEditable(false); txtIp28.setEditable(false);
            btnConnect.setEnabled(false);

            JPanel myRowCard = nodeRowPanels.get(myNodeId);
            myRowCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(241, 196, 15), 2),
                    BorderFactory.createEmptyBorder(12, 15, 12, 15)
            ));

            prepopulateData(myNodeId);
            updateStorageView(myNodeId, localStorage);

            new Thread(this::startSocketServer).start();
            new Thread(this::startHeartbeatCheck).start();

            appendRealTimeLog("[SYSTEM-INIT] Node " + myNodeId + " (" + nodeNameMap.get(myNodeId) + ") online. Socket listener active on port " + (8000 + myNodeId) + ".");
        });

        btnUpload.addActionListener(e -> {
            if (fileTargetYangDipilih == null || !fileTargetYangDipilih.exists()) {
                JOptionPane.showMessageDialog(this, "Silakan pilih berkas terlebih dahulu menggunakan tombol BROWSE!", "Berkas Belum Dipilih", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String fileName = fileTargetYangDipilih.getName();
            int hashKey = calculateHash(fileName);
            int successor = findSuccessor(hashKey);
            
            appendRealTimeLog("\n[WRITE FILE ASLI] Mengirim: " + fileName + " ke Node " + successor);
            
            new Thread(() -> {
                String targetIp = nodeIpMap.get(successor);
                int targetPort = 8000 + successor;
                
                try (Socket s = new Socket(targetIp, targetPort);
                     DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                     DataInputStream dis = new DataInputStream(s.getInputStream());
                     FileInputStream fis = new FileInputStream(fileTargetYangDipilih)) { 
                    
                    dos.writeUTF("WRITE_REAL_FILE;" + fileName + ";" + myNodeId);
                    dos.writeLong(fileTargetYangDipilih.length());
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    dos.flush();
                    
                    String response = dis.readUTF();
                    if ("WRITE_SUCCESS".equals(response)) {
                        appendRealTimeLog(" >> [UPLOAD SUCCESS] File '" + fileName + "' sukses terkirim utuh ke Node " + successor);
                        JOptionPane.showMessageDialog(this, "Berkas asli sukses dialokasikan ke Peer Successor!");
                    }
                    
                } catch (IOException ex) {
                    appendRealTimeLog(" >> [UPLOAD ERROR] Gagal mengirim file: " + ex.getMessage());
                }
            }).start();
        });

        btnSearch.addActionListener(e -> {
            String fileName = txtFileName.getText().trim();
            if (fileName.isEmpty()) {
                appendRealTimeLog("[SEARCH ERROR] Masukkan nama file yang ingin dicari!");
                return;
            }

            int hashKey = calculateHash(fileName);
            appendRealTimeLog("\n>> [PENCARIAN_DHT] Mencari: " + fileName + " | Hash: " + hashKey);

            boolean ditemukanDiLokal = false;
            for (String item : localStorage) {
                if (item.equals(fileName) || item.startsWith(fileName + " (dari Node")) {
                    ditemukanDiLokal = true;
                    break;
                }
            }

            if (ditemukanDiLokal) {
                appendRealTimeLog(" [+] [TARGET MATCH] File '" + fileName + "' ditemukan langsung di Node lokal saya sendiri (" + myNodeId + ")");
                appendRealTimeLog(" >> LOOKUP STATUS  : FILE TERSEDIA (EKSIS)");
                appendRealTimeLog(" >> REPOSITORY HOST : Node " + myNodeId + " (" + nodeNameMap.get(myNodeId) + ")");
                appendRealTimeLog(" >> ROUTER METRIC  : Network hops = 0 Hop");
                return; 
            }

            int currentIndex = activeNodes.indexOf(myNodeId);
            int nextNode = activeNodes.get((currentIndex + 1) % activeNodes.size());
            String nextIp = nodeIpMap.get(nextNode);
            int nextPort = 8000 + nextNode;

            appendRealTimeLog(" [-] Bukan milik saya. Menembak paket ke tetangga depan (Node " + nextNode + " di IP " + nextIp + ")...");
            
            new Thread(() -> {
                sendTCPMessage(nextIp, nextPort, "SEARCH;" + fileName + ";" + hashKey + ";1");
            }).start();
        });

        btnDownload.addActionListener(e -> {
            String fileName = txtFileName.getText().trim();
            if (fileName.isEmpty()) {
                appendRealTimeLog("[DOWNLOAD ERROR] Masukkan atau pilih nama file yang ingin di-download!");
                return;
            }

            String userHome = System.getProperty("user.home");
            File tempFolder = new File(userHome, "Downloads");
            if (!tempFolder.exists()) {
                tempFolder = new File(".");
            }
            
            final File targetFolder = tempFolder;
            int hashKey = calculateHash(fileName);
            int targetSuccessor = findSuccessor(hashKey); 

            appendRealTimeLog("\n[DOWNLOAD REQUEST] Mengunduh file: \"" + fileName + "\"");
            appendRealTimeLog(" -> Menghubungi repositori: Node " + targetSuccessor);
            appendRealTimeLog(" -> Lokasi penyimpanan: " + targetFolder.getAbsolutePath());

            new Thread(() -> {
                String targetIp = nodeIpMap.get(targetSuccessor);
                int targetPort = 8000 + targetSuccessor;

                try (Socket s = new Socket(targetIp, targetPort);
                     DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                     DataInputStream dis = new DataInputStream(s.getInputStream())) {
                    
                    dos.writeUTF("DOWNLOAD;" + fileName);
                    dos.flush();
                    
                    String response = dis.readUTF();
                    if (response != null && response.startsWith("DOWNLOAD_SUCCESS")) {
                        String[] tokens = response.split(";");
                        String receivedFile = tokens[1];
                        
                        File newPhysicalFile = new File(targetFolder, receivedFile);
                        long fileSize = dis.readLong();
                        
                        try (FileOutputStream fos = new FileOutputStream(newPhysicalFile)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            long totalRead = 0;
                            while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                                fos.write(buffer, 0, bytesRead);
                                totalRead += bytesRead;
                            }
                        }

                        boolean sudahAda = false;
                        for (String item : localStorage) {
                            if (item.equals(receivedFile) || item.startsWith(receivedFile + " (dari Node")) {
                                sudahAda = true;
                                break;
                            }
                        }

                        if (!sudahAda) {
                            String infoList = receivedFile + " (dari Node " + targetSuccessor + ")";
                            localStorage.add(infoList);
                            updateStorageView(myNodeId, localStorage); 
                        }
                        
                        appendRealTimeLog(" >> DOWNLOAD STATUS: SUKSES!");
                        appendRealTimeLog(" >> [FILE EXPLORER] Silakan cek folder Downloads kamu, berkas '" + receivedFile + "' sudah mendarat utuh!");
                        
                        JOptionPane.showMessageDialog(this, "Unduhan Selesai!\nFile disimpan di: " + newPhysicalFile.getAbsolutePath());
                        
                    } else {
                        appendRealTimeLog(" >> DOWNLOAD STATUS : GAGAL. File tidak ditemukan di cluster.");
                    }
                } catch (IOException ex) {
                    appendRealTimeLog(" [NET_DISCONNECT] Gagal menghubungi Node " + targetSuccessor);
                }
            }).start();
        });
    }

    private void appendRealTimeLog(String pesan) {
        SwingUtilities.invokeLater(() -> {
            try {
                LocalTime waktuSekarang = LocalTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[HH:mm:ss]");
                String timestamp = waktuSekarang.format(formatter);
                logArea.append(timestamp + " " + pesan + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            } catch (Exception e) {
                System.out.println("Gagal menulis log visual: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // [KODE INTI JARINGAN - SOKET SERVER & PENANGAN REQUEST PEER]
    // =========================================================================
    private void startSocketServer() {
        int myPort = 8000 + myNodeId;
        try {
            serverSocket = new ServerSocket(myPort);
            while (true) {
                Socket socket = serverSocket.accept(); 
                new Thread(() -> handleIncomingClient(socket)).start(); 
            }
        } catch (IOException ex) {
            System.out.println("Server listener alert: " + ex.getMessage());
        }
    }

    private void handleIncomingClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream());
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {

            String message = dis.readUTF();
            if (message == null) return;

            String[] tokens = message.split(";");
            String cmd = tokens[0];

            if (cmd.equals("WRITE_REAL_FILE")) {
                String fName = tokens[1];
                String pengirimId = tokens[2]; 
                
                File folderLokal = new File("node_" + myNodeId + "_storage");
                if (!folderLokal.exists()) folderLokal.mkdir();
                
                File fileTujuan = new File(folderLokal, fName);
                long fileSize = dis.readLong();
                
                try (FileOutputStream fos = new FileOutputStream(fileTujuan)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalRead = 0;
                    while (totalRead < fileSize && (bytesRead = dis.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
                        fos.write(buffer, 0, bytesRead);
                        totalRead += bytesRead;
                    }
                }
                
                String infoList = fName + " (dari Node " + pengirimId + ")";
                if (!localStorage.contains(infoList)) {
                    localStorage.add(infoList);
                    try {
                        updateStorageView(myNodeId, localStorage); 
                    } catch (Exception ex) {
                        System.out.println("Gagal refresh GUI Penerima: " + ex.getMessage());
                    }
                }
                
                dos.writeUTF("WRITE_SUCCESS");
                dos.flush();
                appendRealTimeLog(" -> [RECEIVE SUCCESS] File '" + fName + "' disimpan di folder lokal dari Node " + pengirimId);
            }
            else if (cmd.equals("DOWNLOAD")) {
                String fileName = tokens[1];
                File folderLokal = new File("node_" + myNodeId + "_storage");
                File fileTarget = new File(folderLokal, fileName);

                if (!fileTarget.exists()) {
                    fileTarget = new File(fileName);
                }

                if (fileTarget.exists() && !fileTarget.isDirectory()) {
                    dos.writeUTF("DOWNLOAD_SUCCESS;" + fileName);
                    dos.writeLong(fileTarget.length()); 
                    
                    try (FileInputStream fis = new FileInputStream(fileTarget)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            dos.write(buffer, 0, bytesRead);
                        }
                    }
                    dos.flush();
                    appendRealTimeLog(" -> [DOWNLOAD SENT] File '" + fileName + "' berhasil ditransfer.");
                } else {
                    dos.writeUTF("DOWNLOAD_FAILED");
                    dos.flush();
                }
            }
            else if (cmd.equals("PING")) {
                String filesStr = String.join("#", localStorage);
                dos.writeUTF("PONG;" + filesStr + ";" + nodeNameMap.get(myNodeId));
                dos.flush();
            }
            else if (cmd.equals("SEARCH")) {
                String fileName = tokens[1];
                int hashKey = Integer.parseInt(tokens[2]);
                int hop = Integer.parseInt(tokens[3]);
                int targetSuccessor = findSuccessor(hashKey);

                boolean ditemukan = false;
                for (String item : localStorage) {
                    if (item.equals(fileName) || item.startsWith(fileName + " (dari Node")) {
                        ditemukan = true;
                        break;
                    }
                }

                if (myNodeId == targetSuccessor || ditemukan) {
                    appendRealTimeLog(" [+] [TARGET MATCH] File '" + fileName + "' ditemukan di Node " + myNodeId);
                    dos.writeUTF("FOUND_RESP;" + myNodeId + ";FOUND_LOCAL;" + hop);
                } else {
                    int currentIndex = activeNodes.indexOf(myNodeId);
                    int nextNode = activeNodes.get((currentIndex + 1) % activeNodes.size());
                    String nextIp = nodeIpMap.get(nextNode);
                    int nextPort = 8000 + nextNode;

                    appendRealTimeLog(" [->] [ROUTING] Meneruskan SEARCH ke Node " + nextNode);
                    String relayRes = forwardSearch(nextIp, nextPort, message);
                    dos.writeUTF(relayRes);
                }
                dos.flush();
            }
        } catch (IOException e) {
            System.out.println("Session handler dropped: " + e.getMessage());
        }
    }

    // =========================================================================
    // [KODE INTI JARINGAN - HEARTBEAT MANAGEMENT]
    // =========================================================================
    private void startHeartbeatCheck() {
        while (true) {
            for (int nodeId : activeNodes) {
                if (nodeId == myNodeId) {
                    updateNodeStatusUI(nodeId, true, String.join("#", localStorage));
                    continue;
                }
                final String targetIp = nodeIpMap.get(nodeId);
                final int targetPort = 8000 + nodeId;

                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(targetIp, targetPort), 700);
                    DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                    DataInputStream dis = new DataInputStream(s.getInputStream());

                    dos.writeUTF("PING");
                    dos.flush();
                    
                    String res = dis.readUTF();
                    if (res != null && res.startsWith("PONG")) {
                        String[] parts = res.split(";");
                        String filesList = parts.length > 1 ? parts[1] : "";
                        
                        if (parts.length > 2) {
                            String remoteNodeName = parts[2];
                            nodeNameMap.put(nodeId, remoteNodeName);
                            SwingUtilities.invokeLater(() -> {
                                nodeNameLabels.get(nodeId).setText("NODE " + nodeId + " (" + remoteNodeName + ")");
                            });
                        }
                        updateNodeStatusUI(nodeId, true, filesList);
                    }
                } catch (IOException e) {
                    updateNodeStatusUI(nodeId, false, "");
                }
            }
            try { Thread.sleep(3000); } catch (InterruptedException e) { break; }
        }
    }

    // =========================================================================
    // [KODE INTI JARINGAN - TCP CLIENT UTILS]
    // =========================================================================
    private void sendTCPMessage(String ip, int port, String msg) {
        try (Socket s = new Socket(ip, port);
             DataOutputStream dos = new DataOutputStream(s.getOutputStream());
             DataInputStream dis = new DataInputStream(s.getInputStream())) {
            
            dos.writeUTF(msg);
            dos.flush();
            
            String response = dis.readUTF();
            if (response != null && response.startsWith("FOUND_RESP")) {
                String[] resTokens = response.split(";");
                String statusMsg = resTokens[2].equals("FOUND_LOCAL") ? "FILE TERSEDIA (EKSIS)" : "FILE KOSONG / BELUM DI-UPLOAD";
                appendRealTimeLog(" >> LOOKUP STATUS  : " + statusMsg);
                appendRealTimeLog(" >> REPOSITORY HOST : Node " + resTokens[1] + " (" + nodeNameMap.get(Integer.parseInt(resTokens[1])) + ")");
                appendRealTimeLog(" >> ROUTER METRIC  : Network hops = " + resTokens[3] + " Hop");
            }
        } catch (IOException e) {
            appendRealTimeLog(" [NET_DISCONNECT] Cannot reach target link node on port " + port + ". Peer node dead.");
        }
    }

    private String forwardSearch(String ip, int port, String msg) {
        String[] tokens = msg.split(";");
        int currentHop = Integer.parseInt(tokens[3]);
        String updatedMsg = tokens[0] + ";" + tokens[1] + ";" + tokens[2] + ";" + (currentHop + 1);

        try (Socket s = new Socket(ip, port);
             DataOutputStream dos = new DataOutputStream(s.getOutputStream());
             DataInputStream dis = new DataInputStream(s.getInputStream())) {
            s.setSoTimeout(2000);
            
            dos.writeUTF(updatedMsg);
            dos.flush();
            
            return dis.readUTF(); 
        } catch (IOException e) {
            return "FOUND_RESP;-1;Ring topology split error;0";
        }
    }

    // =========================================================================
    // [KODE INTI JARINGAN - LOGIKA FUNGSI MATEMATIKA DHT]
    // =========================================================================
    private int calculateHash(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.contains("lagua")) return 5;
        if (lower.contains("videob")) return 9;
        if (lower.contains("dokumenc")) return 13;
        if (lower.contains("imaged")) return 20;
        if (lower.contains("tugase")) return 25;
        return Math.abs(fileName.hashCode()) % 32; 
    }

    private int findSuccessor(int hashKey) {
        for (int node : activeNodes) {
            if (node >= hashKey) return node; 
        }
        return activeNodes.get(0); 
    }

    private void prepopulateData(int id) {
        if (id == 7) localStorage.add("laguA.mp3");
        if (id == 11) localStorage.add("videoB.mp4");
        if (id == 17) localStorage.add("dokumenC.pdf");
        if (id == 22) localStorage.add("imageD.jpg");
        if (id == 28) localStorage.add("tugasE.docx");
    }

    // =========================================================================
    // [KODE GUI - REFRESHER UI STATUS MONITOR]
    // =========================================================================
    private void updateNodeStatusUI(int nodeId, boolean online, String files) {
    SwingUtilities.invokeLater(() -> {
        try {
            JLabel lbl = statusIndicators.get(nodeId);
            JPanel card = nodeRowPanels.get(nodeId);
            JTextArea view = fileLists.get(nodeId);
            JProgressBar pb = storageBars.get(nodeId);

            if (online) {
                lbl.setText("ONLINE");
                lbl.setForeground(COLOR_EMERALD);
                card.setBackground(BG_CARD_WHITE);

                if (files == null || files.trim().isEmpty() || files.equals("[Storage Empty]")) {
                    view.setText("[Storage Empty]");
                    pb.setValue(0);
                    pb.setString("0 / 10 Active Slots");
                } else {
                    view.setFont(new java.awt.Font("Courier New", java.awt.Font.PLAIN, 12));
                    
                    // Bersihkan kurung siku bawaan toString() jika ada
                    String cleanedFiles = files.replaceAll("^\\[|\\]$", "").trim();

                    // =========================================================================
                    // 🌟 SOLUSI: Potong berdasarkan Pagar (#), BUKAN KOMA (,) lagi!
                    // =========================================================================
                    String[] fileArray = cleanedFiles.split("#");

                    StringBuilder tabelVisual = new StringBuilder();
                    tabelVisual.append("LIST OF FILENAMES:\n");
                    tabelVisual.append("---------------------------------------------------------------------------------\n");

                    int fileCount = 0;
                    for (String file : fileArray) {
                        String namaFile = file.trim();
                        
                        if (!namaFile.isEmpty() && 
                            !namaFile.equalsIgnoreCase("Node Unreachable") && 
                            !namaFile.equalsIgnoreCase("[Storage Empty]")) {
                            
                            // Jika ada embel-embel node pengirim, bersihkan untuk tampilan visual
                            if (namaFile.contains(" (dari Node ")) {
                                namaFile = namaFile.substring(0, namaFile.indexOf(" (dari Node ")).trim();
                            }
                            
                            tabelVisual.append(namaFile).append("\n");
                            fileCount++;
                        }
                    }

                    view.setText(tabelVisual.toString().trim());
                    pb.setValue(fileCount);
                    pb.setString(fileCount + " / 10 Active Slots");
                }
            } else {
                lbl.setText("OFFLINE");
                lbl.setForeground(new java.awt.Color(239, 68, 68));
                card.setBackground(new java.awt.Color(249, 250, 251));
                view.setText("Node Unreachable");
                pb.setValue(0);
                pb.setString("Node Unreachable");
            }
        } catch (Exception ex) {
            System.out.println("Bentrokan visual terhindari: " + ex.getMessage());
        }
    });
    }

    private void updateStorageView(int nodeId, ArrayList<String> list) {
        SwingUtilities.invokeLater(() -> {
            try {
                String filesStr = String.join("#", list);
                updateNodeStatusUI(nodeId, true, filesStr);
            } catch (Exception e) {
                System.out.println("Gagal sinkronisasi storage view: " + e.getMessage());
            }
        });
    }

    private TitledBorder createCustomTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1), title);
        border.setTitleColor(COLOR_TEAL);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        return border;
    }

    private JButton createFuturisticButton(String text, Color accentColor) {
        JButton btn = new JButton(text);
        btn.setBackground(accentColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return btn;
    }

    private JTextField createFuturisticField(String text) {
        JTextField fld = new JTextField(text);
        fld.setBackground(BG_LIGHT_MAIN);
        fld.setForeground(TEXT_DARK);
        fld.setCaretColor(TEXT_DARK);
        fld.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fld.setHorizontalAlignment(JTextField.CENTER);
        fld.setBorder(BorderFactory.createLineBorder(new Color(200, 204, 210)));
        return fld;
    }

    // =========================================================================
    // [MAIN METHOD (EKSEKUSI PROGRAM)]
    // =========================================================================
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}
        
        SwingUtilities.invokeLater(() -> new P2PDHTMultiLaptopUnique().setVisible(true));
    }
}