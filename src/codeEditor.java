import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;

public class codeEditor extends JFrame {

    private JTextPane textArea;
    private JLabel statusLabel;
    private JTree fileTree;
    private JSplitPane sideBar;
    JFileChooser fileChooser;
    private JScrollPane scrollPane;
    private final Map<String, ImageIcon> fileIconMap;
    private DefaultMutableTreeNode selectedNode, newNode;
    private DefaultTreeModel treeModel;
    private File parentDir;
    private File folder;
    private JTabbedPane tabbedPane;
    private final Map<String, Component> openedFiles;
    private final String[] programmingExtensions = new String[]{"java", "cpp", "py", "html", "css", "js", "c", "ipynb"};
    private final String[] imageExtensions = new String[]{"png", "jpg", "jpeg", "gif", "bmp"};

    public codeEditor() {
        setTitle("Code Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);
        setBackground(Color.DARK_GRAY);

        textArea = new JTextPane();
        statusLabel = new JLabel();
        fileTree = new JTree();
        scrollPane = new JScrollPane();
        fileIconMap = new HashMap<>();
        openedFiles = new HashMap<>();

        createMenuBar();
        createCodeTextArea();
        createStatusBar();
        createToolbar();
        createContentPane();
        loadFileIcons();
        setVisible(true);
    }

    private void loadFileIcons() {
        //programming file icons
        fileIconMap.put("c", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\c.png"));
        fileIconMap.put("cpp", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\c-.png"));
        fileIconMap.put("html", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\html.png"));
        fileIconMap.put("java", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\java.png"));
        fileIconMap.put("js", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\js.png"));
        fileIconMap.put("json", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\json.png"));
        fileIconMap.put("python", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\python.png"));
        fileIconMap.put("jupyter", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\notebook.png"));

        //image file icons
        fileIconMap.put("png", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\png.png"));
        fileIconMap.put("jpg", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\jpg.png"));
        fileIconMap.put("jpeg", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\jpeg.png"));
        fileIconMap.put("gif", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\gif.png"));
        fileIconMap.put("bmp", new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\bmp.png"));
        // Add more icons for other file types as needed
    }

    private void createContentPane() {
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(Color.DARK_GRAY);
        contentPane.setForeground(Color.WHITE);
        Border border = BorderFactory.createEmptyBorder();
        contentPane.setBorder(border);
        contentPane.add(createCodeTextArea(), BorderLayout.CENTER);
        contentPane.add(createNavBar(), BorderLayout.NORTH);

        JPanel sideBar = new JPanel(new BorderLayout());
        sideBar.setBackground(Color.DARK_GRAY);
        sideBar.setForeground(Color.WHITE);
        sideBar.setBorder(border);
        sideBar.add(viewSideBar(), BorderLayout.CENTER);
        sideBar.add(createToolbar(), BorderLayout.WEST);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);
        mainPanel.setForeground(Color.WHITE);
        mainPanel.setBorder(border);
        mainPanel.add(sideBar, BorderLayout.WEST);
        mainPanel.add(contentPane, BorderLayout.CENTER);
        mainPanel.add(createStatusBar(), BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private Component createNavBar() {
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navBar.setBackground(Color.DARK_GRAY);
        navBar.setPreferredSize(new Dimension(getWidth(), 30));
        navBar.setMaximumSize(new Dimension(getWidth(), 30));
        navBar.setMinimumSize(new Dimension(getWidth(), 30));

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(Color.DARK_GRAY);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setPreferredSize(new Dimension(getWidth() - 220, 30));
        tabbedPane.setMaximumSize(new Dimension(getWidth() - 220, 30));
        tabbedPane.setMinimumSize(new Dimension(getWidth() - 220, 30));

        navBar.add(tabbedPane);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int selectedIndex = tabbedPane.getSelectedIndex();
                if (selectedIndex >= 0) {
                    JTextPane selectedTab = (JTextPane) tabbedPane.getSelectedComponent();
                    textArea.setText(selectedTab.getText());
                }
            }
        });

        return navBar;
    }

    private Component createCodeTextArea() {
        textArea = new JTextPane();
        textArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
        textArea.setBackground(Color.DARK_GRAY);
        textArea.setForeground(Color.WHITE);

        StyledDocument styledDocument = textArea.getStyledDocument();
        Style defaultStyle = styledDocument.getStyle(StyleContext.DEFAULT_STYLE);
        Style keywordStyle = styledDocument.addStyle("KeywordStyle", defaultStyle);

        StyleConstants.setForeground(keywordStyle, Color.CYAN);
        StyleConstants.setBold(keywordStyle, true);

        Style commentStyle = styledDocument.addStyle("CommentStyle", defaultStyle);
        StyleConstants.setForeground(commentStyle, Color.GREEN);

        Style stringLiteralStyle = styledDocument.addStyle("StringLiteralStyle", defaultStyle);
        StyleConstants.setForeground(stringLiteralStyle, Color.ORANGE);

        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                statusLabel.setText("Start Typing");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                statusLabel.setText("Removed Edited");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                statusLabel.setText("Updated Editing");
            }
        });
        return textArea;
    }

    private JLabel createStatusBar() {
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder());
        statusLabel.setBackground(Color.DARK_GRAY);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setMaximumSize(new Dimension(getWidth(), 20));
        return statusLabel;
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        Border border = BorderFactory.createEmptyBorder();
        menuBar.setMargin(new Insets(5, 5,5, 5));
        menuBar.setBackground(Color.DARK_GRAY);
        menuBar.setForeground(Color.WHITE);
        menuBar.setBorder(border);
        JMenu fileMenu = new JMenu("File");
        fileMenu.setBackground(Color.DARK_GRAY);
        fileMenu.setForeground(Color.WHITE);
        JMenu newMenu = new JMenu("New");
        newMenu.setBackground(Color.DARK_GRAY);
        newMenu.setForeground(Color.DARK_GRAY);
        JMenuItem newFileMenuItem = new JMenuItem("New File");
        newFileMenuItem.setBackground(Color.DARK_GRAY);
        newFileMenuItem.setForeground(Color.WHITE);
        JMenuItem newFolderMenuItem = new JMenuItem("New Folder");
        newFolderMenuItem.setBackground(Color.DARK_GRAY);
        newFolderMenuItem.setForeground(Color.WHITE);
        JMenu openMenu = new JMenu("Open");
        openMenu.setBackground(Color.DARK_GRAY);
        openMenu.setForeground(Color.DARK_GRAY);
        JMenuItem openFileMenuItem = new JMenuItem("Open File");
        openFileMenuItem.setBackground(Color.DARK_GRAY);
        openFileMenuItem.setForeground(Color.WHITE);
        JMenuItem openFolderMenuItem = new JMenuItem("Open Folder");
        openFolderMenuItem.setBackground(Color.DARK_GRAY);
        openFolderMenuItem.setForeground(Color.WHITE);
        JMenu editMenu = new JMenu("Edit");
        editMenu.setBackground(Color.DARK_GRAY);
        editMenu.setForeground(Color.WHITE);
        JMenuItem undoMenu = new JMenuItem("Undo");
        undoMenu.setBackground(Color.DARK_GRAY);
        undoMenu.setForeground(Color.WHITE);
        JMenuItem redoMenu = new JMenuItem("Redo");
        redoMenu.setBackground(Color.DARK_GRAY);
        redoMenu.setForeground(Color.WHITE);
        JMenuItem cutMenu = new JMenuItem("Cut");
        cutMenu.setBackground(Color.DARK_GRAY);
        cutMenu.setForeground(Color.WHITE);
        JMenuItem copyMenu = new JMenuItem("Copy");
        copyMenu.setBackground(Color.DARK_GRAY);
        copyMenu.setForeground(Color.WHITE);
        JMenuItem pasteMenu = new JMenuItem("Paste");
        pasteMenu.setBackground(Color.DARK_GRAY);
        pasteMenu.setForeground(Color.WHITE);
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setBackground(Color.DARK_GRAY);
        saveMenuItem.setForeground(Color.WHITE);
        JMenuItem deleteMenu = new JMenuItem("Delete");
        deleteMenu.setBackground(Color.DARK_GRAY);
        deleteMenu.setForeground(Color.WHITE);
        JMenu viewMenu = new JMenu("View");
        viewMenu.setBackground(Color.DARK_GRAY);
        viewMenu.setForeground(Color.WHITE);
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setBackground(Color.DARK_GRAY);
        helpMenu.setForeground(Color.WHITE);
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setBackground(Color.DARK_GRAY);
        aboutMenuItem.setForeground(Color.WHITE);

        menuBar.add(fileMenu);
        fileMenu.add(newMenu);
        newMenu.add(newFileMenuItem);
        newMenu.add(newFolderMenuItem);
        fileMenu.add(openMenu);
        openMenu.add(openFileMenuItem);
        openMenu.add(openFolderMenuItem);
        menuBar.add(editMenu);
        editMenu.add(undoMenu);
        editMenu.add(redoMenu);
        editMenu.add(cutMenu);
        editMenu.add(copyMenu);
        editMenu.add(pasteMenu);
        editMenu.add(saveMenuItem);
        editMenu.add(deleteMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        helpMenu.add(aboutMenuItem);
        setJMenuBar(menuBar);

        newFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFileCreated();
            }
        });

        newFolderMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolderCreated();
            }
        });

        openFileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });

        openFolderMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFolder();
            }
        });

        undoMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoMenu.setBackground(Color.DARK_GRAY);
                undoMenu.setForeground(Color.WHITE);
                undoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                undoLastAction();
                statusLabel.setText("Undo");
            }
        });

        redoMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                redoMenu.setBackground(Color.DARK_GRAY);
                redoMenu.setForeground(Color.WHITE);
                redoMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                redoLastAction();
                statusLabel.setText("Redo");
            }
        });

        cutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cutMenu.setBackground(Color.DARK_GRAY);
                cutMenu.setForeground(Color.WHITE);
                cutMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                JOptionPane.showMessageDialog(codeEditor.this, "Cut to clipboard");
                cutAction();
                statusLabel.setText("Cut");
            }
        });

        copyMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyMenu.setBackground(Color.DARK_GRAY);
                copyMenu.setForeground(Color.WHITE);
                copyMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                JOptionPane.showMessageDialog(codeEditor.this, "Copied to clipboard");
                copyAction();
                statusLabel.setText("Copy");
            }
        });

        pasteMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteMenu.setBackground(Color.DARK_GRAY);
                pasteMenu.setForeground(Color.WHITE);
                pasteMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                JOptionPane.showMessageDialog(codeEditor.this, "Pasted from clipboard");
                pasteAction();
                statusLabel.setText("Paste");
            }
        });

        saveMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveMenuItem.setBackground(Color.DARK_GRAY);
                saveMenuItem.setForeground(Color.WHITE);
                saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
                JOptionPane.showMessageDialog(codeEditor.this, "File saved");
                saveFile();
            }
        });

        aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAboutDialog();
            }
        });
    }

    private void openFile() {
        fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            JTextPane newFileTextPane = new JTextPane();
            newFileTextPane.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
            newFileTextPane.setBackground(Color.DARK_GRAY);
            newFileTextPane.setForeground(Color.WHITE);
            // New main tabbed pane to hold file-specific tabbed panes
            JTabbedPane mainTabbedPane = new JTabbedPane();
            mainTabbedPane.addTab(file.getName(), newFileTextPane); // Use mainTabbedPane instead of tabbedPane
            openFileInEditor(file, newFileTextPane); // Pass the new JTextPane instance
            addFileToNavBar(file.getName(), newFileTextPane);
        }
    }

    private void addFileToNavBar(String fileName, JTextPane textArea) {
        if (!openedFiles.containsKey(fileName)) {
            openedFiles.put(fileName, textArea);
            tabbedPane.addTab(fileName, textArea);
        } else {
            tabbedPane.setSelectedComponent(openedFiles.get(fileName));
        }
    }

    private void openFileInEditor(File file, JTextPane textPane) {
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textPane.setText(content.toString());
                statusLabel.setText("File Opened: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void undoLastAction() {
        UndoManager undoManager = new UndoManager();
        undoManager.undo();
        statusLabel.setText("Action undone");
    }

    private void redoLastAction() {
        UndoManager undoManager = new UndoManager();
        undoManager.redo();
        statusLabel.setText("Action restored");
    }

    private void cutAction() {
        textArea.cut();
        statusLabel.setText("Text cut");
    }

    private void copyAction() {
        textArea.copy();
        statusLabel.setText("Text copied");
    }

    private void pasteAction() {
        textArea.paste();
        statusLabel.setText("Text pasted");
    }

    private void saveFile() {
        fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(textArea.getText());
                statusLabel.setText("File saved: " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void newFolderCreated() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showSaveDialog(codeEditor.this) == JFileChooser.APPROVE_OPTION) {
            parentDir = fileChooser.getSelectedFile();
            String folderName = JOptionPane.showInputDialog(codeEditor.this, "Enter folder name:");
            if (folderName != null && !folderName.isEmpty()) {
                File newFolder = new File(parentDir, folderName);
                if (!newFolder.exists()) {
                    boolean created = newFolder.mkdir();
                    if (created) {
                        selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                        if (selectedNode != null) {
                            newNode = new DefaultMutableTreeNode(folderName);
                            selectedNode.add(newNode);
                            treeModel = (DefaultTreeModel) fileTree.getModel();
                            treeModel.reload(selectedNode);
                            JOptionPane.showMessageDialog(codeEditor.this, "Folder created successfully.");
                            statusLabel.setText("New folder created successfully");
                        }
                    } else {
                        JOptionPane.showMessageDialog(codeEditor.this, "Failed to create folder.");
                        statusLabel.setText("Operation to create new folder failed");
                    }
                } else {
                    JOptionPane.showMessageDialog(codeEditor.this, "Folder already exists.");
                    statusLabel.setText("Operation failed since the file with same name already exists");
                }
            }
        }
    }

    private void newFileCreated() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showSaveDialog(codeEditor.this) == JFileChooser.APPROVE_OPTION) {
            parentDir = fileChooser.getSelectedFile();
            String fileName = JOptionPane.showInputDialog(codeEditor.this, "Enter file name:");
            if (fileName != null && !fileName.isEmpty()) {
                File newFile = new File(parentDir, fileName);
                if (!newFile.exists()) {
                    try {
                        boolean created = newFile.createNewFile();
                        if (created) {
                            selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                            if (selectedNode != null) {
                                newNode = new DefaultMutableTreeNode(fileName);
                                selectedNode.add(newNode);
                                treeModel = (DefaultTreeModel) fileTree.getModel();
                                treeModel.reload(selectedNode);
                                JOptionPane.showMessageDialog(codeEditor.this, "File created successfully.");
                                statusLabel.setText("Operation to create new file succeeded");
                            }
                        } else {
                            JOptionPane.showMessageDialog(codeEditor.this, "Failed to create file.");
                            statusLabel.setText("Operation to create new file failed");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(codeEditor.this, "File already exists.");
                    statusLabel.setText("Operation failed since the file with same name already exists");
                }
            }
        } else {
            JOptionPane.showMessageDialog(codeEditor.this, "Operation cancelled by user.");
            statusLabel.setText("Operation cancelled by user or file path is invalid");
        }
    }

    private void showAboutDialog() {
        String message = "Code Editor v1.0\n\n" +
                "This is a simple code editor application.\n" +
                "It allows you to create, open, edit, and save text files.\n" +
                "You can also manage files and folders using the sidebar.\n" +
                "\n" +
                "Author: Nanvneet Shandilya\n" +
                "Email: navneetkumarroy5740084@gmail.com";

        JOptionPane.showMessageDialog(this, message, "About Code Editor", JOptionPane.INFORMATION_MESSAGE);
    }

    private Component createToolbar() {
        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        Border border = BorderFactory.createEmptyBorder();
        toolBar.setBorder(border);
        toolBar.setBackground(Color.DARK_GRAY);
        toolBar.setPreferredSize((new Dimension(40, getHeight())));
        toolBar.setMaximumSize((new Dimension(40, getHeight())));
        toolBar.setMinimumSize((new Dimension(40, getHeight())));
        toolBar.addSeparator();

        JToggleButton projectButton = new JToggleButton();
        projectButton.setBorder(border);
        projectButton.setBackground(Color.DARK_GRAY);
        projectButton.setIcon(new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\project.png"));

        JButton runButton = new JButton();
        runButton.setBorder(border);
        runButton.setBackground(Color.DARK_GRAY);
        runButton.setForeground(Color.WHITE);
        runButton.setIcon(new ImageIcon("F:\\Internship Projects\\codeEditor\\img\\run.png"));

        projectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (projectButton.isSelected()) {
                    // Hide the sideBar by setting its preferred size to zero
                    sideBar.setPreferredSize(new Dimension(200, sideBar.getHeight()));
                } else {
                    // Restore the original preferred size of the sideBar
                    sideBar.setPreferredSize(new Dimension(0, sideBar.getHeight()));
                }
                // Revalidate the split pane to update its layout
                SwingUtilities.getWindowAncestor(projectButton).revalidate();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runSelectedFile();
            }
        });

        toolBar.add(projectButton);
        toolBar.addSeparator();
        toolBar.add(runButton);
        return toolBar;
    }

    private void runSelectedFile() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to run.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fileName = selectedNode.getUserObject().toString();
        String fileExtension = getFileExtension(fileName);

        if (!isProgrammingFile(fileExtension)) {
            JOptionPane.showMessageDialog(this, "The selected file is not a runnable programming file.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File selectedFile = new File(folder.getPath() + File.separator + fileName);

        try {
            ProcessBuilder processBuilder;

            if (isWindows()) {
                // For Windows, use "cmd /c" to execute the file using the default associated application
                processBuilder = new ProcessBuilder("cmd", "/c", selectedFile.getAbsolutePath());
            } else {
                // For other platforms, use "xdg-open" to open the file with the default associated application
                processBuilder = new ProcessBuilder("xdg-open", selectedFile.getAbsolutePath());
            }

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                JOptionPane.showMessageDialog(this, "File executed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to execute the file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred while running the file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win");
    }

    private void openFolder() {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();
            this.folder = new File(folder.getPath());
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(folder.getName());
            loadFolderIntoSidebar(folder, rootNode);
            makeTreeInSidebar(rootNode);
        }
    }

    private Component viewSideBar() {
        JPanel sideBarPanel = new JPanel(new BorderLayout());
        sideBar = new JSplitPane();
        sideBar.setBackground(Color.DARK_GRAY);
        sideBar.setForeground(Color.WHITE);
        sideBar.setBorder(BorderFactory.createEmptyBorder());

        fileTree = new JTree();
        fileTree.setBackground(Color.DARK_GRAY);
        fileTree.setForeground(Color.WHITE);
        fileTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        fileTree.setCellRenderer(new FileTreeCellRenderer());
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (node == null) return;
                Object nodeInfo = node.getUserObject();
                if (node.isLeaf()) {
                    String fileName = nodeInfo.toString();
                    if (isProgrammingFile(getFileExtension(fileName)) || isImageFile(getFileExtension(fileName))) {
                        File selectedFile = new File(folder.getPath() + File.separator + fileName);
                        JTabbedPane mainTabbedPane = new JTabbedPane();
                        JTextPane newFileTextPane = new JTextPane();
                        mainTabbedPane.addTab(selectedFile.getName(), newFileTextPane); // Use mainTabbedPane instead of tabbedPane
                        openFileInEditor(selectedFile, newFileTextPane); // Pass the new JTextPane instance
                        addFileToNavBar(selectedFile.getName(), newFileTextPane);
                    }
                }
            }
        });

        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        treeScrollPane.setBackground(Color.DARK_GRAY);
        treeScrollPane.setForeground(Color.WHITE);
        treeScrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        treeScrollPane.setPreferredSize(new Dimension(200, getHeight()));
        treeScrollPane.setMaximumSize(new Dimension(200, getHeight()));
        treeScrollPane.setMinimumSize(new Dimension(0, getHeight()));

        sideBarPanel.add(treeScrollPane, BorderLayout.CENTER);
        sideBar.add(sideBarPanel, JSplitPane.LEFT);
        sideBar.setDividerLocation(200);
        sideBar.setEnabled(false);
        sideBar.setPreferredSize(new Dimension(200, getHeight()));
        sideBar.setMaximumSize(new Dimension(200, getHeight()));
        sideBar.setMinimumSize(new Dimension(0, getHeight()));
        return sideBar;
    }

    private class FileTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();

            if (userObject instanceof File file) {
                String name = file.getName();
                if (file.isDirectory()) {
                    setIcon(fileIconMap.get("folder")); // Set custom folder icon
                } else {
                    String extension = getFileExtension(name);
                    setIcon(fileIconMap.getOrDefault(extension, fileIconMap.get("file"))); // Set custom file icon or default file icon
                }
            }

            return this;
        }
    }

    private void loadFolderIntoSidebar(File folder, DefaultMutableTreeNode parentDir) {
        File[] files = folder.listFiles();
        if (files == null) return; // Folder is empty or not accessible

        for (File file : files) {
            if (file.isHidden() || !file.canRead()) continue; // Skip hidden or unreadable files
            if (file.isDirectory()) {
                DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(file.getName());
                parentDir.add(folderNode);
                loadFolderIntoSidebar(file, folderNode);
            } else {
                String extension = getFileExtension(file.getName());
                if (isProgrammingFile(extension) || isImageFile(extension)) {
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file.getName());
                    parentDir.add(fileNode);
                }
            }
        }
    }

    private void makeTreeInSidebar(DefaultMutableTreeNode rootNode) {
        treeModel = new DefaultTreeModel(rootNode);
        fileTree.setModel(treeModel);
    }

    private boolean isImageFile(String extension) {
        for (String imageExtension : imageExtensions) {
            if (extension.equals(imageExtension)) {
                return true;
            }
        }
        return false;
    }

    private boolean isProgrammingFile(String extension) {
        for (String programmingExtension : programmingExtensions) {
            if (extension.equals(programmingExtension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String name) {
        int dotIndex = name.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < name.length() - 1) {
            return name.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new codeEditor();
            }
        });
    }
}